# SKtarbucks 기능 확장 (By 김성재) 

- 1차 팀과제 (https://github.com/rlatjdwo555/SKtarbucks)
- 3개의 서비스 추가 (결제, 배달, 푸쉬알람)

# Table of contents

- 서비스 시나리오
- 분석/설계
  - Event Storming
  - 헥사고날 아키텍처 다이어그램 도출
- 구현
  - 시나리오 테스트결과
  - DDD의 적용
  - Gateway 적용
  - 폴리글랏 퍼시스턴스
  - 동기식 호출과 Fallback 처리
  - 비동기식 호출과 Eventual Consistency
 - 운영
   - CI/CD 설정
   - Self-healing
   - 무정지 재배포
   - ConfigMap 사용

# 서비스 시나리오 확장 

## 기존 시나리오
1. 관리자가 카페 메뉴를 등록한다. 
2. 고객이 주문을 한다. <br>
 2-1. 카페에서 주문가능 수를 확인한다. <br>
 2-2. 음료의 주문가능 수가 감소한다. <br>
 
3. 주문이 완료되면 제조 서비스에 데이터가 등록된다.
4. 고객이 주문을 취소한다. <br>
 4-1. 음료의 주문가능 수가 증가한다. <br>
 4-2. 주문상태가 취소로 변경된다. <br>
 
5. 관리자가 메뉴 정보를 삭제한다. <br>
 5-1. 고객의 해당 메뉴 주문상태가 취소로 변경된다. 
 
## 확장 시나리오 
1. 관리자가 카페 메뉴를 등록한다. 
2. 고객이 주문을 한다. 

3. 결제 서비스에 데이터가 추가된다. <br>
 3-1. 결제가 승인되면 제조관리 서비스에 데이터가 추가된다. <br>
 3-2. 결제가 취소되면 주문이 취소되고, 주문가능 수가 증가한다. <br>
 
4. 고객이 주문을 취소한다. <br>
 4-1. 주문상태가 취소로 변경된다. <br>
 4-2. 결제상태가 취소로 변경된다. <br>
 4-3. 제조상태가 취소로 변경된다. <br>
 4-4. 음료의 주문가능 수가 증가한다. <br>

5. 음료 제조가 시작되면 사용자에게 제조시작 푸쉬 알람을 보낸다.
6. 바리스타가 음료 제조를 완료한다.<br>
 6-1. 주문타입이 'TAKEOUT'이면 고객에게 주문완료 푸쉬 알람을 보낸다. <br> 
 6-2. 주문타입이 'DELIVERY"이면 배달 서비스에 데이터를 등록한다. <br> 

7. 배달원이 배달을 출발한다. <br>
 7-1. 고객에게 배달시작 푸쉬 알람을 보낸다. <br>
 7-2. 주문상태가 배달중 으로 변경된다. <br> 

8. 배달원이 배달을 완료한다. <br> 
 8-1. 고객에게 배달완료 푸쉬 알람을 보낸다. <br>
 8-2. 주문상태가 완료로 변경된다. <br> 


# 분석/설계

## 기존 Event Storming
![eventstorming](https://i.imgur.com/RH6rs40.png)


## 확장 Event Storming
![모델링](https://user-images.githubusercontent.com/28692938/126950447-d305ffee-9160-433e-823d-e3ae627ffe57.PNG)



## 헥사고날 아키텍처 다이어그램 도출

### 기존 다이어그램

![image](https://i.imgur.com/XLlfwa9.png)
```
- CQRS 를 위한 Mypage 서비스만 DB를 구분하여 적용
- Cafe에서 Order의 음료 주문가능 수를 확인할 때 RESTful Request/Response 로 구현
```

### 확장 다이어그램

![헥사고날](https://user-images.githubusercontent.com/28692938/126983702-a9f85f63-70ec-4e32-a3b7-968ce51e8605.PNG)
```
- 3개의 서비스 추가 (Payment, Delivery, Push)
- Production 에서 Order의 주문타입을 확인할 때 RESTful Request/Response 로 구현
- MyPage의 ViewHandler에서 신규 서비스의 Event를 수신하도록 확장구현 
```


# 구현

## 시나리오 테스트결과


#### 1.카페 주인이 카페메뉴(메뉴명, 가격, 주문가능 수)를 등록한다.
```
http localhost:8080/cafes cafeNm="Americano" price=3000 stock=10
http localhost:8080/cafes cafeNm="Cafelatte" price=3500 stock=10
http localhost:8080/cafes cafeNm="Ade" price=5000 stock=10
http localhost:8080/cafes cafeNm="Cake" price=8000 stock=10

http localhost:8080/cafes
```
![카페메뉴 등록](https://user-images.githubusercontent.com/28692938/126986011-f506b418-619c-4c10-b7fc-4a04a9dd489f.PNG)



#### 2. 고객이 주문을 요청한다.
- 주문타입은 배달로 요청한다. <br>
orderType="`DELIVERY`"
```
http localhost:8080/orders cafeId=1 custNm="KIM" count=2 orderType="DELIVERY"
```
![주문1](https://user-images.githubusercontent.com/28692938/126986470-d03c2373-e638-4afd-9550-971879c485f3.PNG)

#### 3. 결제 서비스에 데이터가 추가된다.
- 결제 상태값이 결제 요청됨으로 등록된다. <br>
paymentStatus = "`PAYMENT_REQUESTED`"
```
http localhost:8080/payments
```
![주문1-결제1](https://user-images.githubusercontent.com/28692938/126986472-463eb448-ab29-4000-8e60-a49475633f7d.PNG)

- 결제가 승인되면 제조관리 서비스에 데이터가 추가된다.
```
http PATCH localhost:8080/payments/1 paymentStatus="APPROVED"

http localhost:8080/productions
```
![주문1-결제승인](https://user-images.githubusercontent.com/28692938/126986474-4817aaad-f786-46e6-9f1c-50a88143495b.PNG)
![주문1-제조](https://user-images.githubusercontent.com/28692938/126986489-f8ec1fa3-6161-4b82-bb5e-4b496fc06299.PNG)


#### 4. 바리스타가 음료를 제조한다.
 - 제조가 시작되면 고객에게 푸쉬 알람을 보낸다.
 - 주문의 상태가 변경된다.
 orderStatus = `MAKING`
 ```
 http localhost:8080/pushes
 
 http localhost:8080/order/1
 ```
 ![주문1-제조푸쉬](https://user-images.githubusercontent.com/28692938/126986469-9cd81573-cc31-45d4-a3ec-c7e868d7a88c.PNG)
 
 ![주문1-제조 상태 변경](https://user-images.githubusercontent.com/28692938/126986488-d1e915be-82be-4343-ba54-05246957125c.PNG)
 
#### 5. 바리스타가 음료 제조를 완료한다.
- 주문타입이 '`DELIVERY`' 만 배달 서비스에 데이터를 등록한다.
```
http PATCH localhost:8080/productions/1 status="COMPLETED"

http localhost:8080/deliveries
```
![주문1-제조완료](https://user-images.githubusercontent.com/28692938/126986467-31427b88-894e-4947-917a-336f936f3435.PNG)
![주문1-배달시작](https://user-images.githubusercontent.com/28692938/126986479-c86ae0d8-be65-4aa2-9a06-bc19fc08f795.PNG)

- 주문타입이 '`TAKEOUT`'이면 고객에게 주문완료 푸쉬 알람을 보낸다.
- 주문 상태가 **완료**로 변경된다. 
orderStatus = '`COMPLETED`'
```
http localhost:8080/orders cafeId=2 custNm="YOON" count=3 orderType="TAKEOUT"

http PATCH localhost:8080/payments/2 paymentStatus="APPROVED"
http PATCH localhost:8080/productions/2 status="COMPLETED"

http localhost:8080/pushes
http localhost:8080/orders/2
```
![주문2-푸쉬](https://user-images.githubusercontent.com/28692938/126990000-8b1c03ad-7318-4759-993c-dfe8bf47f771.PNG)
 
![주문2-주문상태-완료](https://user-images.githubusercontent.com/28692938/126989999-5fff4de4-bbbf-4e8f-ae81-e15aa072ee5c.PNG)

#### 6. 배달원이 배달을 시작한다.
- 고객에게 배달 시작 푸쉬 알람을 보낸다.
- 주문의 상태값이 **배달 중**으로 변경된다.
orderStatus = "`DELIVERY_IN_PROGRESS`"
```
http localhost:8080/pushes
http localhost:8080/orders/1
```
![주문1-배달시작 푸쉬](https://user-images.githubusercontent.com/28692938/126986475-b279c8a6-7185-438f-be02-5bd66aadaa49.PNG)

![주문1-상태변경-배달중](https://user-images.githubusercontent.com/28692938/126986486-14e39842-f97b-4497-8c91-5fcc658ce9dd.PNG)

#### 7. 배달을 완료한다.
- 고객에게 배달 완료 푸쉬 알람을 보낸다.
- 주문의 상태값이 **배달 완료**로 변경된다.
orderStatus = '`DELIVERY_COMPLETED`'
```
http PATCH localhost:8080/deliveries/1 status="COMPLETED"

http localhost:8080/pushes
http localhost:8080/orders/1
```
![주문1-배달완료](https://user-images.githubusercontent.com/28692938/126986482-e87f0398-6dd5-4463-8f6d-9f9032376d11.PNG)

![주문1-배달완료 푸쉬](https://user-images.githubusercontent.com/28692938/126986480-77993e34-76dd-44ff-ae46-a05a7c3d253c.PNG)

![주문1-상태변경-배달완료](https://user-images.githubusercontent.com/28692938/126986483-72cd80aa-ee4d-4e4e-9abe-b93453bdb1f9.PNG)

#### 8. 주문 후 결제 여부에 따라 주문 가능 수량이 변경된다.
- 최초 주문 가능 수량을 확인한다. (Ade : 10개)
```
http localhost:8080/cafes/3
```
![3번메뉴 수량](https://user-images.githubusercontent.com/28692938/126991701-793514f7-778d-4e4c-8fe5-1b6072ab3711.PNG)

- 주문 요청 시 주문 가능 수량이 감소한다.
```
http localhost:8080/orders cafeId=3 custNm="PARK" count=5 orderType="DELIVERY"

http localhost:8080/cafes/3
```
![주문3](https://user-images.githubusercontent.com/28692938/126991688-8a98b7fb-5ae2-4c19-be66-4cb91f2d2ddf.PNG)

![3번메뉴 수량 5](https://user-images.githubusercontent.com/28692938/126991698-9f6d478b-a88b-4c3d-a923-bf9a03898543.PNG)

- 결제 취소 시 주문이 취소되며 주문 가능 수량이 증가한다.
- 주문 상태값이 취소로 변경된다. <br>
orderStatus = "`CANCELED`"
```
http PATCH localhost:8080/payments/3 status="CANCELED"

http localhost:8080/cafes/3
http localhost:8080/orders/3
```
![주문3-결제취소](https://user-images.githubusercontent.com/28692938/126991689-5d1a2aaf-8344-45d2-90c8-a8720608f6f2.PNG)
![3번메뉴 수량 10](https://user-images.githubusercontent.com/28692938/126991700-f57dbd15-e3f9-4f4c-a9a6-db5c44715422.PNG)

![주문3-주문상태-결제취소](https://user-images.githubusercontent.com/28692938/126991694-85e88da6-c805-487d-bc1b-8f08b5d5530b.PNG)

#### 9. 고객이 주문을 취소한다.
```
http PATCH localhost:8080/orders/4 status="CANCELED"
```
![주문4-주문취소](https://user-images.githubusercontent.com/28692938/126993578-cd05e08a-4b11-4d93-803e-c31c4e2796a4.PNG)

- 해당 주문의 결제 상태값이 취소로 변경된다.
paymentStatus = "`PAYMENT_CANCELED`"

```
http localhost:8080/payments/4 
```
![주문4-주문취소-결제취소](https://user-images.githubusercontent.com/28692938/126993579-cd489219-3dd7-44f4-91ac-8239c849be41.PNG)

- 해당 주문의 제조 상태값이 취소로 변경된다. 
status = "`CANCELED`"
```
http localhost:8080/productions/4 
```
![주문4-제조취소](https://user-images.githubusercontent.com/28692938/126993593-4cc37adf-a026-42d1-af91-ab2bae9d5187.PNG)

- 주문 내역의 음료 주문가능 수가 증가한다.

![메뉴4](https://user-images.githubusercontent.com/28692938/126993581-adab0570-86e2-4389-abbf-a2c2cc18ac6a.PNG)


## DDD의 적용

3개의 서비스 추가 (Payment, Delivery, Push)
* MyPage 는 CQRS 를 위한 서비스

| MSA | 기능 | port | 조회 API | Gateway 사용시 |
|---|:---:|:---:|---|---|
| Order | 주문 관리 | 8081 | http://localhost:8081/orders | http://OrderManage:8080/orders |
| Cafe  | 카페메뉴 관리 | 8082 | http://localhost:8082/cafes | http://CafeManage:8080/cafes |
| Production | 제조 관리 | 8083 | http://localhost:8083/productions | http://ProductionManage:8080/productions |
| MyPage | 마이페이지 | 8084 | http://localhost:8084/myPages | http://MyPage:8080/myPages |
| Payment | 결제 관리 | 8085 | http://localhost:8085/payments | http://Payment:8080/payments |
| Delivery  | 배달 관리 | 8086 | http://localhost:8086/deliveries | http://DeliveryManage:8080/deliveries |
| Push | 푸쉬알람 관리 | 8087 | http://localhost:8087/pushes | http://PushManage:8080/pushes |


## Gateway 적용

```
spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: OrderManage
          uri: http://localhost:8081
          predicates:
            - Path=/orders/**
        - id: CafeManage
          uri: http://localhost:8082
          predicates:
            - Path=/cafes/** 
        - id: ProductionManage
          uri: http://localhost:8083
          predicates:
            - Path=/productions/** 
        - id: MyPage
          uri: http://localhost:8084
          predicates:
            - Path= /myPages/**
        - id: Payment
          uri: http://localhost:8085
          predicates:
            - Path= /payments/**
        - id: Delivery
          uri: http://localhost:8086
          predicates:
            - Path= /deliveries/**
        - id: PushManage
          uri: http://localhost:8087
          predicates:
            - Path= /pushes/**                
...
```


## 폴리글랏 퍼시스턴스

CQRS 를 위한 Mypage 서비스만 DB를 구분하여 적용함. 인메모리 DB인 hsqldb 사용.

```
pom.xml 에 적용
<!-- 
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
		</dependency>
 -->
		<dependency>
		    <groupId>org.hsqldb</groupId>
		    <artifactId>hsqldb</artifactId>
		    <version>2.4.0</version>
		    <scope>runtime</scope>
		</dependency>
```

- MyPage 조회 결과 
```
http localhost:8080/myPages
```
![myPage](https://user-images.githubusercontent.com/28692938/127083260-6709a4d4-3019-4816-b8e5-a013fbd3e509.PNG)



## 동기식 호출과 Fallback 처리
제조 서비스에서 주문 타입을 확인할 때 제조(Production)->주문(Order) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리

- FeignClient 서비스 구현 
- REST url을 config-map 으로 구현 

```java
# OrderService.java

@FeignClient(name="OrderManage", url="${api.order.url}")
public interface OrderService {

    @RequestMapping(method= RequestMethod.GET, value="/orders/{orderId}", consumes = "application/json")
    public Order getOrderType(@PathVariable("orderId") Long orderId);

}
```

- 제조 완료처리를 한 후 Order의 주문 타입을 확인 
```java
# Production.java

@PostUpdate
public void onPostUpdate(){
    if("COMPLETED".equals(status)){
    
        local.external.Order order = new local.external.Order();
        
        order = ProductionManageApplication.applicationContext.getBean(local.external.OrderService.class)
        .getOrderType(orderId);
           
        switch (order.getOrderType()) {
            case "TAKEOUT":
            ProductionChanged productionChanged = new ProductionChanged();
            BeanUtils.copyProperties(this, productionChanged);
            productionChanged.publishAfterCommit();                            
            break;

            case "DELIVERY":
            ProductionSent productionSent = new ProductionSent();
            BeanUtils.copyProperties(this, productionSent);
            productionSent.publishAfterCommit();    
            break;
           
            default :              
        }
    }
}   
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 주문 관리 시스템이 장애가 나면 요청을 처리하지 못하는 것을 확인


```
#주문관리(Order) 서비스를 내려놓음 

#제조 완료 요청
http PATCH aa3c167bec0054e6793d7dcfba21f874-1341135726.ap-northeast-1.elb.amazonaws.com:8080/deliveries/1 status="COMPLETED"  #Fail


#주문 서비스 재기동
cd CafeManage
mvn spring-boot:run

#제조 완료 요청 처리 
http PATCH aa3c167bec0054e6793d7dcfba21f874-1341135726.ap-northeast-1.elb.amazonaws.com:8080/deliveries/1 status="COMPLETED" #Success   
```

## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

푸쉬 알람 서비스는 비동기식으로 구현하였고 푸쉬 서비스가 내려가 있더라도 다른 서비스에는 영향을 주지 않는다.

- Push 서비스가 내려간 상태에서 주문 및 제조 완료 후, 서비스 재기동시 이벤트 수신을 확인한다.
```
# 푸쉬 서비스 다운 
kubectl delete deploy,svc pushmanage -n skcc-ns

# 주문처리
http aa3c167bec0054e6793d7dcfba21f874-1341135726.ap-northeast-1.elb.amazonaws.com:8080/orders cafeId=5 custNm="CHOI" count=2 orderType="TAKEOUT"

# 결제 승인 및 제조완료 처리
http PATCH aa3c167bec0054e6793d7dcfba21f874-1341135726.ap-northeast-1.elb.amazonaws.com:8080/payments/6 paymentStatus="APPROVED"
http PATCH aa3c167bec0054e6793d7dcfba21f874-1341135726.ap-northeast-1.elb.amazonaws.com:8080/productions/5 status="COMPLETED"

# 푸쉬 서비스 재기동 후 Event 수신 확인
kubectl create deployment pushmanage --image=879772956301.dkr.ecr.ap-northeast-1.amazonaws.com/user07-pushmanage:latest -n skcc-ns
kubectl expose deploy pushmanage --port=8080 -n skcc-ns
```

![푸쉬서비스 다운](https://user-images.githubusercontent.com/28692938/127078238-bb066518-0688-45d2-862e-50d0303d70ba.PNG)
![주문6](https://user-images.githubusercontent.com/28692938/127078241-edfca451-7444-42da-be9e-9eb9429ee2f4.PNG)

![푸쉬 서비스 올리고 결과확인](https://user-images.githubusercontent.com/28692938/127078243-4d5c14be-44b7-44c2-a167-a3ca624cef5b.PNG)


- 음료 제조상태 변경 이벤트를 카프카로 송출한다(Publish)
 
```java
...
    @PostPersist
    public void onPostPersist(){
        ProductionChanged productionChanged = new ProductionChanged();
        BeanUtils.copyProperties(this, productionChanged);
        productionChanged.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){
        if("COMPLETED".equals(status)){

           // 주문 타입 확인을 동기식으로 호출
           local.external.Order order = new local.external.Order();
           order = ProductionManageApplication.applicationContext.getBean(local.external.OrderService.class)
           .getOrderType(orderId);
           
           switch (order.getOrderType()) {
               case "TAKEOUT":
                ProductionChanged productionChanged = new ProductionChanged();
                BeanUtils.copyProperties(this, productionChanged);
                productionChanged.publishAfterCommit();                            
                break;

               case "DELIVERY":
                ProductionSent productionSent = new ProductionSent();
                BeanUtils.copyProperties(this, productionSent);
                productionSent.publishAfterCommit();    
                break;
           
               default :
               
           }
        }
    }
...
```
- Push 서비스에서는 제조상태 변경 이벤트를 수신하도록 PolicyHandler 구현

```java
@StreamListener(KafkaProcessor.INPUT)
    public void wheneverProductionChanged_Push(@Payload ProductionChanged productionChanged){

        if(!productionChanged.isMe()) return;
        
        System.out.println("##### listener ProductionChanged: " + productionChanged.toJson());
            
        Push push = new Push();
        push.setOrderId(productionChanged.getOrderId());

        switch (productionChanged.getStatus()){
            case "MAKING":
             push.setMsg("주문하신 음료를 만들고 있습니다.");
             break;

            case "COMPLETED":
             push.setMsg("주문하신 음료가 준비되었습니다.");
             break;
            
            default :
        }

        pushRepository.save(push);
    }
 ```

# 운영

## CI/CD 설정

각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 AWS CodeBuild를 사용하였으며, 
pipeline build script 는 각 프로젝트 폴더 이하에 buildspec.yml 에 포함되었다.
- CodeBuild 기반으로 CI/CD 파이프라인 구성
MSA 서비스별 CodeBuild 프로젝트 생성하여  CI/CD 파이프라인 구성

![ECR](https://user-images.githubusercontent.com/28692938/127078902-76d3b1aa-8f2a-44f5-a6de-039fd9d6daf8.PNG)

![CodeBuild](https://user-images.githubusercontent.com/28692938/127078901-7b88c5ee-83c1-4954-8a34-797c01ffe346.PNG)


- Git Hook 연결
연결한 Github의 소스 변경 발생 시 자동으로 빌드 및 배포 되도록 Git Hook 연결 설정

![GitHook](https://user-images.githubusercontent.com/28692938/127078904-7a150bc9-4ed5-450b-a545-02d0beb778ef.PNG)

![livenessProve](https://user-images.githubusercontent.com/28692938/127082340-93a2d604-b85c-4cd6-bb15-88caf5bd7c95.PNG)

## Self-healing (Liveness Probe)
Pod는 정상적으로 작동하지만 내부의 어플리케이션이 반응이 없다면, 컨테이너는 의미가 없다.
위와 같은 경우는 어플리케이션의 Liveness probe는 Pod의 상태를 체크하다가, Pod의 상태가 비정상인 경우 kubelet을 통해서 재시작한다.

- 임의대로 Liveness probe에서 path를 잘못된 값으로 변경 후, retry 시도 확인
```
# Liveness probe 설정

livenessProbe:
    httpGet:
        path: '/actuator/healthhhhhh'
        port: 8080
    initialDelaySeconds: 120
    timeoutSeconds: 2
    periodSeconds: 5
    failureThreshold: 5  
```

- pushmanage Pod가 여러번 RESTART 한 것을 확인

![livenessProve](https://user-images.githubusercontent.com/28692938/127082552-0043a57b-73c0-46cf-bbe8-7af080e1e46a.PNG)


## 무정지 재배포 (ReadinessProve)

- buildspec.yaml에 ReadinessProbe 설정 
```
# Readiness probe 설정

readinessProbe:
    httpGet:
      path: '/actuator/health'
      port: 8080
    initialDelaySeconds: 30
    timeoutSeconds: 2
    periodSeconds: 5
    failureThreshold: 10
```

- CI/CD 파이프라인을 통해 새버전으로 재배포 작업 간 siege 모니터링 
```
siege -v -c1 -t120S --content-type "application/json" aa3c167bec0054e6793d7dcfba21f874-1341135726.ap-northeast-1.elb.amazonaws.com:8080/pushes
```
![무정지 재배포](https://user-images.githubusercontent.com/28692938/127084113-3e565141-ad9f-4d3d-912e-d06b36ee61b0.PNG)


- 배포기간 동안 Availability 100% 확인

![무정지 재배포 결과](https://user-images.githubusercontent.com/28692938/127084140-5d725d29-2ebe-4bac-9472-0c5ebfabb7e1.PNG)


## ConfigMap 사용

시스템별로 또는 운영중에 동적으로 변경 가능성이 있는 설정들을 ConfigMap을 사용하여 관리합니다.
Application에서 특정 도메일 URL을 ConfigMap 으로 설정하여 운영/개발등 목적에 맞게 변경가능합니다.  



* Production/buildsepc.yaml 내 ConfigMap 정의
```
...
      - |
        cat <<EOF | kubectl apply -f -
        apiVersion: v1
        kind: ConfigMap
        metadata:
          name: my-config2
          namespace: $_NAMESPACE
        data:
          api.order.url: http://OrderManage:8080
        EOF
      - |
            ...
                  env:
                    - name: api.order.url
                      valueFrom:
                        configMapKeyRef:
                          name: my-config2
                          key: api.order.url 
...
```


* url에 configMap 적용
```java
# OrderService.java

@FeignClient(name="OrderManage", url="${api.order.url}")
public interface OrderService {

    @RequestMapping(method= RequestMethod.GET, value="/orders/{orderId}", consumes = "application/json")
    public Order getOrderType(@PathVariable("orderId") Long orderId);

}
```


* kubectl describe 명령어로 configMap 적용여부 확인 
```
kubectl describe pod productionmanage-5457b77f76-hl877 -n skcc-ns
```

![myconfig](https://user-images.githubusercontent.com/28692938/127079844-40cf9909-a5dd-4d65-8c76-091763e49e43.PNG)




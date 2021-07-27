package local;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long cafeId;
    private int count;
    private int price;
    private String custNm;
    private String cafeNm;
    private String status;
    private String orderType;

    @PrePersist
    public void onPrePersist() throws Exception{
        // 주문가능 수량 상태 확인 ( Req / Res : 동기 방식 호출)
        local.external.Cafe cafe = new local.external.Cafe();
        cafe = OrderManageApplication.applicationContext.getBean(local.external.CafeService.class)
        .getCafeStatus(cafeId);

        // 주문 수량 여부에 따라 처리
        if(cafe.getStock()-count >= 0){
          this.setCafeId(cafeId);
          this.setCafeNm(cafe.getCafeNm());
          this.setPrice(cafe.getPrice()*count);
          this.setCustNm(custNm);
          this.setStatus("REQUESTED");
        }
        else{
            this.setStatus("ORDER_CANCELED");
            throw new Exception("음료 재료 소진으로 주문이 불가합니다.");
        }

    }

    @PostPersist
    public void onPostPersist(){
        Requested requested = new Requested();
        BeanUtils.copyProperties(this, requested);
        requested.publishAfterCommit();
    }  

    @PostUpdate
    public void onPostUpdate(){

        System.out.println("#### onPostUpdate :" + this.toString());

        if("CANCELED".equals(this.getStatus())) {
            Canceled canceled = new Canceled();
            BeanUtils.copyProperties(this, canceled);
            canceled.publishAfterCommit();
        }
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCafeId() {
        return cafeId;
    }

    public void setCafeId(Long cafeId) {
        this.cafeId = cafeId;
    }

    public String getCustNm() {
        return custNm;
    }

    public void setCustNm(String custNm) {
        this.custNm = custNm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCafeNm() {
        return cafeNm;
    }

    public void setCafeNm(String cafeNm) {
        this.cafeNm = cafeNm;
    }

    public void setOrderType(String orderType){
        this.orderType = orderType;
    }

    public String getOrderType(){
        return orderType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setPrice(int price){
        this.price = price;
    }

    public int getPrice(){
        return price;
    }

}

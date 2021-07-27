package local;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Production_table")
public class Production {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private int count;
    private String cafeNm;
    private String custNm;
    private String status;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCafeNm() {
        return cafeNm;
    }

    public void setCafeNm(String cafeNm) {
        this.cafeNm = cafeNm;
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}

package local;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Delivery_table")
public class Delivery {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private String cafeNm;
    private String custNm;
    private String status;

    @PostPersist
    public void onPostPersist(){
        DeliveryStarted deliveryStarted = new DeliveryStarted();
        BeanUtils.copyProperties(this, deliveryStarted);
        deliveryStarted.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){
        if("COMPLETED".equals(status)){
            DeliveryCompleted deliveryCompleted = new DeliveryCompleted();
            BeanUtils.copyProperties(this, deliveryCompleted);
            deliveryCompleted.publishAfterCommit();
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

}

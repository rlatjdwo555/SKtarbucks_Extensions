package local;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Payment_table")
public class Payment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private int price;
    private int count;
    private String custNm;
    private String cafeNm;
    private String paymentStatus;

    @PostPersist
    public void onPostPersist(){

    }

    @PostUpdate
    public void onPostUpdate(){

        System.out.println("#### onPostUpdate :" + this.toString());

        if("APPROVED".equals(this.getPaymentStatus())){
            PaymentApproved paymentApproved = new PaymentApproved();
            BeanUtils.copyProperties(this, paymentApproved);
            paymentApproved.publishAfterCommit();
        }
        else if("CANCELED".equals(this.getPaymentStatus())){
            PaymentCancelled paymentCancelled = new PaymentCancelled();
            BeanUtils.copyProperties(this, paymentCancelled);
            paymentCancelled.publishAfterCommit();
        }

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustNm() {
        return custNm;
    }

    public void setCustNm(String custNm) {
        this.custNm = custNm;
    }

    public String getCafeNm() {
        return cafeNm;
    }

    public void setCafeNm(String cafeNm) {
        this.cafeNm = cafeNm;
    }

    public void setOrderId(Long orderId){
        this.orderId = orderId;
    }

    public Long getOrderId(){
        return orderId;
    }

    public void setPrice(int price){
        this.price = price;
    }

    public int getPrice(){
        return price;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setCount(int count){
        this.count = count;
    }

    public int getCount(){
        return count;
    }

}

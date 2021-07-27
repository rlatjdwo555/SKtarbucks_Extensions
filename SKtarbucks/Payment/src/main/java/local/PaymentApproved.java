package local;

public class PaymentApproved extends AbstractEvent {

    private Long id;
    private Long orderId;
    private int price;
    private int count;
    private String custNm;
    private String cafeNm;
    private String paymentStatus;

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

    public void setPrice(int price){
        this.price = price;
    }

    public int getPrice(){
        return price;
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


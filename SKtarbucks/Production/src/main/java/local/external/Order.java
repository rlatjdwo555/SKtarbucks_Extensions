package local.external;

public class Order {

    private Long id;
    private Long cafeId;
    private String custNm;
    private String status;
    private String cafeNm;
    private String orderType;

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

}

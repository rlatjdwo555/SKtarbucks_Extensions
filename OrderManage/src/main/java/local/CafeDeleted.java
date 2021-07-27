
package local;

public class CafeDeleted extends AbstractEvent {

    private Long id;
    private String cafeNm;
    private Long stock;
    private int price;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getCafeNm() {
        return cafeNm;
    }

    public void setCafeNm(String cafeNm) {
        this.cafeNm = cafeNm;
    }

    public void setStock(Long stock){
        this.stock = stock;
    }

    public Long getStock(){
        return stock;
    }

    public void setPrice(int price){
        this.price = price;
    }

    public int getPrice(){
        return price;
    }

}

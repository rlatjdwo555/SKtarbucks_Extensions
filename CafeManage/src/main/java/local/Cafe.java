package local;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Cafe_table")
public class Cafe {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String cafeNm;
    private Long stock;
    private int price;
    
    @PostPersist
    public void onPostPersist(){
        CafeRegistered cafeRegistered = new CafeRegistered();
        BeanUtils.copyProperties(this, cafeRegistered);
        cafeRegistered.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){
        CafeChanged cafeChanged = new CafeChanged();
        BeanUtils.copyProperties(this, cafeChanged);
        cafeChanged.publishAfterCommit();
    }


    @PreRemove
    public void onPreRemove(){
        CafeDeleted cafeDeleted = new CafeDeleted();
        BeanUtils.copyProperties(this, cafeDeleted);
        cafeDeleted.publishAfterCommit();

    }

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

package local;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Push_table")
public class Push {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private String msg;

    @PostPersist
    public void onPostPersist(){
        PushRegistered pushRegistered = new PushRegistered();
        BeanUtils.copyProperties(this, pushRegistered);
        pushRegistered.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate(){
        PushSent pushSent = new PushSent();
        BeanUtils.copyProperties(this, pushSent);
        pushSent.publishAfterCommit();
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

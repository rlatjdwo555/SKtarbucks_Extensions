package local;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

public class PushRegistered extends AbstractEvent {

    private Long id;
    private Long orderId;
    private String msg;

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

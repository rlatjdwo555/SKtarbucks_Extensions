
package local.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="OrderManage", url="${api.order.url}")
public interface OrderService {

    @RequestMapping(method= RequestMethod.GET, value="/orders/{orderId}", consumes = "application/json")
    public Order getOrderType(@PathVariable("orderId") Long orderId);

}
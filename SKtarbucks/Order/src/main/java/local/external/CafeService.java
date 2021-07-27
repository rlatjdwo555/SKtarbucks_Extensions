package local.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="CafeManage", url="${api.cafe.url}")
public interface CafeService {

    @RequestMapping(method= RequestMethod.GET, value="/cafes/{cafeId}", consumes = "application/json")
    public Cafe getCafeStatus(@PathVariable("cafeId") Long cafeId);

}
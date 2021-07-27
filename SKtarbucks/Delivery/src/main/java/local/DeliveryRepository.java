package local;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface DeliveryRepository extends PagingAndSortingRepository<Delivery, Long>{
    
}
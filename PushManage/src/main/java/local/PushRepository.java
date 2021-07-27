package local;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PushRepository extends PagingAndSortingRepository<Push, Long>{
    
}
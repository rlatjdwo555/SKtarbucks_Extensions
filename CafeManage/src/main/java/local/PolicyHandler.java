package local;

import local.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PolicyHandler{

    @Autowired
    CafeRepository cafeRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCanceled_OrderCanceled(@Payload Canceled canceled){

        if(canceled.isMe()){
            System.out.println("##### 주문 취소 요청으로 인한 수량 변화 : " + canceled.toJson());
            Optional<Cafe> temp = cafeRepository.findById(canceled.getCafeId());

            if(temp.isPresent()){
                Cafe a = temp.get();
                a.setStock(a.getStock()+canceled.getCount());
                cafeRepository.save(a);
            }
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRequested_OrderRequested(@Payload Requested requested){

        if(requested.isMe()){
            System.out.println("##### 주문 요청으로 인한 수량 변화 : " + requested.toJson());
            Optional<Cafe> temp = cafeRepository.findById(requested.getCafeId());

            if(temp.isPresent()){
                Cafe a = temp.get();
                a.setStock(a.getStock()-requested.getCount());
                cafeRepository.save(a);
            }
        }
    }
}

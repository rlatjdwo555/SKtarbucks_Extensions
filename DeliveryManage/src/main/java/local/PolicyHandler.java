package local;

import local.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{

    @Autowired
    DeliveryRepository deliveryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverProductionChanged_CreateDelivery(@Payload ProductionSent productionSent){

        if(!productionSent.isMe()) return;
        
        System.out.println("##### listener ProductionSent: " + productionSent.toJson());
            
        Delivery delivery = new Delivery();
        delivery.setOrderId(productionSent.getOrderId());
        delivery.setCustNm(productionSent.getCustNm());
        delivery.setCafeNm(productionSent.getCafeNm());
        delivery.setStatus("PROGRESS");
        deliveryRepository.save(delivery);
    }
}

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
    PushRepository pushRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCanceled_Push(@Payload Canceled canceled){

        if(!canceled.isMe()) return;
        
        System.out.println("##### listener Canceled: " + canceled.toJson());
            
        Push push = new Push();
        push.setOrderId(canceled.getId());
        push.setMsg("주문하신 음료가 취소되었습니다.");
        pushRepository.save(push);
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryStarted_Push(@Payload DeliveryStarted deliveryStarted){

        if(!deliveryStarted.isMe()) return;
        
        System.out.println("##### listener DeliveryStarted: " + deliveryStarted.toJson());
            
        Push push = new Push();
        push.setOrderId(deliveryStarted.getOrderId());
        push.setMsg("주문하신 음료의 배달이 시작되었습니다.");
        pushRepository.save(push);
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryCompleted_Push(@Payload DeliveryCompleted deliveryCompleted){

        if(!deliveryCompleted.isMe()) return;
        
        System.out.println("##### listener DeliveryCompleted: " + deliveryCompleted.toJson());
            
        Push push = new Push();
        push.setOrderId(deliveryCompleted.getOrderId());
        push.setMsg("주문하신 음료의 배달이 완료되었습니다.");
        pushRepository.save(push);
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverProductionChanged_Push(@Payload ProductionChanged productionChanged){

        if(!productionChanged.isMe()) return;
        
        System.out.println("##### listener ProductionChanged: " + productionChanged.toJson());
            
        Push push = new Push();
        push.setOrderId(productionChanged.getOrderId());

        switch (productionChanged.getStatus()){
            case "MAKING":
             push.setMsg("주문하신 음료를 만들고 있습니다.");
             break;

            case "COMPLETED":
             push.setMsg("주문하신 음료가 준비되었습니다.");
             break;
            
            default :
        }

        pushRepository.save(push);
    }
}

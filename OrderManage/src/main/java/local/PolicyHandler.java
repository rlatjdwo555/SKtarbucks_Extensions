package local;

import local.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PolicyHandler{

    @Autowired
    OrderRepository orderRepository;
    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCafeDeleted_ForceCancel(@Payload CafeDeleted cafeDeleted){

        if(cafeDeleted.isMe()){
            System.out.println("##### listener ForceCancel : " + cafeDeleted.toJson());
            List<Order> list = orderRepository.findByCafeId(cafeDeleted.getId());
            for(Order temp : list){
                // 본인이 취소한건은 제외
                if(!"CANCELED".equals(temp.getStatus())) {
                    temp.setStatus("FORCE_CANCELED");
                    orderRepository.save(temp);
                }
            }
        }
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverProductionCompleted_ChangeOrderStatus(@Payload ProductionChanged productionChanged){

        if(!productionChanged.isMe()) return;
        
        System.out.println("##### listener ProductionChanged : " + productionChanged.toJson());

        orderRepository.findById(productionChanged.getOrderId())
        .ifPresent(
            order->{
                order.setStatus(productionChanged.getStatus());
                orderRepository.save(order);
            }
        ); 
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentCancelled_CanceledOrder(@Payload PaymentCancelled paymentCancelled){

        if(!paymentCancelled.isMe()) return;
        
        System.out.println("##### listener PaymentCancelled : " + paymentCancelled.toJson());

        orderRepository.findById(paymentCancelled.getOrderId())
        .ifPresent(
            order->{
                order.setStatus("CANCELED");
                orderRepository.save(order);
            }
        );     
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryStarted_ChangeOrderStatus(@Payload DeliveryStarted deliveryStarted){

        if(!deliveryStarted.isMe()) return;
        
        System.out.println("##### listener DeliveryStarted : " + deliveryStarted.toJson());

        orderRepository.findById(deliveryStarted.getOrderId())
        .ifPresent(
            order->{
                order.setStatus("DELIVERY_IN_PROGRESS");
                orderRepository.save(order);
            }
        ); 
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryCompleted_ChangeOrderStatus(@Payload DeliveryCompleted deliveryCompleted){

        if(!deliveryCompleted.isMe()) return;
        
        System.out.println("##### listener DeliveryCompleted : " + deliveryCompleted.toJson());

        orderRepository.findById(deliveryCompleted.getOrderId())
        .ifPresent(
            order->{
                order.setStatus("DELIVERY_COMPLETED");
                orderRepository.save(order);
            }
        ); 
    }
}

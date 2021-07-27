package local;

import local.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PolicyHandler{

    @Autowired
    ProductionRepository productionRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentApproved_ProductionComplete(@Payload PaymentApproved paymentApproved){

        if(!paymentApproved.isMe()) return;
        
        // 결제 승인으로 인한 제조 확정
        System.out.println("##### listener PaymentApproved: " + paymentApproved.toJson());
            
        Production production = new Production();
        production.setOrderId(paymentApproved.getOrderId());
        production.setCustNm(paymentApproved.getCustNm());
        production.setCafeNm(paymentApproved.getCafeNm());
        production.setCount(paymentApproved.getCount());
        production.setStatus("MAKING");
        productionRepository.save(production);
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCanceled_ProductionCancel(@Payload Canceled canceled){

        if(canceled.isMe()){
            //  주문 취소로 인한 취소
            Production temp = productionRepository.findByOrderId(canceled.getId());
            temp.setStatus("CANCELED");
            productionRepository.save(temp);

        }
    }

}

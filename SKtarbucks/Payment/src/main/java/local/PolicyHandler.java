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
    @Autowired PaymentRepository paymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderRequested_PaymentRequestPolicy(@Payload Requested requested){

        if(!requested.validate()) return;

        System.out.println("\n\n##### listener PaymentRequestPolicy : " + requested.toJson() + "\n\n");

        Payment payment = new Payment();
        payment.setOrderId(requested.getId());
        payment.setCafeNm(requested.getCafeNm());
        payment.setCustNm(requested.getCustNm());
        payment.setPrice(requested.getPrice());
        payment.setCount(requested.getCount());
        payment.setPaymentStatus("PAYMENT_REQUESTED");
        paymentRepository.save(payment);
        
    }

    // 예약취소로 인한 결제취소
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCanceled_PaymentCancelPolicy(@Payload Canceled canceled){

        if(!canceled.validate()) return;  

        System.out.println("\n\n##### listener PaymentCancelPolicy : " + canceled.toJson() + "\n\n");

        if("CANCELED".equals(canceled.getStatus())){
            Payment payment = paymentRepository.findByOrderId(canceled.getId());
            payment.setPaymentStatus("PAYMENT_CANCELED");
            paymentRepository.save(payment);
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}

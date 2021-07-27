package local;

import local.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class MyPageViewHandler {


    @Autowired
    private MyPageRepository myPageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenRequested_then_CREATE_1 (@Payload Requested requested) {
        try {
            if (requested.isMe()) {
                // view 객체 생성
                MyPage myPage = new MyPage();
                // view 객체에 이벤트의 Value 를 set 함
                myPage.setOrderId(requested.getId());
                myPage.setCustNm(requested.getCustNm());
                myPage.setCafeNm(requested.getCafeNm());
                myPage.setStatus(requested.getStatus());
                myPage.setCount(requested.getCount());
                myPage.setOrderType(requested.getOrderType());
                // view 레파지토리에 save
                myPageRepository.save(myPage);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenCanceled_then_UPDATE_1(@Payload Canceled canceled) {
        try {
            if (canceled.isMe()) {
                // view 객체 조회
                List<MyPage> myPageList = myPageRepository.findByOrderId(canceled.getId());
                for(MyPage myPage : myPageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    myPage.setStatus(canceled.getStatus());                   
                    // view 레파지 토리에 save
                    myPageRepository.save(myPage);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void whenProductionChanged_then_UPDATE_2(@Payload ProductionChanged productionChanged) {
        try {
            if (productionChanged.isMe()) {
      
                List<MyPage> myPageList = myPageRepository.findByOrderId(productionChanged.getOrderId());

                for (MyPage myPage : myPageList) {
                    myPage.setStatus(productionChanged.getStatus());                 
                    myPageRepository.save(myPage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenDeliveryStarted_then_UPDATE_3(@Payload DeliveryStarted deliveryStarted) {
        try {
            if (deliveryStarted.isMe()) {

                List<MyPage> myPageList = myPageRepository.findByOrderId(deliveryStarted.getOrderId());

                for (MyPage myPage : myPageList) {
                    myPage.setStatus("DELIVERY_IN_PROGRESS");
                    myPageRepository.save(myPage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenDeliveryCompleted_then_UPDATE_4(@Payload DeliveryCompleted deliveryCompleted) {
        try {
            if (deliveryCompleted.isMe()) {

                List<MyPage> myPageList = myPageRepository.findByOrderId(deliveryCompleted.getOrderId());

                for (MyPage myPage : myPageList) {
                    myPage.setStatus("DELIVERY_COMPLETED");
                    myPageRepository.save(myPage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentCancelled_then_UPDATE_5(@Payload PaymentCancelled paymentCancelled) {
        try {
            if (paymentCancelled.isMe()) {

                List<MyPage> myPageList = myPageRepository.findByOrderId(paymentCancelled.getOrderId());

                for(MyPage myPage : myPageList){             
                    myPage.setStatus("PAYMENT_CANCELED");                   
                    myPageRepository.save(myPage);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
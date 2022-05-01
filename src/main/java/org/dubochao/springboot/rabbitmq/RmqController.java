package org.dubochao.springboot.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class RmqController {

    @Autowired
    private ConnectionFactory connectionFactory;
    @Autowired
    private RmqConfiguration rmqConfiguration;
    @Autowired(required = false)
    private RmqMessageConsumer rmqMessageConsumer;
    @Autowired(required = false)
    private RmqMessageProducer rmqMessageProducer;

    @GetMapping("/deleteAllQueue")
    public Map<String, String> deleteAllQueue(){
        Map<String, String> resultMap = new HashMap<>();
        try {
            stopProducer();
            stopConsumer();
            TimeUnit.SECONDS.sleep(3);
            for (int i = 1; i <= rmqConfiguration.getQueueCount(); i++) {
                Connection connection = connectionFactory.newConnection();
                Channel channel = connection.createChannel();
                channel.confirmSelect();
                String queueName = rmqConfiguration.getQueueNamePrefix() + i;
                AMQP.Queue.DeleteOk deleteOk = channel.queueDelete(queueName);
                channel.close();
                connection.close();
                log.info("成功删除：queueName={} messageCount={}", queueName, deleteOk.getMessageCount());
                resultMap.put(queueName, deleteOk.toString());
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return resultMap;
    }

    @GetMapping("/startProducer")
    public boolean startProducer(){
        try {
            if (rmqMessageProducer != null) {
                rmqMessageProducer.afterPropertiesSet();
                return true;
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return false;
    }

    @GetMapping("/stopProducer")
    public boolean stopProducer(){
        try {
            if (rmqMessageProducer != null) {
                rmqMessageProducer.destroy();
                return true;
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return false;
    }

    @GetMapping("/startConsumer")
    public boolean startConsumer(){
        try {
            if (rmqMessageConsumer != null) {
                rmqMessageConsumer.afterPropertiesSet();
                return true;
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return false;
    }

    @GetMapping("/stopConsumer")
    public boolean stopConsumer(){
        try {
            if (rmqMessageConsumer != null) {
                rmqMessageConsumer.destroy();
                return true;
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return false;
    }
}

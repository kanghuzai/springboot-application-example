package org.dubochao.springboot.rabbitmq;

import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@Primary
@Slf4j
@ConditionalOnExpression("#{!T(java.lang.System).getProperty('os.name').toLowerCase().startsWith('mac')}")
public class RmqMessageConsumer implements InitializingBean, DisposableBean {

    @Autowired
    private RabbitProperties rabbitProperties;
    @Autowired
    private ConnectionFactory connectionFactory;
    @Autowired
    private RmqConfiguration rmqConfiguration;

    private List<Connection> connectionList = new ArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        int y = 1;
        while (y <= rmqConfiguration.getQueueCount()) {
            int i = 1;
            while (i <= rmqConfiguration.getConsumerCount()) {
                Connection connection = consumer(rmqConfiguration.getQueueNamePrefix() + y, i);
                if(connection != null){
                    connectionList.add(connection);
                }
                i++;
            }
            y++;
        }
    }

    @Override
    public void destroy() throws Exception {
        Thread.getAllStackTraces().keySet().forEach(thread -> {
            if (thread.getName().startsWith("rmp-consumer-message")) {
                thread.interrupt();
            }
        });
        connectionList.forEach(connection -> {
            try {
                if (connection != null && connection.isOpen()) {
                    connection.close();
                }
            }catch (Exception e1){
                log.error(e1.getMessage(), e1);
            }
        });
        connectionList.clear();
    }

    public Connection consumer(String queueName, int i) {
        try {
            Connection connection = connectionFactory.newConnection("rmp-consumer-connection-" + i);
            connection.addShutdownListener((cause) -> log.error(cause.getMessage(), cause));
            connection.addBlockedListener(new BlockedListener() {
                @Override
                public void handleUnblocked() throws IOException {
                    log.info("[阻塞恢复] 当前连接已从阻塞中恢复.");
                }
                @Override
                public void handleBlocked(String reason) throws IOException {
                    log.error("[连接阻塞] 当前连接已被阻塞. reason={}", reason);
                }
            });
            Channel channel = connection.createChannel();
            channel.basicQos(rmqConfiguration.getConsumerPrefetch());
            channel.confirmSelect();
            channel.queueDeclare(queueName, true, false, false, new HashMap<>());
            channel.queueBind(queueName, "network.test.exchange", "network.test.routingKey");
            channel.basicConsume(queueName, false, queueName + "@" + i, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    Thread.currentThread().setName("rmp-consumer-message" + i);
                    try {
                        log.info("{} --> {}", consumerTag, new String(body));
                        int consumerWorkTime = rmqConfiguration.getConsumerWorkTime();
                        int random = new Random().nextInt(consumerWorkTime);
                        if(random == consumerWorkTime){
                            new RuntimeException("random等于" + consumerWorkTime + "异常");
                        }
                        TimeUnit.MILLISECONDS.sleep(random);
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (InterruptedException e) {
                        log.info("{}消费者线程中断退出", consumerTag);
                    }catch (Exception exception){
                        log.info(exception.getMessage(), exception);
                        channel.basicNack(envelope.getDeliveryTag(), false, true);
                    }
                }
            });
            return connection;
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return null;
    }
}

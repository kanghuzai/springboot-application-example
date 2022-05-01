package org.dubochao.springboot.rabbitmq;

import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Primary
@Slf4j
@ConditionalOnExpression("#{!T(java.lang.System).getProperty('os.name').toLowerCase().startsWith('mac')}")
public class RmqMessageProducer implements InitializingBean, DisposableBean {

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private ConnectionFactory connectionFactory;
    @Autowired
    private RmqConfiguration rmqConfiguration;

    private List<Thread> threadList = new ArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        int i = 1;
        while ( i <= rmqConfiguration.getProducerCount()) {
            Thread slaveThread = new Thread(new SlaveProducer(i));
            slaveThread.setName("rmq-producer-message-" + i);
            slaveThread.start();
            threadList.add(slaveThread);
            i++;
        }
    }

    @Override
    public void destroy() throws Exception {
        threadList.forEach(thread -> {
            if (thread.isAlive()) {
                thread.interrupt();
            }
        });
        threadList.clear();
    }

    class SlaveProducer implements Runnable{

        private int i;
        public SlaveProducer(int i){
            this.i = i;
        }
        @Override
        public void run() {
            Connection connection = null;
            Channel channel = null;
            try {
                connection = connectionFactory.newConnection("rmp-producer-connection-" + i);
                connection.addShutdownListener((ShutdownSignalException e) -> log.error(e.getMessage(), e));
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
                channel = connection.createChannel();
                channel.exchangeDeclare("network.test.exchange", BuiltinExchangeType.DIRECT);
                channel.confirmSelect();
                while (true) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(new Random().nextInt(rmqConfiguration.getProducerWorkTime()));
                        String msgBody = Thread.currentThread().getName() + "@" + UUID.randomUUID().toString().replace("-", "");
                        channel.basicPublish("network.test.exchange", "network.test.routingKey", new AMQP.BasicProperties(), msgBody.getBytes());
                        log.info(msgBody);
                    }catch (InterruptedException ie) {
                        try {
                            if (connection != null && connection.isOpen()) {
                                connection.close();
                            }
                        } catch (Exception e1) {
                            log.error(e1.getMessage(), e1);
                        }
                        log.info("发送者salve线程中断退出");
                        return;
                    }catch (Exception e){
                        log.error(e.getMessage(), e);
                    }
                }
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }
        }
    }
}

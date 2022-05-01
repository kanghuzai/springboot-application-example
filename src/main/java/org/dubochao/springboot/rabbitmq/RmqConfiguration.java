package org.dubochao.springboot.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class RmqConfiguration {

    @Bean
    public ConnectionFactory connectionFactory(RabbitProperties rabbitProperties){
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitProperties.getHost());
        connectionFactory.setPort(rabbitProperties.getPort());
        connectionFactory.setUsername(rabbitProperties.getUsername());
        connectionFactory.setPassword(rabbitProperties.getPassword());
        connectionFactory.setVirtualHost(rabbitProperties.getVirtualHost());
        if (rabbitProperties.getConnectionTimeout() != null) {
            connectionFactory.setConnectionTimeout((int) rabbitProperties.getConnectionTimeout().getSeconds());  // default 60s
        }
        if (rabbitProperties.getRequestedHeartbeat() != null) {
            connectionFactory.setRequestedHeartbeat((int) rabbitProperties.getRequestedHeartbeat().getSeconds());  // default 60s
        }
        connectionFactory.setAutomaticRecoveryEnabled(true);
        return connectionFactory;
    }

    @Value("${rabbitmq.queue.count:50}")
    private int queueCount;
    @Value("${rabbitmq.producer.count:10}")
    private int producerCount;
    @Value("${rabbitmq.consumer.count:${rabbitmq.producer.count:10}}")
    private int consumerCount;
    @Value("${rabbitmq.consumer.prefetch:10}")
    private int consumerPrefetch;
    @Value("${rabbitmq.producer.work-millisecond:1000}")
    private int producerWorkTime;
    @Value("${rabbitmq.consumer.work-millisecond:${rabbitmq.producer.work-millisecond:600}}")
    private int consumerWorkTime;
    @Value("${rabbitmq.queue.name-prefix:dubochao.network.test.queue}")
    private String queueNamePrefix;


}

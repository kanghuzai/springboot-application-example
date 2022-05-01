# springboot-application-example
springboot常用功能使用示例

## rabbitmq-module使用

| VM参数                                | 默认值                         | 含义       |
|-------------------------------------|-----------------------------|----------|
| spring.rabbitmq.requested-heartbeat | 60s                         | 心跳间隔时间   |
| spring.rabbitmq.connection-timeout  | 60s                         | 链接超时时间   |
| rabbitmq.producer.count             | 10                          | 模拟生产者数   |
| rabbitmq.consumer.count             | 10                          | 模拟消费者数   |
| rabbitmq.queue.count                | 50                          | 模拟队列数    |
| server.port                         | 8080                        | http端口   |
| rabbitmq.producer.work-millisecond  | 1000                        | 生产者处理时间  |
| rabbitmq.consumer.work-millisecond  | 900                         | 生产者处理时间  |
| rabbitmq.queue.name-prefix          | dubochao.network.test.queue | 网络模拟测试队列 |

```shell
# 带自定义参数和日志输出模式运行
nohup java -jar springboot-application-example-1.0-SNAPSHOT.jar -Dserver.port=8088 > applicaton.log 2>&1 &

# 有日志输出模式运行
nohup java -jar springboot-application-example-1.0-SNAPSHOT.jar > applicaton.log 2>&1 &

# 无日志输出模式运行
nohup java -jar springboot-application-example-1.0-SNAPSHOT.jar > /dev/null 2>&1 &

# 开启模拟应用与rmq-server的网络故障
nohup sh network_mock.sh 10000 192.9.251.216 > network.log 2>&1 &

# 关闭应用
kill -9 `jps -l | grep springboot-application | awk '{print $1}'`

# 删除所有测试队列(同时会关闭所有生产者和消费者，仅限于单节点生产消费者模式)
curl http://127.0.0.1:8080/deleteAllQueue

# 关闭所有消费者
curl http://127.0.0.1:8080/stopConsumer

# 关闭所有生产者
curl http://127.0.0.1:8080/stopProducer

# 启动所有消费者
curl http://127.0.0.1:8080/startConsumer

# 启动所有生产者
curl http://127.0.0.1:8080/startProducer
```





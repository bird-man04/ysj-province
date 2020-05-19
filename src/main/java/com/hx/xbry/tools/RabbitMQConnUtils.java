package com.hx.xbry.tools;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @ClassName RabbitMQConnUtil
 * @Description rabbitMQ 工具类
 * @Author fmy
 * @Date 2019/10/22 22:00
 * @Version 1.0
 */
public class RabbitMQConnUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQConnUtils.class);
    private static ConnectionFactory factory;
    private static Connection connection;

    /**
     * @Description 消费MQ
     * @Author fmy
     * @Date 2020/3/4 18:06
     * @Param [channel, queue, autoAck, consumer]
     * @Return void
     **/
    public static void basicConsume(Channel channel, String queue, boolean autoAck, Consumer consumer) throws IOException {
        channel.basicConsume(queue, autoAck, consumer);
    }

    /**
     * @Description fanout MQ类型消息
     * @Author fmy
     * @Date 2020/3/11 11:00
     * @Param [exchange, queue, body, header]
     * @Return void
     **/
    public static void send(String exchange, byte[] body, Map<String, Object> header) {
        try {
            Channel channel = getChannel();
            channel.exchangeDeclare(exchange, "fanout", true, false, null);
            basicPublish(channel, exchange, "", createBasicProperties(header), body);
        } catch (Exception e) {
            LOGGER.error("", e);
            //fix 如果发送失败，尝试重新获取MQ连接
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException ex) {
                    LOGGER.info("", ex);
                }
                connection = null;
            }
            factory = null;
        }
    }

    /**
     * @Description 生产MQ
     * @Author fmy
     * @Date 2019/12/5 16:59
     * @Param [exchange, queue, routingKey, body]
     * @Return void
     **/
    public static void send(String exchange, String queue, String routingKey, byte[] body, Map<String, Object> header) {
        try {
            Channel channel = getChannel();
            channel.exchangeDeclare(exchange, "topic", true, false, null);
            channel.queueDeclare(queue, true, false, false, null);
            channel.queueBind(queue, exchange, routingKey);
            basicPublish(channel, exchange, routingKey, createBasicProperties(header), body);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    /**
     * @Description 发布消息
     * @Author fmy
     * @Date 2019/10/23 15:08
     * @Param [channel, exchange, routingKey, basicProperties, body]
     * @Return void
     **/
    private static void basicPublish(Channel channel, String exchange, String routingKey, AMQP.BasicProperties basicProperties, byte[] body) throws IOException {
        channel.basicPublish(exchange, routingKey, basicProperties, body);
        LOGGER.info("MQ发送成功...");
    }

    /**
     * @Description 创建BasicProperties
     * @Author fmy
     * @Date 2019/10/23 15:06
     * @Param []
     * @Return com.rabbitmq.client.AMQP.BasicProperties
     **/
    private static AMQP.BasicProperties createBasicProperties(Map<String, Object> header) {
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        builder.headers(header);
        builder.deliveryMode(2);
        builder.expiration(String.valueOf(7 * 24 * 60 * 60 * 1000));//默认保留一个周
        return builder.build();
    }

    /**
     * @Description 获取channel
     * @Author fmy
     * @Date 2019/10/23 15:08
     * @Param []
     * @Return com.rabbitmq.client.Channel
     **/
    public static Channel getChannel() throws IOException, TimeoutException {
        return getConnection().createChannel();
    }

    /**
     * @Description 获取连接
     * @Author fmy
     * @Date 2019/10/23 15:08
     * @Param []
     * @Return com.rabbitmq.client.Connection
     **/
    private static Connection getConnection() throws IOException, TimeoutException {
        if (connection == null || !connection.isOpen()) {
            connection = getConnectionFactory().newConnection();
        }
        return connection;
    }

    /**
     * @Description 获取连接工厂
     * @Author fmy
     * @Date 2019/10/23 15:09
     * @Param []
     * @Return com.rabbitmq.client.ConnectionFactory
     **/
    private static ConnectionFactory getConnectionFactory() {
        if (factory == null) {
            factory = new ConnectionFactory();
            factory.setHost(PropertiesUtils.getProperty("RABBIT.HOST"));
            factory.setPort(Integer.parseInt(PropertiesUtils.getProperty("RABBIT.PORT", "5672").trim()));
            factory.setUsername(PropertiesUtils.getProperty("RABBIT.USER"));
            factory.setPassword(PropertiesUtils.getProperty("RABBIT.PWD"));
            LOGGER.info("RabbitMQ 连接信息:" );
            LOGGER.info(PropertiesUtils.getProperty("RABBIT.HOST") );
            LOGGER.info(PropertiesUtils.getProperty("RABBIT.PORT") );
            LOGGER.info(PropertiesUtils.getProperty("RABBIT.USER") );
            LOGGER.info(PropertiesUtils.getProperty("RABBIT.PWD") );
        }
        return factory;
    }
}

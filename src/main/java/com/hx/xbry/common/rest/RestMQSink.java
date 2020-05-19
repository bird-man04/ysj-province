package com.hx.xbry.common.rest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hx.xbry.tools.CommonUtils;
import com.hx.xbry.tools.RabbitMQConnUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * @ClassName RestMQSink
 * @Description 发送MQ方式Sink
 * @Author fmy
 * @Date 2020/04/10 16:53
 * @Version 1.0
 */
public class RestMQSink extends AbstractSink implements Configurable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestMQSink.class);

    private static boolean isSendMQ;
    private static String exchange;

    @Override
    public void configure(Context context) {
        isSendMQ = context.getBoolean("send.mq", true);
        exchange = context.getString("exchange");
    }

    @Override
    public Status process() {
        Status status = Status.READY;
        Transaction transaction = getTransaction();// 必须开启事务
        try {
            transaction.begin();
            Event event = getChannel().take();
            if (event == null) {
                status = Status.BACKOFF;
            } else {
                JSONArray ja = JSONArray.parseArray(new String(event.getBody(), StandardCharsets.UTF_8));
                if (ja.size() > 0) {
                    ja = CommonUtils.sortByProperty(ja, true);
                    for (int i = 0; i < ja.size(); i++) {
                        JSONObject resultJO = ja.getJSONObject(i);
                        if (isSendMQ) {
                            RabbitMQConnUtils.send(exchange, resultJO.toJSONString().getBytes(StandardCharsets.UTF_8), new HashMap<>());
                        }
                    }
                }
            }
            transaction.commit();
        } catch (Exception e) {
            LOGGER.error("", e);
            transaction.rollback();
            status = Status.BACKOFF;
        } finally {
            transaction.close();
        }
        return status;
    }

    private Transaction getTransaction() {
        return getChannel().getTransaction();
    }

}

package com.hx.xbry.tools;

import com.hx.xbry.FlumeProcess;
import org.apache.flume.Channel;
import org.apache.flume.Event;
import org.apache.flume.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.flume.Sink.Status;

/**
 * @ClassName FlumeUtils
 * @Description
 * @Author fmy
 * @Date 2020/5/11 10:30
 * @Version 1.0
 */
public class FlumeUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlumeUtils.class);

    public static Status process(Channel channel, FlumeProcess flumeProcess) {
        Status status = Status.READY;
        Transaction transaction = channel.getTransaction();
        try {
            transaction.begin();
            Event event = channel.take();
            if (event == null) {
                status = Status.BACKOFF;
            } else {
                flumeProcess.process(event);
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            status = Status.BACKOFF;
            LOGGER.error("处理event消息", e);
        } finally {
            transaction.close();
        }
        return status;
    }
}

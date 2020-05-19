package com.hx.xbry.qh.iot;

import com.hx.xbry.qh.iot.runable.AIOSRunnable;
import com.hx.xbry.qh.iot.runable.AMTIRunnable;
import com.hx.xbry.qh.iot.runable.DSOIRunnable;
import com.hx.xbry.qh.iot.runable.GSRIRunnable;
import org.apache.flume.Context;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.conf.Configurable;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName IOTSource
 * @Description 青海物联网增量数据采集source
 * @Author fmy
 * @Date 2019/11/27 11:49
 * @Version 1.0
 */
public class IotSource extends AbstractSource implements EventDrivenSource, Configurable {

    private static final Logger LOGGER = LoggerFactory.getLogger(IotSource.class);

    @Override
    public void configure(Context context) {

    }

    @Override
    public synchronized void start() {
        ChannelProcessor channelProcessor = getChannelProcessor();
        ExecutorService es = Executors.newFixedThreadPool(SubBizType.values().length);
        es.submit(new AIOSRunnable(channelProcessor));
        es.submit(new AMTIRunnable(channelProcessor));
        es.submit(new DSOIRunnable(channelProcessor));
        es.submit(new GSRIRunnable(channelProcessor));
        LOGGER.info("物联网采集程序已启动...");
    }

    @Override
    public synchronized void stop() {
        super.stop();
        LOGGER.info("物联网采集程序已停止...");
    }

}

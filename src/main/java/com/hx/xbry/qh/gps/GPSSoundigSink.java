package com.hx.xbry.qh.gps;

import org.apache.flume.Context;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName YdpSink
 * @Description 青海GPS移动探空资料采集Sink
 * @Author fmy
 * @Date 2019/12/23 15:53
 * @Version 1.0
 */
public class GPSSoundigSink extends AbstractSink implements Configurable {

    @Override
    public void configure(Context context) {

    }

    @Override
    public Status process() {
        try {
            TimeUnit.DAYS.sleep(365);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Status.READY;
    }

}

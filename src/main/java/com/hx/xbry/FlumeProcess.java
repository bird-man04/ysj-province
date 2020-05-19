package com.hx.xbry;

import org.apache.flume.Event;

/**
 * @ClassName Process
 * @Description Event消息处理接口
 * @Author fmy
 * @Date 2020/5/11 10:14
 * @Version 1.0
 */
@FunctionalInterface
public interface FlumeProcess {

    void process(Event event);
}

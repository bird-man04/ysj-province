package com.hx.xbry.tools;

import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.event.SimpleEvent;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.hx.xbry.bean.Constants.SDF14;

/**
 * @ClassName SourceUtils
 * @Description
 * @Author fmy
 * @Date 2019/11/26 15:23
 * @Version 1.0
 */
public class SourceUtils {

    public static void sendEvent(String body, Map<String, String> header, ChannelProcessor channelProcessor) {
        SimpleEvent event = new SimpleEvent();
        event.setBody(body.getBytes(StandardCharsets.UTF_8));
        event.setHeaders(header);
        channelProcessor.processEvent(event);
    }

    public static Map<String, String> createHeader(Date beginTime) {
        Map<String,String> map  = new HashMap<>();
        map.put("BEGIN_TIME",SDF14.format(beginTime));
        return map;
    }

    public static Map<String, String> createHeader(Date beginTime,String dataType) {
        Map<String,String> map  = new HashMap<>();
        map.put("BEGIN_TIME",SDF14.format(beginTime));
        map.put("DATA_TYPE",dataType);
        return map;
    }

    public static Map<String, String> createHeader() {
        return new HashMap<>();
    }
}

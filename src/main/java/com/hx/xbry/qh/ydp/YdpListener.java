package com.hx.xbry.qh.ydp;

import com.hx.xbry.tools.IOUtils;
import com.hx.xbry.tools.TimeUtils;
import net.contentobjects.jnotify.JNotifyListener;
import org.apache.flume.Event;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.event.SimpleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.hx.xbry.bean.Constants.SDF14;

/**
 * @ClassName YdpListener
 * @Description 青海雨滴谱仪资料采集Listener
 * @Author fmy
 * @Date 2019/12/16 15:01
 * @Version 1.0
 */
public class YdpListener implements JNotifyListener {

    public static final Logger LOGGER = LoggerFactory.getLogger(YdpListener.class);
    private static ConcurrentHashMap<String, Long> sizeMap; //需要持久化记录，不然会多发(通过判断小文件是否存在进行多发）
    private ChannelProcessor channelProcessor;

    static {
        YdpListener.sizeMap = new ConcurrentHashMap<>();
    }

    private static Long getSize(String fileName) {
        return sizeMap.get(fileName);
    }

    private static void setSize(String fileName, long size) {
        sizeMap.put(fileName, size);
    }

    YdpListener(ChannelProcessor channelProcessor) {
        this.channelProcessor = channelProcessor;
    }

    @Override
    public void fileCreated(int i, String s, String s1) {//fixme 会同时创建8分钟以前的文件，跟踪是否内容有改变(问一下张老师）
        File file = new File(s + File.separator + s1);
        if (!file.isFile()) {
            return;
        }
        if (file.getName().startsWith("P_") && file.getName().endsWith(".txt")) { //谱图数据
            try {
                String content = IOUtils.read(file, 0).trim();
                if (!content.isEmpty()) {
                    YdpUtils.sendInDI(YdpUtils.createDIJsonObject("putu", new Date().getTime(), file));
                    channelProcessor.processEvent(createEvent(file, content));
                }
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        } else if (file.getName().startsWith("UB_") && file.getName().endsWith(".txt")) { //原始数据
            try {
                String content = IOUtils.read(file, 0).trim();
                if (!content.isEmpty()) {
                    setSize(file.getName(), content.length());
                    YdpUtils.sendInDI(YdpUtils.createDIJsonObject("original", new Date().getTime(), file));//发送接入DI
                    channelProcessor.processEvent(createEvent(file, content));
                }
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        }
    }

    @Override
    public void fileModified(int i, String s, String s1) {
        File file = new File(s + File.separator + s1);
        if (!file.isFile()) {
            return;
        }
        if (file.getName().startsWith("UB_") && file.getName().endsWith(".txt")) {// 原始数据
            Long size = getSize(file.getName());
            if (size == null) {
                setSize(file.getName(), 0);
                size = 0L;
            }
            try {
                //fix 原始数据文件一开始生成大小就为200kb，大小是没有变化的，需要把空格去掉 fmy 2019-12-17
                String content = IOUtils.read(file, size).trim();
                if (content.length() == 0) {
                    return;
                }
                setSize(file.getName(), size + content.length());
                channelProcessor.processEvent(createEvent(file, content));
                YdpUtils.sendInDI(YdpUtils.createDIJsonObject("original", new Date().getTime(), file));//发送接入DI
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        }
    }

    /**
     * @Description 创建event
     * @Author fmy
     * @Date 2019/12/16 15:55
     * @Param [file, size]
     * @Return org.apache.flume.Event
     **/
    private Event createEvent(File file, String content) {
        Event event = new SimpleEvent();
        Map<String, String> header = new HashMap<>();
        header.put("fileName", file.getName());
        header.put("absPath", file.getAbsolutePath());
        header.put("beginTime", SDF14.format(TimeUtils.beijing2world(new Date())));
        event.setHeaders(header);
        event.setBody(content.getBytes(StandardCharsets.UTF_8));
        return event;
    }

    @Override
    public void fileRenamed(int i, String s, String s1, String s2) {
        LOGGER.info(s + File.separator + s1 + " Renamed");
    }

    @Override
    public void fileDeleted(int i, String s, String s1) {
        LOGGER.info(s + File.separator + s1 + " Deleted");
    }
}

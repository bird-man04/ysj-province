package com.hx.xbry.qh.ydp;

import com.hx.xbry.setting.DllLoad;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import org.apache.flume.Context;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.hx.xbry.bean.Constants.MASK_CODE;

/**
 * @ClassName YdpSource
 * @Description 青海雨滴谱仪资料采集Source
 * @Author fmy
 * @Date 2019/12/16 14:55
 * @Version 1.0
 */
public class YdpSource extends AbstractSource implements Configurable, EventDrivenSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(YdpSource.class);
    private static String monitorDir = "";
    private static boolean watchSubtree = true;

    @Override
    public void configure(Context context) {
        monitorDir = context.getString("monitorDir");
        watchSubtree = context.getBoolean("watchSubtree",true);
    }

    @Override
    public synchronized void start() {
        if (!new File(monitorDir).isDirectory()) {
            if (new File(monitorDir).mkdirs()) {
                LOGGER.info("Successfully create Dir " + monitorDir);
            }
        }
        try {
            DllLoad.add2JavaLibPath();
            int watchID = JNotify.addWatch(monitorDir, MASK_CODE, watchSubtree, new YdpListener(getChannelProcessor()));
            if (watchID != 0) {
                LOGGER.info("监听目录" + monitorDir + "成功！");
                LOGGER.info("监听ID:" + watchID);
                TimeUnit.DAYS.sleep(365 * 50);
            }
        } catch (JNotifyException | InterruptedException e) {
            LOGGER.error("", e);
            System.exit(0);
        }
    }

    @Override
    public synchronized void stop() {
        super.stop();
    }
}

package com.hx.xbry.qh.gps;

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
 * @ClassName GPSSoundingSource
 * @Description 青海GPS移动探空资料采集source
 * @Author fmy
 * @Date 2019/12/10 10:37
 * @Version 1.0
 */
public class GPSSoundingSource extends AbstractSource implements Configurable, EventDrivenSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(GPSSoundingSource.class);
    private boolean watchSubtree = true;
    private String monitorDir = "";
    private boolean isTransferFile;
    private String saveDir = "";
    private boolean isDeleteSourceFile = false;// 是否删除源文件
    private boolean isPushFtp;
    private String uploadPath;

    @Override
    public void configure(Context context) {
        monitorDir = context.getString("monitorDir");
        watchSubtree = context.getBoolean("watchSubtree", true);
        isTransferFile = context.getBoolean("isTransferFile", false);
        saveDir = context.getString("saveDir");
        isDeleteSourceFile = context.getBoolean("isDeleteSourceFile", false);
        isPushFtp = context.getBoolean("isPushFtp", true);
        uploadPath = context.getString("upload.path");
    }

    @Override
    public synchronized void start() {
        // 因为每次探空任务分次进行，文件是整体生成后拷贝到内网指定目录
        // 可从一个文件夹下采集到另一个文件夹下或推送到远程FTP（可配置）
        if (!new File(monitorDir).isDirectory()) {
            if (new File(monitorDir).mkdirs()) {
                LOGGER.info("Successfully create Dir " + monitorDir);
            }
        }
        try {
            DllLoad.add2JavaLibPath();
            int watchID = JNotify.addWatch(monitorDir, MASK_CODE, watchSubtree, new GPSListener(isTransferFile,saveDir, isDeleteSourceFile,isPushFtp,uploadPath));
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

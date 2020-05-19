package com.hx.xbry.runnable;

import com.hx.xbry.tools.FileUtils;
import com.hx.xbry.tools.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.hx.xbry.bean.Constants.SDF;
import static com.hx.xbry.bean.Constants.SDF10;

/**
 * @ClassName FileCleanRunnable
 * @Description 文件清理线程类
 * @Author fmy
 * @Date 2020/2/27 13:58
 * @Version 1.0
 */
public class FileCleanRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileCleanRunnable.class);
    private final String fileDir;// 清理目录
    private int cleanInterval = 7 * 24;// 小文件删除周期（小时）
    private String fileNameReg = "";// 文件名正则匹配规则

    public FileCleanRunnable(String fileDir) {
        this.fileDir = fileDir;
    }

    public FileCleanRunnable(String fileDir, int cleanInterval) {
        this.fileDir = fileDir;
        this.cleanInterval = cleanInterval;
    }

    public FileCleanRunnable(String fileDir, String fileNameReg) {
        this.fileDir = fileDir;
        this.fileNameReg = fileNameReg;
    }

    public FileCleanRunnable(String fileDir, int cleanInterval, String fileNameReg) {
        this.fileDir = fileDir;
        this.cleanInterval = cleanInterval;
        this.fileNameReg = fileNameReg;
    }

    @Override
    public void run() {
        try {
            Date nowTime = new Date();
            Date dTime = SDF.parse(SDF10.format(nowTime) + " 23:59:59");
            long sleepMills = TimeUtils.calculateSleepMills(nowTime, dTime);
            LOGGER.info("监控目录：" + fileDir + "清理程序已启动...");
            LOGGER.info("第一次清理还剩" + mills2HoursAndMinutesString(sleepMills) + "...");
            Thread.sleep(sleepMills);
        } catch (ParseException | InterruptedException e) {
            LOGGER.error("", e);
        }
        while (true) {
            try {
                if (fileNameReg.isEmpty()) {
                    FileUtils.cleanSmallFile(fileDir, cleanInterval);
                } else {
                    FileUtils.cleanSmallFile(fileDir, cleanInterval, fileNameReg);
                }
                LOGGER.info("下一次清理还剩" + cleanInterval + "小时...");
                TimeUnit.HOURS.sleep(cleanInterval);
            } catch (InterruptedException e) {
                LOGGER.error("", e);
                break;
            }
        }
    }

    /**
    *@Description 毫秒数转换成小时分钟表示
    *@Author fmy
    *@Date 2020/2/27 16:07
    *@Param [mills]
    *@Return java.lang.String
    **/
    private String mills2HoursAndMinutesString(long mills) {
        int seconds = (int) (mills / 1000);
        int minute = seconds / 60;
        int hour = minute / 60;
        return hour + "小时" + (minute - hour * 60) + "分钟";
    }
}

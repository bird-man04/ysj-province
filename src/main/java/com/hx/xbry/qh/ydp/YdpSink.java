package com.hx.xbry.qh.ydp;

import com.alibaba.fastjson.JSONObject;
import com.hx.xbry.tools.CommonUtils;
import com.hx.xbry.tools.FileUtils;
import com.hx.xbry.tools.FtpUtils;
import com.hx.xbry.tools.TimeUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hx.xbry.bean.Constants.*;

/**
 * @ClassName YdpSink
 * @Description 青海雨滴谱仪资料采集Sink
 * @Author fmy
 * @Date 2019/12/16 15:00
 * @Version 1.0
 */
public class YdpSink extends AbstractSink implements Configurable {

    private static final Logger LOGGER = LoggerFactory.getLogger(YdpSink.class);
    private static final String FORMAT = "yyyyMM";
    private static final String FORMAT0 = "dd";
    private static final String TMP_DIR;

    static {
        if (CommonUtils.isWindows()) {
            TMP_DIR = System.getProperty("java.io.tmpdir") + "small" + File.separator + "ysj" + File.separator + "special-data" + File.separator + "ydp";
        } else {
            TMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + "small" + File.separator + "ysj" + File.separator + "special-data" + File.separator + "ydp";
        }
    }

    private static int cleanInterval;// 小文件删除周期（小时）
    private static String smallFileDir;
    private static boolean isPushFtp;
    private static String uploadPath;

    @Override
    public void configure(Context context) {
        cleanInterval = context.getInteger("clean.interval", 24);
        smallFileDir = context.getString("smallFile.path", TMP_DIR);
        isPushFtp = context.getBoolean("isPushFtp", true);
        uploadPath = context.getString("upload.path");
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.submit(new CleanRunnable());
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
                process0(event);
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            status = Status.BACKOFF;
            LOGGER.error("Delivery exception: {}", e.toString());
        } finally {
            transaction.close();
        }
        return status;
    }

    /**
     * @Description 处理event
     * @Author fmy
     * @Date 2019/12/16 16:00
     * @Param [event]
     * @Return void
     **/
    private void process0(Event event) {
        String body = new String(event.getBody(), StandardCharsets.UTF_8);
        Map<String, String> header = event.getHeaders();
        try {
            if (header.get("fileName").startsWith("UB_")) {
                processDataSource(body, header);
                return;
            }
            processPutuData(body, header);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    /**
     * @Description 处理原始数据
     * @Author fmy
     * @Date 2019/12/16 17:16
     * @Param [body, header]
     * @Return void
     **/
    private void processDataSource(String body, Map<String, String> header) throws IOException, ParseException {
        String[] contexts = body.split("\\r?\\n");
        if (contexts.length > 0) {
            loop:
            for (String context : contexts) {
                String fileName = header.get("fileName");
                if (fileName == null) {
                    return;
                }
                JSONObject resultJO = new JSONObject();
                resultJO.put("fileName", fileName);
                String[] headers = fileName.split("_");
                if (headers.length < 2) {
                    return;
                }
                String siteId = headers[1];
                resultJO.put("siteId", siteId);
                resultJO.put("dataType", "original");
                String beginTime = header.get("beginTime");
                String[] texts = context.split("\\s+");
                if (texts.length < 3) {
                    continue;
                }
                resultJO.put("data", context.substring(26));
                Date dataTime = SDF.parse(texts[1] + " " + texts[2]);// 北京时
                long timeStamp = dataTime.getTime();
                resultJO.put("dataTime", SDF.format(TimeUtils.beijing2world(dataTime)));// 世界时
                long dataTimestamp = SDF.parse(texts[1] + " " + texts[2]).getTime();
                String dir = smallFileDir + File.separator + siteId + File.separator + "original" + File.separator + TimeUtils.getCurrWorldDateFormatStr(dataTimestamp, FORMAT) + File.separator;
                String smallFileName = siteId + "_" + dataTimestamp + "_" + beginTime + "_" + SDF14.format(TimeUtils.beijing2world(new Date())) + ".txt";
                String path = dir + smallFileName;
                //是否存在该时间戳开始的文件进行过滤
                File parentFile = new File(new File(path).getParent());
                if (parentFile.isDirectory()) {
                    File[] files = parentFile.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.getName().startsWith(String.valueOf(dataTimestamp))) {
                                continue loop;
                            }
                        }
                    }
                }
                FileUtils.createIgnoreRepeatedFile(path, resultJO.toJSONString());
                if (isPushFtp) {
                    FtpUtils.uploadFile(uploadPath + "/" + siteId + "/original/" + TimeUtils.getCurrWorldDateFormatStr(dataTimestamp, FORMAT), smallFileName, path);
                }
                YdpUtils.sendOutDI(YdpUtils.createDIJsonObject("original",timeStamp));//发送分发DI
            }
        }
    }

    /**
     * @Description 处理谱图数据
     * @Author fmy
     * @Date 2019/12/17 10:16
     * @Param [body, header]
     * @Return void
     **/
    private void processPutuData(String body, Map<String, String> header) throws IOException, ParseException {
        String[] contexts = body.split("\\r?\\n");
        if (contexts.length < 4) {
            return;
        }
        String fileName = header.get("fileName");
        if (fileName == null) {
            return;
        }
        JSONObject resultJO = new JSONObject();
        resultJO.put("fileName", fileName);
        String[] headers = fileName.split("_");
        if (headers.length < 2) {
            return;
        }
        String siteId = headers[1];
        resultJO.put("siteId", siteId);
        resultJO.put("data", body);
        resultJO.put("dataType", "putu");
        String[] lineTexts = contexts[0].split("\\s+");
        if (lineTexts.length < 2) {
            return;
        }
        Date dataTime = SDF.parse(lineTexts[0] + " " + lineTexts[1] + ":00");// 北京时
        long timeStamp = dataTime.getTime();
        resultJO.put("dataTime", SDF.format(TimeUtils.beijing2world(dataTime)));// 世界时
        resultJO.put("PT", lineTexts[2]);
        resultJO.put("PV", contexts[2]);
        resultJO.put("PS", contexts[3]);

        long dataTimestamp = dataTime.getTime();
        String dir = smallFileDir + File.separator + siteId + File.separator + "putu" + File.separator + TimeUtils.getCurrWorldDateFormatStr(dataTimestamp, FORMAT) + File.separator + TimeUtils.getCurrWorldDateFormatStr(dataTimestamp, FORMAT0) + File.separator;
        String smallFileName = siteId + "_" + dataTimestamp + "_" + header.get("beginTime") + "_" + SDF14.format(TimeUtils.beijing2world(new Date())) + ".txt";
        String path = dir + smallFileName;
        //是否存在该时间戳开始的文件进行过滤
        File parentFile = new File(new File(path).getParent());
        if (parentFile.isDirectory()) {
            File[] files = parentFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().startsWith(String.valueOf(dataTimestamp))) {
                        LOGGER.info("谱图数据重复:" + file.getName());
                        return;
                    }
                }
            }
        }
        FileUtils.createIgnoreRepeatedFile(path, resultJO.toJSONString());
        if (isPushFtp) {
            FtpUtils.uploadFile(uploadPath + "/" + siteId + "/putu/" + TimeUtils.getCurrWorldDateFormatStr(dataTimestamp, FORMAT) + "/" + TimeUtils.getCurrWorldDateFormatStr(dataTimestamp, FORMAT0), smallFileName, path);
        }
        YdpUtils.sendOutDI(YdpUtils.createDIJsonObject("putu",timeStamp));//发送分发DI
    }

    /**
     * @Description 开启事务
     * @Author fmy
     * @Date 2019/11/26 13:53
     * @Param []
     * @Return org.apache.flume.Transaction
     **/
    private Transaction getTransaction() {
        return getChannel().getTransaction();
    }

    private static class CleanRunnable implements Runnable {
        @Override
        public void run() {//每天凌晨清理小文件
            try {
                Date nowTime = new Date();
                Date dTime = SDF.parse(SDF10.format(nowTime) + " 23:59:59");
                LOGGER.info("休眠毫秒数：" + TimeUtils.calculateSleepMills(nowTime, dTime));
                Thread.sleep(TimeUtils.calculateSleepMills(nowTime, dTime));
            } catch (ParseException | InterruptedException e) {
                LOGGER.error("", e);
            }
            while (true) {
                try {
                    FileUtils.cleanSmallFile(smallFileDir, cleanInterval);
                    TimeUnit.HOURS.sleep(cleanInterval);
                } catch (InterruptedException e) {
                    LOGGER.error("", e);
                }
            }
        }
    }
}

package com.hx.xbry.common.rest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hx.xbry.runnable.FileCleanRunnable;
import com.hx.xbry.tools.CommonUtils;
import com.hx.xbry.tools.FileUtils;
import com.hx.xbry.tools.TimeUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.Executors;

import static com.hx.xbry.bean.Constants.SDF;
import static com.hx.xbry.bean.Constants.SDF14;

/**
 * @ClassName RestSink
 * @Description 生成本地文件Sink
 * @Author fmy
 * @Date 2019/1/14 11:49
 * @Version 1.0
 */
public class RestSink extends AbstractSink implements Configurable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestSink.class);

    private static String smallFileDir;
    private static String dataCode; //资料四级编码

    @Override
    public void configure(Context context) {
        int cleanInterval = context.getInteger("clean.interval", 2 * 24); // 小文件删除周期（小时）
        smallFileDir = context.getString("smallFile.path");
        dataCode = context.getString("data.code");
        Executors.newFixedThreadPool(1).submit(new FileCleanRunnable(smallFileDir, cleanInterval));
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
                String body = new String(event.getBody(), StandardCharsets.UTF_8);
                JSONArray ja = JSONArray.parseArray(body);
                if (ja.size() > 0) {
                    ja = CommonUtils.sortByProperty(ja, true);
                    boolean isTraceData = "TRACE".equals(event.getHeaders().get("DATA_TYPE"));
                    for (int i = 0; i < ja.size(); i++) {
                        JSONObject resultJO = ja.getJSONObject(i);
                        String smallFileName = resultJO.getString("AIRPLANE_NUMBER") + "_" + resultJO.getString("ID") + "_" + event.getHeaders().get("BEGIN_TIME") + "_" + SDF14.format(TimeUtils.beijing2world(new Date())) + ".txt";
                        String path = smallFileDir + File.separator + smallFileName;
                        FileUtils.createIgnoreRepeatedFile(path, resultJO.toJSONString());
                        if (isTraceData) {
                            RestUtils.sendDI(RestUtils.createDIJsonObject("trace", dataCode, TimeUtils.getCurrWorldTime(resultJO.getString("DATA_TIME"), SDF)));
                        } else {
                            RestUtils.sendDI(RestUtils.createDIJsonObject("detection", dataCode, TimeUtils.getCurrWorldTime(resultJO.getString("DATA_TIME"), SDF)));
                        }
                    }
                }
            }
            transaction.commit();
        } catch (Exception e) {
            LOGGER.error("", e);
            transaction.rollback();
            status = Status.BACKOFF;
        } finally {
            transaction.close();
        }
        return status;
    }

    private Transaction getTransaction() {
        return getChannel().getTransaction();
    }

}

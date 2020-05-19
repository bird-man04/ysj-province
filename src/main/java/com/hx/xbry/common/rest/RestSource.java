package com.hx.xbry.common.rest;

import com.alibaba.fastjson.JSONArray;
import com.hx.xbry.setting.StateSetting;
import com.hx.xbry.tools.HttpUtils;
import com.hx.xbry.tools.SourceUtils;
import com.hx.xbry.tools.TimeUtils;
import org.apache.flume.Context;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.hx.xbry.bean.Constants.BATCH_NUM;

/**
 * @ClassName RestSource
 * @Description 飞机轨迹、探测资料接口
 * @Author fmy
 * @Date 2019/11/27 11:49
 * @Version 1.0
 */
public class RestSource extends AbstractSource implements PollableSource, Configurable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestSource.class);

    private long intervalMillis; // 检查周期（毫秒）
    private StateSetting stateSetting;
    private static String url;
    private static int count;

    @Override
    public void configure(Context context) {
        intervalMillis = context.getInteger("intervalMillis", 10 * 1000);
        url = context.getString("url");
        String recordFile = context.getString("recordFile");
        stateSetting = new StateSetting(recordFile);
        stateSetting.init();
    }

    @Override
    public Status process() {
        Status status = Status.READY;
        //TODO  2020/5/19 17:49 fmy
        //TODO  2020/5/19 18:19 fmy 优化访问接口的方式 防止
        try {
            Thread.sleep(intervalMillis);
        } catch (InterruptedException e) {
            LOGGER.error("", e);
        }
        if ((++count) % 100 == 0) {
            if (url.contains("trace")) {
                LOGGER.info("轨迹数据采集中...");
            } else {
                LOGGER.info("探测数据采集中...");
            }
            count = 0;
        }
        try {
            Date beginTime = TimeUtils.beijing2world(new Date());
            JSONArray queryJa = queryNewData(url);
            if (queryJa.isEmpty()) {
                return status;
            }
            if (url.contains("trace")) {
                SourceUtils.sendEvent(queryJa.toJSONString(), SourceUtils.createHeader(beginTime, "TRACE"), getChannelProcessor());
            } else {
                SourceUtils.sendEvent(queryJa.toJSONString(), SourceUtils.createHeader(beginTime, "DETECTION"), getChannelProcessor());
            }
        } catch (Exception e) {
            LOGGER.error("", e);
            status = Status.BACKOFF;
        }
        return status;
    }

    private JSONArray queryNewData(String url) throws IOException {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("num", BATCH_NUM);
        return stateSetting.removeDuplicate(JSONArray.parseArray(HttpUtils.sendGet(url, paramMap)));
    }

    @Override
    public long getBackOffSleepIncrement() {
        return 0;
    }

    @Override
    public long getMaxBackOffSleepInterval() {
        return 0;
    }
}

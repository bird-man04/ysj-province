package com.hx.xbry.setting;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hx.xbry.tools.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import static com.hx.xbry.bean.Constants.*;

/**
 * @ClassName StateSetting
 * @Description 程序运行状态记录类
 * @Author fmy
 * @Date 2019/10/12 16:43
 * @Version 1.0
 **/
public class StateSetting {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateSetting.class);
    private final ConcurrentHashMap<String, Long> recordMap = new ConcurrentHashMap<>();// id记录
    private final String recordPath;

    public StateSetting(String recordPath) {
        this.recordPath = PUBLIC_RECORD_DIR + recordPath;
    }

    public String getRecordPath() {
        return this.recordPath;
    }

    public ConcurrentHashMap<String, Long> getRecordMap() {
        return recordMap;
    }

    /**
     * @Description 初始化发送mq失败的消息，实现断点续传
     * @Author fmy
     * @Date 2019/10/23 10:51
     * @Param []
     * @Return void
     **/
    public void init() {
        try {
            loadRecord();
            LOGGER.info("配置文件路径:" + getRecordPath() + " 加载完成...");
        } catch (IOException e) {
            LOGGER.error("", e);
            System.exit(0);
        }
    }

    /**
    *@Description 重新加载配置文件
    *@Author fmy
    *@Date 2020/3/30 10:27
    *@Param []
    *@Return void
    **/
    public void reloadRecord() throws IOException {
        FileUtils.createIfNotExist(getRecordPath(), "{\"ID\":0}");
        JSONObject jo = JSON.parseObject(FileUtils.read(getRecordPath()));
        getRecordMap().clear();
        for (String key : jo.keySet()) {
            getRecordMap().put(key, jo.getLong(key));
        }
        LOGGER.info("重新加载配置:" + getRecordPath() + "成功...");
    }

    /**
     * @Description 初始化记录
     * @Author fmy
     * @Date 2019/11/7 12:00
     * @Param []
     * @Return void
     **/
    private void loadRecord() throws IOException {
        FileUtils.createIfNotExist(getRecordPath(), "{\"ID\":0}");
        JSONObject jo = JSON.parseObject(FileUtils.read(getRecordPath()));
        for (String key : jo.keySet()) {
            getRecordMap().put(key, jo.getLong(key));
        }
    }

    /**
     * @Description 添加或更新记录
     * @Author fmy
     * @Date 2019/11/7 12:01
     * @Param [type, id]
     * @Return void
     **/
    public void addRecordMap(Long id) throws IOException {
        getRecordMap().put("ID", id);
        JSONObject jsonObject = JSON.parseObject(FileUtils.read(getRecordPath()));
        jsonObject.put("ID", id);
        FileUtils.rewrite(getRecordPath(), jsonObject.toJSONString());
    }

    /**
     * @Description 去重
     * @Author fmy
     * @Date 2020/3/10 9:13
     * @Param [jsonArray, stateSetting]
     * @Return com.alibaba.fastjson.JSONArray
     **/
    public JSONArray removeDuplicate(JSONArray jsonArray) throws IOException {
        return removeDuplicate(jsonArray, "ID");
    }

    /**
     * @Description 去重
     * @Author fmy
     * @Date 2020/3/28 22:42
     * @Param [jsonArray]
     * @Return com.alibaba.fastjson.JSONArray
     **/
    public JSONArray removeDuplicate(JSONArray jsonArray, String key) throws IOException {
        JSONArray result = new JSONArray();
        if (getRecordMap().get("ID") == null) {
            reloadRecord();
        }
        long oldId = getRecordMap().get("ID");
        long newId = oldId;
        //TODO 做一个排序优化
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jo = jsonArray.getJSONObject(i);
            if (jo.getString(key) == null || jo.getString(key).isEmpty()) {
                continue;
            }
            int id = Integer.parseInt(jo.getString(key).trim());
            if (!isNew(id, oldId)) {
                continue;
            }
            if (id > newId) {
                newId = id;
            }
            result.add(jo);
        }
        if (result.size() > 0) {
            addRecordMap(newId); //fix fmy 2020-1-9 一批中,考虑有多个的情况
        }
        return result;
    }

    /**
     * @Description 判断是否为新数据
     * @Author fmy
     * @Date 2020/3/9 12:20
     * @Param [id, oldId]
     * @Return boolean
     **/
    private boolean isNew(int id, long oldId) {
        return id > oldId;
    }
}

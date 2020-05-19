package com.hx.xbry.qh.ydp;

import com.alibaba.fastjson.JSONObject;
import com.hx.xbry.bean.RestfulInfo;
import com.hx.xbry.bean.SendType;
import com.hx.xbry.bean.StatDI;
import com.hx.xbry.tools.PropertiesUtils;
import com.hx.xbry.tools.RestfulDataUtils;
import com.hx.xbry.tools.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static com.hx.xbry.bean.Constants.SDF;

/**
 * @ClassName YdpUtils
 * @Description 青海雨滴谱仪资料采集 工具类
 * @Author fmy
 * @Date 2020/2/23 23:15
 * @Version 1.0
 */
public class YdpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(YdpUtils.class);
    private static final String FORMAT = "yyyy-MM-dd HH:00:00";

    /**
     * @Description 发送接入DI
     * @Author fmy
     * @Date 2020/2/23 23:18
     * @Param [content]
     * @Return void
     **/
    static void sendInDI(JSONObject jsonObject) {
        try {
            RestfulDataUtils.send(createDI(jsonObject, 2), SendType.DI);
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

    /**
     * @Description 发送分发
     * @Author fmy
     * @Date 2020/2/23 23:19
     * @Param [jsonObject]
     * @Return void
     **/
    static void sendOutDI(JSONObject jsonObject) {
        try {
            RestfulDataUtils.send(createDI(jsonObject, 3), SendType.DI);
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

    /**
     * @Description 新建接入DI信息
     * @Author fmy
     * @Date 2020/2/23 23:29
     * @Param [jo, processLink]
     * @Return com.hx.xbry.bean.RestfulInfo
     **/
    private static RestfulInfo createDI(JSONObject jo, int processLink) {
        RestfulInfo restfulInfo = new RestfulInfo();
        restfulInfo.setType(PropertiesUtils.getProperty("DI.TYPE", "SPACE.CS.STATION.DI"));
        restfulInfo.setName("青海人影雨滴谱探测资料");
        if ("putu".equals(jo.getString("type"))) {
            restfulInfo.setMessage("谱图数据");
        } else {
            restfulInfo.setMessage("原始数据");
        }
        restfulInfo.setOccur_time(jo.getLongValue("timeStamp"));
        restfulInfo.setFields(createStatDI(jo, jo.getLongValue("timeStamp"), processLink));
        return restfulInfo;
    }

    /**
     * @Description 创建StatDI信息
     * @Author fmy
     * @Date 2019/11/7 8:49
     * @Param [longitude, latitude, height, occurTime, type, processLink]
     * @Return StatDI
     **/
    private static StatDI createStatDI(JSONObject jo, long occurTime, int processLink) {
        StatDI statDI = new StatDI();
        statDI.setDATA_TYPE("A.0001.0049.R002");
        statDI.setSYSTEM("QH-RY"); // qhry = 青海人影
        if (occurTime != -1) {
            statDI.setDATA_TIME(TimeUtils.getCurrWorldDateFormatStr(occurTime, FORMAT));// 整点表示
        }
        Date now = new Date();
        statDI.setTRAN_TIME(SDF.format(TimeUtils.beijing2world(now)));
        statDI.setRECORD_TIME(SDF.format(TimeUtils.beijing2world(now)));
        statDI.setPROCESS_LINK(String.valueOf(processLink));
        if (2 == processLink) { //2表示接入DI
            statDI.setFILE_NAME_O(jo.getString("fileName"));
            // statDI.setNewFileName(path);
            statDI.setFILE_SIZE(String.valueOf(new File(jo.getString("absPath")).length()));
        }
        return statDI;
    }

    /**
     * @Description 创建接收 DI必要信息JO
     * @Author fmy
     * @Date 2020/2/24 0:12
     * @Param [type, timeStamp, file] 类型，数据时间戳，文件
     * @Return com.alibaba.fastjson.JSONObject
     **/
    static JSONObject createDIJsonObject(String type, long timeStamp, File file) {
        JSONObject resultJo = new JSONObject();
        resultJo.put("type", type);
        resultJo.put("timeStamp", timeStamp);
        resultJo.put("fileName", file.getName());
        resultJo.put("absPath", file.getAbsolutePath());
        return resultJo;
    }

    /**
     * @Description 创建分发 DI必要信息JO
     * @Author fmy
     * @Date 2020/2/24 0:12
     * @Param [type, timeStamp, file] 类型，数据时间戳，文件
     * @Return com.alibaba.fastjson.JSONObject
     **/
    static JSONObject createDIJsonObject(String type, long timeStamp) {
        JSONObject resultJo = new JSONObject();
        resultJo.put("type", type);
        resultJo.put("timeStamp", timeStamp);
        return resultJo;
    }
}

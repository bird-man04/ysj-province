package com.hx.xbry.common.rest;

import com.alibaba.fastjson.JSONObject;
import com.hx.xbry.bean.RestfulInfo;
import com.hx.xbry.bean.SendType;
import com.hx.xbry.bean.StatDI;
import com.hx.xbry.qh.ydp.YdpUtils;
import com.hx.xbry.tools.PropertiesUtils;
import com.hx.xbry.tools.RestfulDataUtils;
import com.hx.xbry.tools.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

import static com.hx.xbry.bean.Constants.FORMAT;
import static com.hx.xbry.bean.Constants.SDF;

/**
 * @ClassName RestUtils
 * @Description Rest接口工具类
 * @Author fmy
 * @Date 2020/03/06 11:41
 * @Version 1.0
 */
class RestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(YdpUtils.class);

    /**
     * @Description 发送分发
     * @Author fmy
     * @Date 2020/2/23 23:19
     * @Param [jsonObject]
     * @Return void
     **/
    static void sendDI(JSONObject jsonObject) {
        try {
            RestfulDataUtils.send(createDI(jsonObject), SendType.DI);
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
    private static RestfulInfo createDI(JSONObject jo) {
        RestfulInfo restfulInfo = new RestfulInfo();
        restfulInfo.setType(PropertiesUtils.getProperty("DI.TYPE", "SPACE.CS.STATION.DI"));
        restfulInfo.setName("人影空地系统资料");
        if ("trace".equals(jo.getString("TYPE"))) {
            restfulInfo.setMessage("轨迹数据");
        } else {
            restfulInfo.setMessage("探测数据");
        }
        restfulInfo.setOccur_time(jo.getLongValue("DATA_TIMESTAMP"));
        restfulInfo.setFields(createStatDI(jo, jo.getLongValue("DATA_TIMESTAMP")));
        return restfulInfo;
    }

    /**
     * @Description 创建StatDI信息
     * @Author fmy
     * @Date 2019/11/7 8:49
     * @Param [longitude, latitude, height, occurTime, type, processLink]
     * @Return StatDI
     **/
    private static StatDI createStatDI(JSONObject jo, long occurTime) {
        StatDI statDI = new StatDI();
        statDI.setDATA_TYPE(jo.getString("DATA_CODE"));
        statDI.setSYSTEM(PropertiesUtils.getProperty("DI.SYSTEM")); // QH-RY = 青海人影
        if (occurTime != -1) {
            statDI.setDATA_TIME(TimeUtils.getCurrWorldDateFormatStr(occurTime, FORMAT));// 整点表示
        }
        statDI.setTRAN_TIME(SDF.format(TimeUtils.beijing2world(new Date())));
        statDI.setRECORD_TIME(SDF.format(TimeUtils.beijing2world(new Date())));
        statDI.setPROCESS_LINK(String.valueOf(3));
        return statDI;
    }

    /**
    *@Description 创建分发 DI必要信息JO
    *@Author fmy
    *@Date 2020/3/11 11:39
    *@Param [type, timeStamp]
    *@Return com.alibaba.fastjson.JSONObject
    **/
    static JSONObject createDIJsonObject(String type,String dataCode, long timeStamp) {
        JSONObject resultJo = new JSONObject();
        resultJo.put("TYPE", type);
        resultJo.put("DATA_CODE", dataCode);
        resultJo.put("DATA_TIMESTAMP", timeStamp);
        return resultJo;
    }

}

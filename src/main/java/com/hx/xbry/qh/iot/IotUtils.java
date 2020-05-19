package com.hx.xbry.qh.iot;

import com.alibaba.fastjson.JSONObject;
import com.hx.xbry.bean.Constants;
import com.hx.xbry.bean.RestfulInfo;
import com.hx.xbry.bean.SendType;
import com.hx.xbry.bean.StatDI;
import com.hx.xbry.tools.PropertiesUtils;
import com.hx.xbry.tools.RestfulDataUtils;
import com.hx.xbry.tools.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

import static com.hx.xbry.bean.Constants.SDF;

/**
 * @ClassName IOTUtils
 * @Decription 物联网工具类
 * @Author fmy
 * @Date 2020/01/15 15:08
 * @Version 1.0
 */
class IotUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(IotUtils.class);

    /**
     * @Description 发送分发DI
     * @Author fmy
     * @Date 2019/11/26 9:48
     * @Param [msg]
     * @Return void
     **/
    static void sendDI(JSONObject jsonObject) {
        try {
            RestfulDataUtils.send(createDI(jsonObject), SendType.DI);
        } catch (IOException e) {
            LOGGER.error("Send OUT DI failed ", e);
        }
    }

    /**
     * @Description 新建DI信息
     * @Author fmy
     * @Date 2019/11/7 15:19
     * @Param [jo, processLink]
     * @Return com.hx.xbry.bean.RestfulInfo
     **/
    private static RestfulInfo createDI(JSONObject jo) {
        RestfulInfo restfulInfo = new RestfulInfo();
        restfulInfo.setType(PropertiesUtils.getProperty("DI.TYPE", "SPACE.CS.STATION.DI"));
        restfulInfo.setName("物联网数据");
        restfulInfo.setMessage(jo.getString("TYPE"));
        long occurTime = jo.getLongValue("DATA_TIME");
        restfulInfo.setOccur_time(occurTime);
        restfulInfo.setFields(createStatDI(occurTime));
        return restfulInfo;
    }

    /**
     * @Description 创建StatDI信息
     * @Author fmy
     * @Date 2019/11/7 8:49
     * @Param [longitude, latitude,  occurTime, type, processLink]
     * @Return StatDI
     **/
    private static StatDI createStatDI(long occurTime) {
        StatDI statDI = new StatDI();
        statDI.setDATA_TYPE("Z.0009.0004.R001");
        statDI.setSYSTEM("IOT_SYS"); // IOT_SYS = 物联网系统
        if (occurTime != -1) {
            statDI.setDATA_TIME(TimeUtils.getCurrWorldDateFormatStr(occurTime, Constants.FORMAT));// 整点表示
        }
        statDI.setTRAN_TIME(SDF.format(TimeUtils.beijing2world(new Date())));
        statDI.setRECORD_TIME(SDF.format(TimeUtils.beijing2world(new Date())));
        statDI.setPROCESS_LINK(String.valueOf(3));
        return statDI;
    }

    /**
     * @Description 创建参数
     * @Author fmy
     * @Date 2020/5/11 15:23
     * @Param [timeStamp, type] 时间戳表示
     * @Return com.alibaba.fastjson.JSONObject
     **/
    public static JSONObject createParamJo(long timeStamp, String type) {
        JSONObject resultJo = new JSONObject();
        resultJo.put("DATA_TIME", timeStamp);
        resultJo.put("TYPE", type);
        return resultJo;
    }

}

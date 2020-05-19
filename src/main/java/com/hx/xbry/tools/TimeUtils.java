package com.hx.xbry.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @ClassName TimeUtils
 * @Description 时间处理工具类
 * @Author fmy
 * @Date 2019/10/22 22:00
 * @Version 1.0
 */
public class TimeUtils {

    /**
    *@Description 北京时转为世界时
    *@Author fmy
    *@Date 2019/10/22 22:03
    *@Param [bj]
    *@Return java.util.Date
    **/
    public static Date beijing2world(Date bj) {
        return new Date(bj.getTime() - 8 * 60 * 60 * 1000);
    }

    /**
     *@Description 获取时间戳
     *@Author fmy
     *@Date 2019/12/16 10:45
     *@Param [str14] 传入十四位时间字符串（世界时）
     *@Return long
     **/
    public static long getCurrWorldTime(String str,SimpleDateFormat sdf) throws  ParseException {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return  sdf.parse(str).getTime();
    }

    /**
     *@Description 获取时间戳
     *@Author fmy
     *@Date 2019/12/16 10:45
     *@Param [str14] 传入十四位时间字符串（世界时）
     *@Return long
     **/
    public static long getCurrWorldTime(String str14) throws  ParseException {
        return  getCurrWorldTime(str14,new SimpleDateFormat("yyyyMMddHHmmss"));
    }

    /**
     *@Description 获取时间字符串
     *@Author fmy
     *@Date 2019/12/16 11:24
     *@Param [timestamp, format]
     *@Return java.lang.String
     **/
    public static String getCurrWorldDateFormatStr(long timestamp, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(timestamp));
    }

    /**
     * @Description 计算整文件dump线程首次启动需要休眠的时间（毫秒）
     * @Author fmy
     * @Date 2019/12/4 16:11
     * @Param []
     * @Return long
     **/
    public static long calculateSleepMills(Date nowTime, Date dTime) {
        long n;
        if (nowTime.getTime() > dTime.getTime()) {// 需要加一天
            n = dTime.getTime() + 24 * 60 * 60 * 1000 - nowTime.getTime();
        } else {
            n = dTime.getTime() - nowTime.getTime();
        }
        return n;
    }
}

package com.hx.xbry.tools;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @ClassName CommonUtils
 * @Description 通用工具类
 * @Author fmy
 * @Date 2019/11/26 11:25
 * @Version 1.0
 */
public class CommonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

    /**
     * @Description 判断是否为windows系统
     * @Author fmy
     * @Date 2019/10/23 10:55
     * @Param []
     * @Return boolean
     **/
    public static boolean isWindows() {
        return System.getProperty("os.name") == null || System.getProperty("os.name").toUpperCase().contains("WINDOWS");
    }

    /**
     *@Description 根据数据时间排序
     *@Author fmy
     *@Date 2020/5/8 17:45
     *@Param [jsonArray, isPositive]
     *@Return com.alibaba.fastjson.JSONArray
     **/
    public static JSONArray sortById(JSONArray jsonArray, boolean isPositive) {
        return sortByProperty(jsonArray, isPositive, "ID");
    }

    /**
    *@Description 根据数据时间排序
    *@Author fmy
    *@Date 2020/5/8 17:45
    *@Param [jsonArray, isPositive]
    *@Return com.alibaba.fastjson.JSONArray
    **/
    public static JSONArray sortByProperty(JSONArray jsonArray, boolean isPositive) {
        return sortByProperty(jsonArray, isPositive, "DATA_TIME");
    }

    /**
     * @Description 根据属性排序
     * @Author fmy
     * @Date 2020/5/8 17:39
     * @Param [jsonArray, isPositive, key] isPositive true表示正序，从小到大
     * @Return com.alibaba.fastjson.JSONArray
     **/
    public static JSONArray sortByProperty(JSONArray jsonArray, boolean isPositive, String key) {
        List<JSONObject> resultList = JSONArray.parseArray(jsonArray.toJSONString(), JSONObject.class);
        resultList.sort((o1, o2) -> {
            if (o1 == null || o2 == null) {
                return 0;
            }
            if (isPositive) {
                return o1.getString(key).compareTo(o2.getString(key));
            }
            return o2.getString(key).compareTo(o1.getString(key));
        });
        return JSONArray.parseArray(resultList.toString());
    }

}

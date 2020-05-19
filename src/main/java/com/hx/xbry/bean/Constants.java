package com.hx.xbry.bean;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * @ClassName Constants
 * @Description 常量
 * @Author fmy
 * @Date 2019/12/3 9:25
 * @Version 1.0
 */
public class Constants {

    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat SDF6 = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat SDF8 = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat SDF10 = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat SDF14 = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final SimpleDateFormat SDF_HH = new SimpleDateFormat("HH");
    public static final String FORMAT = "yyyy-MM-dd HH:00:00";

    public static final String DTF8 = "yyyyMMdd";
    public static final String DTF14 = "yyyyMMddHHmmss";

    public static final int BUFFER_SIZE = 4096;
    public static final int MASK_CODE = 5;//(0101:表示只监控新建和文件变化)
    public static final String BATCH_NUM = "12";// 支持两架飞机同时在飞的数据
    public static final String PUBLIC_CONF_DIR = System.getProperty("user.dir") + File.separator + ".." + File.separator + "conf" + File.separator;
    public static final String PUBLIC_RECORD_DIR = System.getProperty("user.dir") + File.separator + ".." + File.separator + "record" + File.separator;
}

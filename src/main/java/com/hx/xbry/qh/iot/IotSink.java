package com.hx.xbry.qh.iot;

import com.alibaba.fastjson.JSONObject;
import com.hx.xbry.setting.StateSetting;
import com.hx.xbry.tools.*;
import org.apache.flume.Context;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.hx.xbry.bean.Constants.*;
import static com.hx.xbry.qh.iot.SubBizType.AIOS;

/**
 * @ClassName IOTSink
 * @Description 青海物联网sink
 * @Author fmy
 * @Date 2019/11/27 11:49
 * @Version 1.0
 */
public class IotSink extends AbstractSink implements Configurable {

    private static final Logger LOGGER = LoggerFactory.getLogger(IotSink.class);

    private static String savePath;
    private StateSetting stateSetting;

    @Override
    public void configure(Context context) {
        savePath = context.getString("save.path");
        stateSetting = new StateSetting("msgIdRecord.json");
        stateSetting.init();
    }

    @Override
    public Status process() {
        return FlumeUtils.process(getChannel(), event -> {
            String body = new String(event.getBody(), StandardCharsets.UTF_8);
            try {
                String subBizType = event.getHeaders().get("DATA_TYPE");

                JSONObject jo = JSONObject.parseObject(body);
                String data = createData(subBizType, jo);
                if (data.isEmpty()) {
                    return;
                }
                JSONObject resultJo = new JSONObject();
                resultJo.put("DATA", data);
                long msgId = stateSetting.getRecordMap().get("ID");
                resultJo.put("MSG_ID", ++msgId);// 8位 00004773
                resultJo.put("SEND_TIME", LocalDateTime.now().format(DateTimeFormatter.ofPattern(DTF14)));// 20200418104212
                resultJo.put("BIZ_TYPE", "RY");// RY=人影
                resultJo.put("SUB_BIZ_TYPE", subBizType);
                resultJo.put("SYSTEM_VERSION", "0001");
                resultJo.put("PRIORITY", "00");
                resultJo.put("SENDER", "RLQHDKSJ");// 青海物联网系统代字
                resultJo.put("FILE_COUNT", "00");
                String dir = savePath + File.separator + resultJo.getString("BIZ_TYPE") + File.separator + subBizType + File.separator + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DTF8)) + File.separator;
                String fileName = subBizType + "_" + msgId + "_" + event.getHeaders().get("BEGIN_TIME") + "_" + SDF14.format(TimeUtils.beijing2world(new Date())) + ".txt";
                FileUtils.create(dir + fileName, resultJo.toJSONString());
                if (update(subBizType, jo.getIntValue("KID"))) {// 更新数据库表记录
                    LOGGER.info("已更新:" + jo.getIntValue("KID"));
                } else {
                    LOGGER.info("KID更新失败...");
                }
                stateSetting.addRecordMap(msgId);
                IotUtils.sendDI(IotUtils.createParamJo(LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli(), subBizType));
            } catch (Exception e2) {
                LOGGER.error("", e2);
            }
        });
    }

    /**
     * @Description 更新字段is_processed, 标记是否已处理, 0表示未处理, 1表示已处理
     * @Author fmy
     * @Date 2020/5/11 14:41
     * @Param [kid] 数据行id
     * @Return boolean
     **/
    private boolean update(String type, int kid) {
        String table;
        switch (type) {
            case "AIOS":
                table = "z_private_storage_info";
                break;
            case "AMTI":
                table = "";
                break;
            case "DSOI":
                table = "z_private_dispatch_info";
                break;
            case "GSRI":
                table = "z_private_fire_declare";
                break;
            default:
                table = "";
                break;

        }
        String updateSQL = "UPDATE " + table + " SET is_processed = 1 WHERE kid = " + kid;
        Connection conn = null;
        try {
            conn = JDBCUtils.getConnection();
            conn.createStatement().execute(updateSQL);
            return true;
        } catch (SQLException e) {
            LOGGER.error("", e);
        } finally {
            JDBCUtils.free(conn);
        }
        return false;
    }

    /**
     * @Description 创建数据属性
     * @Author fmy
     * @Date 2020/5/11 14:05
     * @Param [subBizType, jo]
     * @Return java.lang.String
     **/
    private String createData(String subBizType, JSONObject jo) {
        String data = "";
        if (AIOS.name().equals(subBizType)) {
            data = createAIOSXml(jo);
        } else if (SubBizType.AMTI.name().equals(subBizType)) {
            data = createAMTIXml(jo);
        } else if (SubBizType.DSOI.name().equals(subBizType)) {
            data = createDSOIXml(jo);
        } else if (SubBizType.GSRI.name().equals(subBizType)) {
            data = createGSRIData(jo);
        }
        return data;
    }

    /**
     * @Description 创建弹药出入库xml
     * @Author fmy
     * @Date 2020/5/9 10:23
     * @Param []
     * @Return java.lang.String
     **/
    private static String createAIOSXml(JSONObject jo) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("STORE");// 添加根节点
        root.addElement("STOREHOUSEID").addText(getString(jo, "STORE_HOUSE_ID"));// 仓库编码
        String storeFlag; //出入库标识，分为六种类型：11为运输入库、12为作业入库、13为采购入库出库类型、21为运输出库、22为作业出库、23为销毁出库
        if ("0".equals(getString(jo, "STORE_FLAG"))) {
            storeFlag = "21";
        } else {
            storeFlag = "11";
        }
        root.addElement("STOREFLAG").addText(storeFlag);// 出入库标识
        root.addElement("USERID").addText(getString(jo, "USER_ID"));// 责任人编码
        root.addElement("STOREDATE").addText(getString(jo, "STORE_DATE"));// 出入库时间 yyyy-HH-mm HH:mm:ss
        Element ammoInfoElement = root.addElement("AMMOINFO");
        Element boxInfoElement = ammoInfoElement.addElement("BOXINFO");
        boxInfoElement.addElement("BOXCODE").addText(getString(jo, "BOX_CODE"));// 弹箱编码
        boxInfoElement.addElement("AMMOCODE").addText(getString(jo, "AMMO_CODE"));// 弹药编码
        return document2String(document);
    }

    /**
     * @Description 创建弹药轨迹XML
     * @Author fmy
     * @Date 2020/5/9 11:35
     * @Param [jo]
     * @Return java.lang.String
     **/
    public static String createAMTIXml(JSONObject jo) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("TRANSPORT");// 添加根节点
        root.addElement("TRANSPORTID").addText(getString(jo, "TRANSPORT_ID"));// 运输编号
        root.addElement("LONGITUDE").addText(getString(jo, "LONGITUDE"));// 经度
        root.addElement("LATITUDE").addText(getString(jo, "LATITUDE"));// 纬度
        root.addElement("CREATEDATE").addText(getString(jo, "DATA_TIME"));// 编制时间 yyyy-HH-mm HH:mm:ss
        return document2String(document);
    }

    /**
     * @Description 创建派发单信息XML
     * @Author fmy
     * @Date 2020/5/9 11:41
     * @Param [jo]
     * @Return java.lang.String
     **/
    private String createDSOIXml(JSONObject jo) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("DISTRIBUTEORDER");// 添加根节点
        root.addElement("DISTRIBUTEORDERID").addText(getString(jo, "DISTRIBUTE_ORDER_ID"));// 派发单编号
        root.addElement("TRANSPORTID").addText(getString(jo, "TRANSPORT_ID"));// 运输编号
        root.addElement("DISTRIBUTEORGID").addText(getString(jo, "DISTRIBUTE_ORG_ID"));// 派发单位编码
        root.addElement("RECEIVEORGID").addText(getString(jo, "RECEIVE_ORG_ID"));// 接收单位编码
        root.addElement("DISTRIBUTEUSERID").addText(getString(jo, "DISTRIBUTE_USER_ID"));// 派发单位责任人编码
        root.addElement("RECEIVEUSERID").addText(getString(jo, "RECEIVE_USER_ID"));// 接收单位责任人编码
        root.addElement("DISTRIBUTEDATE").addText(getString(jo, "DISTRIBUTE_DATE"));// 派发时间 yyyy-HH-mm HH:mm:ss

        Element ammoInfoElement = root.addElement("AMMOINFO");
        Element boxInfoElement = ammoInfoElement.addElement("BOXINFO");
        boxInfoElement.addElement("BOXCODE").addText(getString(jo, "BOX_CODE"));// 弹箱编码
        boxInfoElement.addElement("AMMOCODE").addText(getString(jo, "AMMO_CODE"));// 弹药编码
        return document2String(document);
    }

    /**
     * @Description 创建地面作业实时记录信息XML
     * @Author fmy
     * @Date 2020/5/9 11:41
     * @Param [jo]
     * @Return java.lang.String   00001,520402001,20140405,182200,182400,4550,340020,031020303,01210305010
     **/
    private String createGSRIData(JSONObject jo) {
        String xh = getXH(getString(jo,"ID"));
        String workId = getString(jo,"WORK_ID","000000000");
        String date = getString(jo,"WORK_DATE");
        String startTime = getString(jo,"START_TIME");
        String endTime = getString(jo,"END_TIME");
        String elevation = getString(jo,"ELEVATION");
        String azimuth = getString(jo,"AZIMUTH");
        String equipment = getString(jo,"EQUIPMENT");
        String ammoInfo = getString(jo,"AMMO_INFO");
        StringBuilder rsb = new StringBuilder();
        rsb.append(xh).append(",");
        rsb.append(workId).append(",");
        rsb.append(date).append(",");
        rsb.append(startTime).append(",");
        rsb.append(endTime).append(",");
        rsb.append(elevation).append(",");
        rsb.append(azimuth).append(",");
        rsb.append(equipment).append(",");
        rsb.append(ammoInfo);
        return rsb.toString();
    }

    /**
     *@Description 获取序号
     *@Author fmy
     *@Date 2020/5/15 16:36
     *@Param [id]
     *@Return java.lang.String
     **/
    String getXH(String id) {
        if (id.isEmpty()) {
            return "";
        }
        int i = Integer.parseInt(id.trim());
        if (String.valueOf(i).length() < 5) {
            StringBuilder sb = new StringBuilder();
            for (int i1 = (5 - String.valueOf(i).length()); i1 > 0; i1--) {
                sb.append("0");
            }
            return sb.append(i).toString();
        }
        return id;
    }


    /**
     * @Description 格式化输出Document
     * @Author fmy
     * @Date 2020/5/9 11:33
     * @Param [document]
     * @Return java.lang.String
     **/
    private static String document2String(Document document) {
        XMLWriter writer = null;
        try (OutputStream os = new ByteArrayOutputStream()) {
            writer = new XMLWriter(os, OutputFormat.createPrettyPrint());
            writer.write(document);
            writer.flush();
            return StringUtils.replaceBlankLine(os.toString());
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    LOGGER.error("", e);
                }
            }
        }
        return "";
    }

    private static String getString(JSONObject jsonObject, String key) {
        return jsonObject.getString(key) == null ? "" : jsonObject.getString(key);
    }

    private static String getString(JSONObject jsonObject, String key, String defaultValue) {
        return jsonObject.getString(key) == null ? defaultValue.isEmpty() ? "" : defaultValue : jsonObject.getString(key);
    }

}

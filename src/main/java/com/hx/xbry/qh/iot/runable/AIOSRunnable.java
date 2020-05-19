package com.hx.xbry.qh.iot.runable;

import com.alibaba.fastjson.JSONObject;
import com.hx.xbry.qh.iot.SubBizType;
import com.hx.xbry.setting.StateSetting;
import com.hx.xbry.tools.JDBCUtils;
import com.hx.xbry.tools.SourceUtils;
import com.hx.xbry.tools.TimeUtils;
import org.apache.flume.channel.ChannelProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName AIOSRunnable
 * @Description 处理弹药出入库资料线程类
 * @Author fmy
 * @Date 2020/5/8 16:26
 * @Version 1.0
 */
public class AIOSRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AIOSRunnable.class);
    private final ChannelProcessor channelProcessor;
    private final StateSetting stateSetting;
    private final int period;

    public AIOSRunnable(ChannelProcessor channelProcessor) {
        this.channelProcessor = channelProcessor;
        period = 60;
        stateSetting = new StateSetting("AIOSRecord.json");
        stateSetting.init();
    }

    public AIOSRunnable(ChannelProcessor channelProcessor, StateSetting stateSetting, int period) {
        this.channelProcessor = channelProcessor;
        this.stateSetting = stateSetting;
        this.period = period;
    }

    @Override
    public void run() {
        LOGGER.info("启动弹药出入库信息采集...");
        while (true) {
            try {
                collect();
            } catch (SQLException | IOException e) {
                LOGGER.error("",e);
            }finally {
                try {
                    TimeUnit.SECONDS.sleep(period);
                } catch (InterruptedException e) {
                    LOGGER.error("",e);
                }
            }
        }
    }

    /**
     * @Description 采集
     * @Author fmy
     * @Date 2020/5/9 9:18
     * @Param []
     * @Return void
     **/
    private void collect() throws SQLException, IOException {
        long kid = stateSetting.getRecordMap().get("ID");
        if (kid == 0) {
            updateId();
            kid = stateSetting.getRecordMap().get("ID");
        }
        Connection conn = JDBCUtils.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT kid,id,is_processed FROM z_private_storage_info WHERE kid > " + kid + " AND is_processed = 0 ORDER BY id ASC");
        ResultSet rs = ps.executeQuery();
        long newId = 0;
        while (rs.next()) {
            Date beginTime = TimeUtils.beijing2world(new Date());
            boolean isProcessed = rs.getInt("is_processed") != 0;
            if (isProcessed) {
                continue;
            }
            int tid = rs.getInt("kid");
            if (tid > kid) {
                newId = tid;
            }
            PreparedStatement ps2 = conn.prepareStatement("SELECT b.direction storeFlag,b.unit_id storeHouseId,b.operater userId,b.operate_datetime storeDate,c.item_code ammoCode,c.box_no boxCode FROM storage_item a LEFT JOIN storage_info b ON  a.storage_id = b.id LEFT JOIN item_info c ON a.item_id = c.id where a.Id = ?;");
            ps2.setString(1, rs.getString("id"));
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) {
                JSONObject jo = new JSONObject();
                jo.put("KID", tid);
                jo.put("STORE_HOUSE_ID", rs2.getString("storeHouseId"));//仓库编码
                jo.put("STORE_FLAG", rs2.getString("storeFlag"));//出入库标识  1入库 0出库
                jo.put("USER_ID", rs2.getString("userId"));//责任人编码
                jo.put("STORE_DATE", rs2.getString("storeDate"));//出入库时间 yyyy-MM-dd HH:mm:ss 北京时
                jo.put("BOX_CODE", rs2.getString("ammoCode"));//弹箱编码
                jo.put("AMMO_CODE", rs2.getString("boxCode"));//弹药编码
                LOGGER.info("采集数据:" + jo);
                SourceUtils.sendEvent(jo.toJSONString(), SourceUtils.createHeader(beginTime, SubBizType.AIOS.name()), channelProcessor);
            }
            try {
                JDBCUtils.free(rs2, ps2);
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
        if (newId > kid) {
            stateSetting.addRecordMap(newId);
        }
        try {
            JDBCUtils.free(rs, ps, conn);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    /**
    *@Description 更新ID
    *@Author fmy
    *@Date 2020/5/9 16:30
    *@Param []
    *@Return void
    **/

    private void updateId() throws IOException, SQLException {
        Connection conn = JDBCUtils.getConnection();
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("SELECT kid FROM z_private_storage_info ORDER BY kid DESC LIMIT 1;");
        long id = 0;
        if (rs.next()) {
            id = rs.getLong(1);
        }
        stateSetting.addRecordMap(id);
        JDBCUtils.free(rs, stat, conn);
    }
}

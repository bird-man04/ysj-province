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
 * @ClassName DSOIRunnable
 * @Description 派发单信息资料线程类
 * @Author fmy
 * @Date 2020/5/13 10:10
 * @Version 1.0
 */
public class DSOIRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DSOIRunnable.class);
    private final ChannelProcessor channelProcessor;
    private final StateSetting stateSetting;
    private final int period;

    public DSOIRunnable(ChannelProcessor channelProcessor) {
        this.channelProcessor = channelProcessor;
        period = 60;
        stateSetting = new StateSetting("DSOIRecord.json");
        stateSetting.init();
    }

    public DSOIRunnable(ChannelProcessor channelProcessor, StateSetting stateSetting, int period) {
        this.channelProcessor = channelProcessor;
        this.stateSetting = stateSetting;
        this.period = period;
    }

    @Override
    public void run() {
        LOGGER.info("启动派发单信息采集...");
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
        PreparedStatement ps = conn.prepareStatement("SELECT kid,id,is_processed FROM z_private_dispatch_info WHERE kid > " + kid + " AND is_processed = 0 ORDER BY id ASC");
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
            PreparedStatement ps2 = conn.prepareStatement("SELECT b.dispatch_order distributeOrderId,b.factory_operator distributeOrgId,b.receive_province_code receiveOrgId,b.receive_operator receiveUserId,b.transport_date distributeDate,a.boxcode boxCode,a.ammocode ammoCode  FROM dispatch_item a LEFT JOIN dispatch_info b ON a.dispatch_id = b.id WHERE a.id = ?;");
            ps2.setString(1, rs.getString("id"));
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) {
                JSONObject jo = new JSONObject();
                jo.put("KID", tid);
                jo.put("DISTRIBUTE_ORDER_ID", rs2.getString("distributeOrderId"));
                jo.put("TRANSPORT_ID", "");// 暂无
                jo.put("DISTRIBUTE_ORG_ID", rs2.getString("distributeOrgId"));
                jo.put("RECEIVE_ORG_ID", rs2.getString("receiveOrgId"));
                jo.put("DISTRIBUTE_USER_ID", "");// 暂无
                jo.put("RECEIVE_USER_ID", rs2.getString("receiveUserId"));
                jo.put("DISTRIBUTE_DATE", rs2.getString("distributeDate"));

                jo.put("BOX_CODE", rs2.getString("boxCode"));
                jo.put("AMMO_CODE", rs2.getString("ammoCode"));
                LOGGER.info("采集数据:" + jo);
                SourceUtils.sendEvent(jo.toJSONString(), SourceUtils.createHeader(beginTime, SubBizType.DSOI.name()), channelProcessor);
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
        ResultSet rs = stat.executeQuery("SELECT kid FROM z_private_dispatch_info ORDER BY kid DESC LIMIT 1;");
        long id = 0;
        if (rs.next()) {
            id = rs.getLong(1);
        }
        stateSetting.addRecordMap(id);
        JDBCUtils.free(rs, stat, conn);
    }
}

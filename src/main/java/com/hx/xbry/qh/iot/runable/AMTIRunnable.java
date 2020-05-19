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
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.hx.xbry.bean.Constants.*;

/**
 * @ClassName AMTIRunnable
 * @Description 处理弹药运输资料线程类
 * @Author fmy
 * @Date 2020/5/8 16:26
 * @Version 1.0
 */
public class AMTIRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AMTIRunnable.class);
    private final ChannelProcessor channelProcessor;
    private final StateSetting stateSetting;
    private final int period;

    public AMTIRunnable(ChannelProcessor channelProcessor) {
        this.channelProcessor = channelProcessor;
        period = 60;
        stateSetting = new StateSetting("AMTIRecord.json");
        stateSetting.init();
    }

    public AMTIRunnable(ChannelProcessor channelProcessor, StateSetting stateSetting, int period) {
        this.channelProcessor = channelProcessor;
        this.stateSetting = stateSetting;
        this.period = period;
    }

    @Override
    public void run() {
        LOGGER.info("启动弹药运输信息采集...");
        while (true) {
            try {
                collect();
            } catch (SQLException | IOException | ParseException e) {
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


    private void collect() throws SQLException, IOException, ParseException {
        long id = stateSetting.getRecordMap().get("ID");
        if (id == 0) {
            updateId();
            id = stateSetting.getRecordMap().get("ID");
        }
        Connection conn = JDBCUtils.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT id,longitude,latitude,data_time FROM car_gps WHERE id > " + id + " ORDER BY Id ASC;" );
        ResultSet rs = ps.executeQuery();
        long newId = 0;
        while (rs.next()) {
            Date beginTime = TimeUtils.beijing2world(new Date());
            int tid = rs.getInt(1);
            if (tid > id) {
                newId = tid;
            }
            String date = SDF14.format(TimeUtils.beijing2world(SDF.parse(rs.getString(4))));
            JSONObject resultJO = new JSONObject();
            //FIXME TRANSPORT_ID 未从表中未获得关联单号  2020/5/15 16:32 fmy
            resultJO.put("TRANSPORT_ID", "");
            resultJO.put("LONGITUDE", rs.getString(2));
            resultJO.put("LATITUDE", rs.getString(3));
            resultJO.put("DATA_TIME", SDF.format(SDF14.parse(date)));
            SourceUtils.sendEvent(resultJO.toJSONString(), SourceUtils.createHeader(beginTime, SubBizType.AMTI.name()), channelProcessor);
        }
        if (newId > id) {
            stateSetting.addRecordMap(newId);
        }
        try {
            JDBCUtils.free(rs, ps, conn);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    /**
     * @Description 更新id
     * @Author fmy
     * @Date 2020/1/13 16:32
     * @Param []
     * @Return void
     **/
    private void updateId() throws IOException, SQLException {
        Connection conn = JDBCUtils.getConnection();
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("SELECT id FROM car_gps ORDER BY id DESC LIMIT 1;");
        long id = 0;
        if (rs.next()) {
            id = rs.getLong(1);
        }
        stateSetting.addRecordMap(id);
        JDBCUtils.free(rs, stat, conn);
    }

}

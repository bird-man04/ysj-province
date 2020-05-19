package com.hx.xbry.tools;

import com.hx.xbry.bean.SendType;
import org.nutz.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @ClassName RestfulDataUtils
 * @Description DI、EI处理工具
 * @Author fmy
 * @Date 2019/10/22 11:06
 * @Version 1.0
 */
public class RestfulDataUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestfulDataUtils.class);
    private static int count;//减少DI打印频率

    /**
     * @Description 单条发送
     * @Author fmy
     * @Date 2019/10/22 10:59
     * @Param [obj, sendType]
     * @Return int
     **/
    public static int send(Object obj, SendType sendType) throws IOException {
        return send0(obj, getConnection(sendType));
    }

    /**
     * @Description 批量发送
     * @Author fmy
     * @Date 2019/11/25 14:55
     * @Param [obj, sendType]
     * @Return int
     **/
    public static int batchSend(Object obj, SendType sendType) throws IOException {
        return send0(obj, getBatchConnection(sendType));
    }

    /**
     * @Description 发送DI EI
     * @Author fmy
     * @Date 2019/11/26 10:24
     * @Param [obj, connection]
     * @Return int
     **/
    private static int send0(Object obj, HttpURLConnection connection) {
        HttpURLConnection conn = null;
        OutputStream os = null;
        try {
            conn = connection;
            os = conn.getOutputStream();
            String input = Json.toJson(obj);
            os.write(input.getBytes(StandardCharsets.UTF_8));
            os.flush();
            if (conn.getResponseCode() != 200) {
                LOGGER.error("Failed Send DI or EI:\n" + input);
                LOGGER.error("HTTP error code :" + conn.getResponseCode());
            } else {
                if (count++ % 100 == 0) {
                    LOGGER.info("Successfully Send DI or EI , HTTP code = 200:\n" + input);
                }
            }
        } catch (IOException e) {
            LOGGER.error("", e);
            return -1;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (IOException e) {
                LOGGER.error("Failed Close resource", e);
            }
        }
        return 0;
    }

    /**
     * @Description 获取批量发送DI的HTTP连接
     * @Author fmy
     * @Date 2019/10/22 10:58
     * @Param [sendType] 发送类型，分DI和EI
     * @Return java.net.HttpURLConnection
     **/
    private static HttpURLConnection getBatchConnection(SendType sendType) throws IOException {
        String url;
        if (sendType == SendType.DI) {
            url = PropertiesUtils.getProperty("DI.BATCH.URL");
        } else {
            url = PropertiesUtils.getProperty("EI.BATCH.URL");
        }
        return getHttpURLConnection(url);
    }

    /**
     * @Description 获取单个发送DI的HTTP连接
     * @Author fmy
     * @Date 2019/10/22 10:58
     * @Param [sendType] 发送类型，分DI和EI
     * @Return java.net.HttpURLConnection
     **/
    private static HttpURLConnection getConnection(SendType sendType) throws IOException {
        String url;
        if (sendType == SendType.DI) {
            url = PropertiesUtils.getProperty("DI.URL");

        } else {
            url = PropertiesUtils.getProperty("EI.URL");
        }
        return getHttpURLConnection(url);
    }

    /**
     * @Description 获取连接
     * @Author fmy
     * @Date 2019/11/26 10:24
     * @Param [url]
     * @Return java.net.HttpURLConnection
     **/
    private static HttpURLConnection getHttpURLConnection(String url) throws IOException {
        HttpURLConnection httpConnection = (HttpURLConnection) new URL(url).openConnection();
        httpConnection.setDoOutput(true);
        httpConnection.setRequestMethod("POST");
        httpConnection.setRequestProperty("Content-Type", "application/json");
        return httpConnection;
    }
}


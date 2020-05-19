package com.hx.xbry.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
*@Description HTTP连接工具
*@Author fmy
*@Date 2019/11/7 14:47
*@Param
*@Return
**/
public class HttpUtils {

    public static String sendGet(String url, Map<String, String> parameters) throws IOException {
        StringBuilder result = new StringBuilder();
        HttpURLConnection conn = null;
        BufferedReader in = null;
        try {
            if (parameters.size() < 1) {// 参数不正确，返回空数组
                return "[]";
            }
            StringBuilder sb = new StringBuilder();
            for (String name : parameters.keySet()) {
                // fix 对时间（eg:2019-10-12 09:00:34）进行encode后2019-10-12+09%3A42%3A14,导致接口报错  fmy 2019-10-12
                // sb.append(name).append("=").append(java.net.URLEncoder.encode(parameters.get(name), "UTF-8")).append("&");
                sb.append(name).append("=").append(parameters.get(name)).append("&");
            }
            conn = getGetHttpURLConnection(url + "?" + sb.substring(0, sb.length() - 1));
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return result.toString();
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url   发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) throws IOException {
        StringBuilder result = new StringBuilder();
        OutputStreamWriter out = null;
        BufferedReader in = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.connect();

            out = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8);
            out.write(param);
            out.flush();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result.toString();
    }

    private static HttpURLConnection getGetHttpURLConnection(String fullUrl) throws IOException {
        HttpURLConnection httpConn = (HttpURLConnection) new URL(fullUrl).openConnection();
        httpConn.setRequestProperty("Accept", "*/*");
        httpConn.setRequestProperty("Connection", "Keep-Alive");
        httpConn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
        httpConn.setConnectTimeout(10000);//fix 因为访问时总是超时错误，增加设置超时时间 fmy 2019-10-15
        httpConn.setReadTimeout(10000);
        httpConn.connect();
        return httpConn;
    }
}


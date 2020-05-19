package com.hx.xbry.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static com.hx.xbry.bean.Constants.BUFFER_SIZE;

/**
 * Create by fmy on 2019/9/23 14:54:00
 */
public class IOUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);

    /**
     * @Description 跳过字节数
     * @Author fmy
     * @Date 2019/12/2 11:38
     * @Param [inputStream, size]
     * @Return void
     **/
    private static void skip(InputStream inputStream, long size) throws IOException {
        long skipsize = size;
        long skipedsize;
        while (true) {
            skipedsize = inputStream.skip(skipsize);
            if (skipedsize == size) {
                break;
            }
            skipsize = size - skipedsize;
        }
    }

    /**
     * @Description 读取新增内容
     * @Author fmy
     * @Date 2019/12/2 11:39
     * @Param [inputStream, size]
     * @Return java.lang.String
     **/
    public static String read(InputStream inputStream, long size) throws IOException {
        skip(inputStream, size);
        byte[] buff = new byte[BUFFER_SIZE];
        int len;
        StringBuilder sb = new StringBuilder();
        while ((len = inputStream.read(buff, 0, buff.length)) != -1) {
            sb.append(new String(buff, 0, len));
        }
        return sb.toString();
    }

    /**
    *@Description 读取新增内容
    *@Author fmy
    *@Date 2019/12/17 14:26
    *@Param [file, size]
    *@Return java.lang.String
    **/
    public static String read(File file, long size) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return read(is, size);
        }
    }
}

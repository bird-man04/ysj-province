package com.hx.xbry.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.hx.xbry.bean.Constants.*;

/**
 * @ClassName ZipUtils
 * @Description Zip压缩工具类
 * @Author fmy
 * @Date 2019/12/4 16:47
 * @Version 1.0
 */
public class ZipUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtils.class);

    /**
     * @Description 把文件集合打成zip压缩包
     * @Author fmy
     * @Date 2019/12/4 17:22
     * @Param [srcFiles, zipFile] 压缩文件集合,zip文件名
     * @Return void
     **/
    public static void toZip(List<File> srcFiles, File zipFile) throws RuntimeException {
        long start = System.currentTimeMillis();
        if (zipFile == null) {
            LOGGER.error("压缩包文件名为空！");
            return;
        }
        if (!zipFile.getName().endsWith(".zip")) {
            LOGGER.error("压缩包文件名异常，zipFile={}", zipFile.getPath());
            return;
        }
        if (!zipFile.getParentFile().exists()) {
            if (zipFile.getParentFile().mkdirs()) {
                LOGGER.info("Successfully create dir " + zipFile.getParentFile().getAbsolutePath());
            }
        }
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (File srcFile : srcFiles) {
                byte[] buf = new byte[BUFFER_SIZE];
                zos.putNextEntry(new ZipEntry(srcFile.getName()));
                int len;
                FileInputStream in = new FileInputStream(srcFile);
                while ((len = in.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }
                zos.setComment(srcFile.getName());
                zos.closeEntry();
                in.close();
            }
            long end = System.currentTimeMillis();
            LOGGER.info(zipFile.getName() + "压缩完成，耗时：" + (end - start) + " ms");
        } catch (Exception e) {
            LOGGER.error("ZipUtil toZip exception, ", e);
            throw new RuntimeException("zipFile error from ZipUtils", e);
        }
    }
}

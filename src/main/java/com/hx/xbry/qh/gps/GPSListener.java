package com.hx.xbry.qh.gps;

import com.hx.xbry.tools.FileUtils;
import com.hx.xbry.tools.FtpUtils;
import net.contentobjects.jnotify.JNotifyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @ClassName YdpListener
 * @Description 青海GPS移动探空资料采集Listener
 * @Author fmy
 * @Date 2019/12/16 15:01
 * @Version 1.0
 */
public class GPSListener implements JNotifyListener {

    public static final Logger LOGGER = LoggerFactory.getLogger(GPSListener.class);
    private static final Set<String> fileSet;
    private final boolean isTransferFile;
    private final String saveDir;
    private final boolean isDeleteSourceFile;
    private final boolean isPushFtp;
    private final String uploadPath;

    static {
        fileSet = new HashSet<>();
    }

    GPSListener(boolean isTransferFile, String saveDir, boolean isDeleteSourceFile, boolean isPushFtp, String uploadPath) {
        this.isTransferFile = isTransferFile;
        this.saveDir = saveDir;
        this.isDeleteSourceFile = isDeleteSourceFile;
        this.isPushFtp = isPushFtp;
        this.uploadPath = uploadPath;
    }

    @Override
    public void fileCreated(int i, String s, String s1) {// 只监控新增文件，如果要修改，删除后重传
        String filePath = s + File.separator + s1;
        LOGGER.info(filePath + " Created");
        File file = new File(filePath);
        if (!file.isFile()) {
            return;
        }
        if (!isValidFileName(file.getName())) {
            return;
        }
        //fix create file事件会触发多次，通过暂存文件绝对路径来过滤 fmy 2019-12-23
        if (fileSet.contains(filePath)) {
            return;
        }
        fileSet.add(filePath);
        if (isTransferFile) {
            FileUtils.transfer(file, saveDir + File.separator + file.getParentFile().getName());
        }
        if (isPushFtp) {
            FtpUtils.uploadFile(uploadPath , file.getParentFile().getName() + "_" + file.getName(), filePath);
        }
        if (isDeleteSourceFile) {
            if (file.delete()) {
                LOGGER.info("删除文件" + file.getName() + "成功!");
            }
        }
        fileSet.remove(filePath);
    }

    /**
     * @Description 是否有效文件名
     * @Author fmy
     * @Date 2019/12/10 11:08
     * @Param [name]
     * @Return boolean
     **/
    private static boolean isValidFileName(String name) {
        return name.equals("EDT.tsv") || name.equals("FLEDT.tsv") || name.equals("STD.tsv") || name.equals("FLSTD.tsv")
                || name.equals("FRAWPTU.tsv") || name.equals("GPS_ORB.tsv") || name.equals("GPSCCLOC.tsv")
                || name.equals("GPSCCREM.tsv") || name.equals("GPSDCC_RESULT.tsv") || name.equals("RS92SONDEID.tsv") || name.equals("RSSTATUS.tsv");
    }

    @Override
    public void fileModified(int i, String s, String s1) {
//        LOGGER.info(s + File.separator + s1 + " Modified");
    }

    @Override
    public void fileRenamed(int i, String s, String s1, String s2) {
        LOGGER.info(s + File.separator + s1 + " Renamed");
    }

    @Override
    public void fileDeleted(int i, String s, String s1) {
        LOGGER.info(s + File.separator + s1 + " Deleted");
    }
}

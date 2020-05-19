package com.hx.xbry.tools;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FtpUtils {

    public static final Logger LOGGER = LoggerFactory.getLogger(FtpUtils.class);
    private static String hostname = PropertiesUtils.getProperty("FTP.HOST");
    private static int port = Integer.parseInt(PropertiesUtils.getProperty("FTP.PORT", "21"));
    private static String username = PropertiesUtils.getProperty("FTP.USER");
    private static String password = PropertiesUtils.getProperty("FTP.PWD");

    /**
     * @Description 初始化ftp服务器
     * @Author fmy
     * @Date 2019/12/16 17:33
     * @Param []
     * @Return void
     **/
    private static FTPClient getFtpClient() {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setControlEncoding("utf-8");
        try {
            ftpClient.connect(hostname, port);
            ftpClient.login(username, password);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                LOGGER.info("Connect failed...ftp服务器:" + hostname + ":" + port);
            }
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        return ftpClient;
    }

    /**
     * @Description 上传文件
     * @Author fmy
     * @Date 2019/12/16 17:33
     * @Param [pathname, fileName, fileAbsPath] ftp服务保存地址 上传到ftp的文件名 待上传文件的名称（绝对地址）
     * @Return boolean
     **/
    public static void uploadFile(String pathName, String fileName, String fileAbsPath) {
        FTPClient ftpClient = null;
        try (InputStream inputStream = new FileInputStream(new File(fileAbsPath))) {
            ftpClient = getFtpClient();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            createDirecroty(pathName, ftpClient);
            ftpClient.changeWorkingDirectory(pathName);
            ftpClient.storeFile(fileName, inputStream);
            ftpClient.logout();
            LOGGER.info(fileAbsPath + "上传成功");
        } catch (Exception e) {
            LOGGER.error(fileAbsPath + "上传失败", e);
        } finally {
            if (ftpClient != null && ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    LOGGER.error("Close FTP connection failed ", e);
                }
            }
        }
    }

    /**
     * @Description 改变目录路径
     * @Author fmy
     * @Date 2019/12/16 18:00
     * @Param [ftpClient, directory]
     * @Return boolean
     **/
    private static boolean changeWorkingDirectory(String directory, FTPClient ftpClient) throws IOException {
        return ftpClient.changeWorkingDirectory(directory);
    }

    /**
    *@Description 创建多层目录文件，如果有ftp服务器已存在该文件，则不创建，如果无，则创建
    *@Author fmy
    *@Date 2019/12/16 18:23
    *@Param [remote, ftpClient]
    *@Return void
    **/
    private static void createDirecroty(String remote, FTPClient ftpClient) throws IOException {
        String directory = remote + "/";
        // 如果远程目录不存在，则递归创建远程服务器目录
        if (!directory.equalsIgnoreCase("/") && !changeWorkingDirectory(directory, ftpClient)) {
            int start;
            int end;
            if (directory.startsWith("/")) {
                start = 1;
            } else {
                start = 0;
            }
            end = directory.indexOf("/", start);
            String path = "";
            StringBuilder paths = new StringBuilder();
            do {
                String subDirectory = new String(remote.substring(start, end).getBytes("GBK"), StandardCharsets.ISO_8859_1);
                path = path +"/"+ subDirectory;
                if (!existFile(path, ftpClient)) {
                    if (makeDirectory(subDirectory, ftpClient)) {
                        changeWorkingDirectory(subDirectory, ftpClient);
                    } else {
                        changeWorkingDirectory(subDirectory, ftpClient);
                    }
                } else {
                    changeWorkingDirectory(subDirectory, ftpClient);
                }
                paths.append("/").append(subDirectory);
                start = end + 1;
                end = directory.indexOf("/", start);
                // 检查所有目录是否创建完毕
            } while (end > start);
        }
    }

    /**
     * @Description 判断ftp服务器文件是否存在
     * @Author fmy
     * @Date 2019/12/16 18:11
     * @Param [path, ftpClient]
     * @Return boolean
     **/
    private static boolean existFile(String path, FTPClient ftpClient) throws IOException {
        return ftpClient.listFiles(path).length > 0;
    }

    /**
     * @Description 创建目录
     * @Author fmy
     * @Date 2019/12/16 18:10
     * @Param [dir, ftpClient]
     * @Return boolean
     **/
    private static boolean makeDirectory(String dir, FTPClient ftpClient) throws IOException {
        return ftpClient.makeDirectory(dir);
    }

    /**
     * @Description 下载文件
     * @Author fmy
     * @Date 2019/12/16 18:07
     * @Param [pathName, fileName, localPath] FTP服务器文件目录 文件名称 下载后的文件路径
     * @Return boolean
     **/
    public boolean downloadFile(String pathName, String fileName, String localPath) {
        boolean flag = false;
        FTPClient ftpClient = null;
        try {
            ftpClient = getFtpClient();
            ftpClient.changeWorkingDirectory(pathName);
            FTPFile[] ftpFiles = ftpClient.listFiles();
            for (FTPFile file : ftpFiles) {
                if (fileName.equalsIgnoreCase(file.getName())) {
                    File localFile = new File(localPath + File.separator + file.getName());
                    try (OutputStream os = new FileOutputStream(localFile)) {
                        ftpClient.retrieveFile(file.getName(), os);
                    }
                }
            }
            ftpClient.logout();
            flag = true;
            LOGGER.info(pathName + fileName + "下载成功");
        } catch (Exception e) {
            LOGGER.error(pathName + fileName + "下载失败", e);
        } finally {
            if (ftpClient != null && ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    LOGGER.error("", e);
                }
            }
        }
        return flag;
    }

    /**
     * @Description 删除文件
     * @Author fmy
     * @Date 2019/12/16 18:01
     * @Param [pathName, fileName] FTP服务器保存目录 要删除的文件名称
     * @Return boolean
     **/
    public static boolean deleteFile(String pathName, String fileName) {
        boolean flag = false;
        FTPClient ftpClient = null;
        try {
            ftpClient = getFtpClient();
            ftpClient.changeWorkingDirectory(pathName);
            ftpClient.dele(fileName);
            ftpClient.logout();
            flag = true;
            LOGGER.info(pathName + fileName + "删除成功");
        } catch (Exception e) {
            LOGGER.error(pathName + fileName + "删除失败", e);
        } finally {
            if (ftpClient != null && ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    LOGGER.error("", e);
                }
            }
        }
        return flag;
    }

}
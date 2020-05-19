package com.hx.xbry.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;
import java.util.List;

import static com.hx.xbry.bean.Constants.BUFFER_SIZE;
import static com.hx.xbry.bean.Constants.SDF8;

/**
 * @ClassName FileUtils
 * @Description 文件处理工具类
 * @Author fmy
 * @Date 2019/10/12 16:43
 * @Version 1.0
 **/
public class FileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    /**
     * @Description 写入文件内容
     * @Author fmy
     * @Date 2019/10/23 11:06
     * @Param [file, content]
     * @Return void
     **/
    private static void write(final File file, final String content) throws IOException {
        final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(content);
        bw.flush();
        bw.close();
    }

    /**
     * @Description 清理文件内容
     * @Author fmy
     * @Date 2019/10/23 11:05
     * @Param [fileName]
     * @Return void
     **/
    private static void clearFile(String fileName) throws IOException {
        createIgnoreRepeatedFile(fileName, "");
        try (FileWriter fileWriter = new FileWriter(new File(fileName))) {
            fileWriter.write("");
            fileWriter.flush();
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

    /**
     * @Description 回写文件内容
     * @Author fmy
     * @Date 2019/10/23 11:05
     * @Param [fileName, text]
     * @Return void
     **/
    public static void rewrite(String fileName, String text) throws IOException {
        clearFile(fileName);
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(text);
            fileWriter.flush();
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

    /**
     * @Description 读取文本文件
     * @Author fmy
     * @Date 2019/10/23 11:04
     * @Param [path]
     * @Return java.lang.String
     **/
    public static String readTextFile(String path) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path))) {
            byte[] bys = new byte[1024];
            int len;
            while ((len = bis.read(bys)) != -1) {
                result.append(new String(bys, 0, len));
            }
        }
        return result.toString();
    }

    /**
    *@Description 文件不存在则新建
    *@Author fengmingyang
    *@Date 2020/5/13 16:59
    *@Param [path, initContent]
    *@Return void
    **/
    public static void createIfNotExist(String path, String initContent) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            createIgnoreRepeatedFile(path,initContent);
        }
    }

    /**
     * @Description 创建新文件，文件已存在就替换
     * @Author fmy
     * @Date 2019/10/23 11:03
     * @Param [path, initContent]
     * @Return void
     **/
    public static void createIgnoreRepeatedFile(String path, String initContent) throws IOException {
        File file = new File(path);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            if (parentFile.mkdirs()) {
                LOGGER.info("成功创建目录:" + parentFile.getAbsolutePath());
            }
        }
        if (file.createNewFile()) {
            LOGGER.info("成功创建文件:" + file.getAbsolutePath());
        }
        write(file, initContent);
    }

    /**
     * @Description 创建新文件，文件已存在改名后存储
     * @Author fmy
     * @Date 2020/03/05 11:29
     * @Param [path, initContent]
     * @Return void
     **/
    public static void create(String path, String initContent) throws IOException {
        File file = new File(path);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            if (parentFile.mkdirs()) {
                LOGGER.info("成功创建目录:" + parentFile.getAbsolutePath());
            }
        }
        if (file.exists()) {//如果文件存在，比如abc.txt存在另存为abc(1).txt
            String parentFilePath = parentFile.getAbsolutePath();
            String fileName = file.getName();//区分有扩展名和无扩展名 有扩展名以最后一个.以后的当做扩展名
            File newFile0;
            if (fileName.contains(".")) {
                newFile0 = new File(parentFilePath + File.separator + fileName.substring(0, fileName.lastIndexOf(".")) + "(1)" + fileName.substring(fileName.lastIndexOf(".")));
            } else {
                newFile0 = new File(parentFilePath + File.separator + fileName + "(1)");
            }
            if (newFile0.exists()) {
                int repeatCount = 1;
                while (true) {
                    //取出重复数加1，如果没有就新建，有就继续加一，只有创建成功
                    File newFile;
                    if (fileName.contains(".")) {
                        newFile = new File(parentFilePath + File.separator + fileName.substring(0, fileName.lastIndexOf(".")) + "(" + ++repeatCount + ")" + fileName.substring(fileName.lastIndexOf(".")));
                    } else {
                        newFile = new File(parentFilePath + File.separator + fileName + "(" + ++repeatCount + ")");
                    }
                    if (!newFile.exists()) {
                        if (newFile.createNewFile()) {
                            LOGGER.info("成功创建文件:" + newFile.getAbsolutePath());
                            write(newFile, initContent);
                            break;
                        }
                    }
                }
            } else {
                if (!newFile0.exists()) {
                    if (newFile0.createNewFile()) {
                        LOGGER.info("成功创建文件:" + newFile0.getAbsolutePath());
                        write(newFile0, initContent);
                    }
                }
            }
        } else {
            if (file.createNewFile()) {
                LOGGER.info("成功创建文件:" + file.getAbsolutePath());
                write(file, initContent);
            }
        }
    }

    /**
     * @Description 清理临时目录下的临时文件
     * @Author fmy
     * @Date 2019/10/23 11:02
     * @Param [cleanCycle]
     * @Return void
     **/
    public static void cleanSmallFile(String fileDir, int cleanCycle) {
        FileUtils.deleteTmpFile(fileDir, cleanCycle);
        LOGGER.info("存储目录" + fileDir + "周期清理已完成...");
    }

    /**
     * @Description 清理临时目录下的临时文件
     * @Author fmy
     * @Date 2020/2/27 15:43
     * @Param [fileDir, cleanCycle, fileNameReg]
     * @Return void
     **/
    public static void cleanSmallFile(String fileDir, int cleanCycle, String fileNameReg) {
        FileUtils.deleteTmpFile(fileDir, cleanCycle, fileNameReg);
        LOGGER.info("存储目录：" + fileDir + "周期清理已完成...");
    }

    /**
     * @Description 删除过期临时文件
     * @Author fmy
     * @Date 2019/10/23 11:01
     * @Param [path, interval] 临时文件路径，周期时间（小时）
     * @Return void
     **/
    private static void deleteTmpFile(String path, int interval) {
        File srcFile = new File(path);
        if (srcFile.exists() && srcFile.isDirectory()) {
            File[] files = srcFile.listFiles();
            assert files != null;
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteTmpFile(file.getAbsolutePath(), interval);
                    if (file.delete()) {
                        LOGGER.info("成功删除：" + file.getAbsolutePath());
                    }
                } else {
                    if (new Date().getTime() - file.lastModified() > interval * 60 * 60 * 1000) {
                        if (file.delete()) {
                            LOGGER.info("成功删除：" + file.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    /**
     * @Description 删除过期临时文件
     * @Author fmy
     * @Date 2020/2/27 15:42
     * @Param [path, interval, fileNameReg] 临时文件路径，周期时间（小时）,文件名正则匹配规则
     * @Return void
     **/
    private static void deleteTmpFile(String path, int interval, String fileNameReg) {
        File srcFile = new File(path);
        if (srcFile.exists() && srcFile.isDirectory()) {
            File[] files = srcFile.listFiles();
            assert files != null;
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteTmpFile(file.getAbsolutePath(), interval);
                    if (file.delete()) {
                        LOGGER.info("成功删除：" + file.getName());
                    }
                } else {
                    if (new Date().getTime() - file.lastModified() > interval * 60 * 60 * 1000 && file.getName().endsWith(fileNameReg)) {
                        if (file.delete()) {
                            LOGGER.info("成功删除：" + file.getName());
                        }
                    }
                }
            }
        }
    }

    /**
     * 读取文件内容
     *
     * @param path path
     * @return String
     */
    public static String read(String path) {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(path); BufferedInputStream bis = new BufferedInputStream(fis)) {
            byte[] bys = new byte[1024];
            int len;
            while ((len = bis.read(bys)) != -1) {
                sb.append(new String(bys, 0, len));
            }
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        return sb.toString();
    }

    /**
     * @Description 备份文件
     * @Author fmy
     * @Date 2019/12/4 18:50
     * @Param [files]
     * @Return void
     **/
    public static void dump(List<File> files, String dumpDir) {
        files.forEach(file -> {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        File oFile = new File(dumpDir + File.separator + SDF8.format(new Date()) + File.separator + file.getName());
                        if (!oFile.getParentFile().exists()) {
                            if (oFile.getParentFile().mkdirs()) {
                                LOGGER.error("Successfully create dir " + oFile.getParentFile().getAbsolutePath());
                            }
                        }
                        FileOutputStream fos = new FileOutputStream(oFile);
                        byte[] buf = new byte[BUFFER_SIZE];
                        int len;
                        while ((len = fis.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                        }
                        fos.close();
                    } catch (IOException e) {
                        LOGGER.error("", e);
                    }
                }
        );
    }

    /**
     * @Description 赋值文件至另一目录（该目录已存在则覆盖已有同名文件）
     * @Author fmy
     * @Date 2019/12/10 11:21
     * @Param [file, saveDir]
     * @Return void
     **/
    public static void transfer(File file, String saveDir) {
        if (!file.isFile()) {
            return;
        }
        try (InputStream is = new FileInputStream(file)) {
            if (!new File(saveDir).exists()) {
                if (new File(saveDir).mkdirs()) {
                    LOGGER.info("Successfully create dir" + saveDir);
                }
            }
            File nFile = new File(saveDir + File.separator + file.getName());
            if (!nFile.exists()) {
                if (nFile.createNewFile()) {
                    LOGGER.info("Successfully create file" + nFile.getName());
                }
            }
            OutputStream os = new FileOutputStream(nFile);
            int len;
            byte[] bytes = new byte[BUFFER_SIZE];
            while ((len = is.read(bytes)) != -1) {
                os.write(bytes, 0, len);
            }
            os.flush();
            os.close();
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }
}

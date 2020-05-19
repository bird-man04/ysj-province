package com.hx.xbry.setting;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLDecoder;

/**
 * @ClassName SpecialDataSource
 * @Description 包文件加载类
 * @Author fmy
 * @Date 2019/12/16 14:34
 * @Version 1.0
 */
public class DllLoad {

    private static String pwd;
    private static String libPath;

    static {
        DllLoad.pwd = System.getProperty("user.dir");
        DllLoad.libPath = pwd + File.separator + ".." + File.separator + "library";
    }

    /**
    *@Description 添加lib路径至java包路径
    *@Author fmy
    *@Date 2019/12/16 14:30
    *@Param []
    *@Return void
    **/
    public static void add2JavaLibPath() {
        try {
            String libPath = System.getProperty("java.library.path");
            if (libPath == null || libPath.length() == 0) {
                libPath = "";
            }
            final String[] allPath = libPath.split(System.getProperty("path.separator"));
            DllLoad.libPath = URLDecoder.decode(DllLoad.libPath, "utf-8");
            boolean havePath = false;
            String[] array;
            for (int length = (array = allPath).length, i = 0; i < length; ++i) {
                final String each = array[i];
                if (each.equals(DllLoad.libPath)) {
                    havePath = true;
                    break;
                }
            }
            if (!havePath) {
                addDir(DllLoad.libPath);
            }
        } catch (Throwable e) {
            throw new RuntimeException("load Convert.dll error!", e);
        }
    }

    /**
    *@Description 添加lib文件路径
    *@Author fmy
    *@Date 2019/12/16 14:29
    *@Param [libDir]
    *@Return void
    **/
    private static void addDir(final String libDir) throws IOException {
        try {
            final Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);
            final String[] paths = (String[]) field.get(null);
            for (String path : paths) {
                if (libDir.equals(path)) {
                    return;
                }
            }
            final String[] tmp = new String[paths.length + 1];
            System.arraycopy(paths, 0, tmp, 0, paths.length);
            tmp[paths.length] = libDir;
            field.set(null, tmp);
        } catch (IllegalAccessException e) {
            throw new IOException("Failed to get permissions to set library path");
        } catch (NoSuchFieldException e2) {
            throw new IOException("Failed to get field handle to set library path");
        }
    }

}

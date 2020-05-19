package com.hx.xbry.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName StringUtils
 * @Description 字符串操作工具类
 * @Author fmy
 * @Date 2020/5/9 9:52
 * @Version 1.0
 */
public class StringUtils {

    public static String replaceBlank(String str) {
        return replace(str,"\\s*|\t|\r|\n");
    }

    public static String replaceBlankLine(String str) {
        return replace(str,"[\t\r\n]");
    }

    private static String replace(String str, String regex) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
}

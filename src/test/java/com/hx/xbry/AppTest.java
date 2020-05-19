package com.hx.xbry;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    public void test() {
        File file = new File("\\\\HUAXIN_SERVER21\\share");
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File file1 : files) {
                    System.out.println(file1.getAbsolutePath());
                }
            }
        }
    }

    public void test1() {
        File file = new File("E:\\P_XN002_2017080102.txt");
        try (FileInputStream fis = new FileInputStream(file)){
            int len = 0;
            byte[] bytes = new byte[4096];
            if ((len = fis.read(bytes)) != -1) {
                String res = new String(bytes,0,len);
                System.out.println(res);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

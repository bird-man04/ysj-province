package com.hx.xbry.bean;

import com.hx.xbry.tools.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * @ClassName MyDataSource
 * @Description 数据源类
 * @Author fmy
 * @Date 2020/1/13 17:07
 * @Version 1.0
 */
public class MyDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyDataSource.class);
    private final static String driver = PropertiesUtils.getProperty("DB.DRIVER", "com.mysql.jdbc.Driver");//jdbc driver name
    private static final String url = PropertiesUtils.getProperty("DB.URL");
    private static final String user = PropertiesUtils.getProperty("DB.USER");
    private static final String password = PropertiesUtils.getProperty("DB.PWD");

    private static final int initCount = Integer.parseInt(PropertiesUtils.getProperty("DB.INIT.COUNT", "5"));
    private static final int maxCount = Integer.parseInt(PropertiesUtils.getProperty("DB.MAX.COUNT", "10"));
    private static int realMaxCount = 0;
    private static int currentCount = 0;
    private final LinkedList<Connection> connectionPool = new LinkedList<>();

    static {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            LOGGER.error("",e);
            System.exit(0);
        }
    }

    //只让包内引用，下面会定义一个JDBCUtils去获取Connection连接
    public MyDataSource() {
        for (int i = 0; i < initCount; i++) {
            try {
                this.connectionPool.addLast(this.createConnection());
                currentCount++;
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }
        realMaxCount = initCount;
    }

    /**
     * @Description 新建连接
     * @Author fmy
     * @Date 2020/1/13 17:21
     * @Param []
     * @Return java.sql.Connection
     **/
    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * @Description 获取连接
     * @Author fmy
     * @Date 2020/1/13 17:23
     * @Param []
     * @Return java.sql.Connection
     **/
    public Connection getConnection() throws SQLException {
        synchronized (this.connectionPool) {
            if (this.connectionPool.size() > 0) {
                LOGGER.info("Available connection count:" + currentCount);
                currentCount--;
                return this.connectionPool.removeFirst();
            }
            if (realMaxCount < maxCount) {
                realMaxCount++;
                return this.createConnection();
            }
            throw new SQLException("No connection");
        }
    }

    /**
     * @Description 释放连接
     * @Author fmy
     * @Date 2020/1/13 17:22
     * @Param [conn]
     * @Return void
     **/
    public void free(Connection conn) {
        synchronized (this.connectionPool) {
            this.connectionPool.addLast(conn);
            currentCount++;
        }
    }
}

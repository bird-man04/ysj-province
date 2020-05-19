package com.hx.xbry.tools;

import com.hx.xbry.bean.MyDataSource;

import java.sql.*;

/**
 * @ClassName JDBCUtils
 * @Description JDBC工具类
 * @Author fmy
 * @Date 2020/1/13 17:06
 * @Version 1.0
 */
public class JDBCUtils {

    private static final MyDataSource dataSource = new MyDataSource();

    /**
     * @Description 对外提供的获取连接的方法
     * @Author fmy
     * @Date 2020/1/13 17:21
     * @Param []
     * @Return java.sql.Connection
     **/
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * @Description 对外提供的释放连接的方法
     * @Author fmy
     * @Date 2020/1/13 17:21
     * @Param [rs, st, conn]
     * @Return void
     **/
    public static void free(ResultSet rs, Statement st, Connection conn) throws SQLException {
        if (rs != null) {
            rs.close();
        }
        if (st != null) {
            st.close();
        }
        dataSource.free(conn);
    }

    public static void free(ResultSet rs, Statement st) throws SQLException {
        if (rs != null) {
            rs.close();
        }
        if (st != null) {
            st.close();
        }
    }

    public static void free(Connection conn) {
        dataSource.free(conn);
    }

}

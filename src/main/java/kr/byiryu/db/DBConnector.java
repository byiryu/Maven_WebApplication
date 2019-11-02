package kr.byiryu.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnector {
    private static class Singleton {
        private static final DBConnector instance = new DBConnector();
    }

    public static DBConnector getInstance() {
        return Singleton.instance;
    }

    private Connection conn = null;
    private static final String dbConnectUrl = DBInfo.TYPE + ":" + DBInfo.TOOLS + "://" + DBInfo.DOMAIN + ":" + DBInfo.PORT + "/" + DBInfo.DATABASE + "?autoReconnect=true&useSSL=false&validationQuery=select 1";

    public Connection login() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(dbConnectUrl, DBInfo.ID.toString(), DBInfo.PW.toString());
            // System.out.println("DB 연결 완료");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("DB 연결 실패");
        }
        return conn;
    }
}

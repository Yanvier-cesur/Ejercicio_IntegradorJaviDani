package com.cesur.javGdaniM.data.repositorio;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;
public class DataSourceFactory {
    private static String url = "jdbc:mysql://localhost:3306";
    private static String username = System.getenv("USER") ;
    private static String password = System.getenv("PASSWORD") ;
    private static DataSource dataSource = null;

    public static DataSource getDataSource() {
        if(dataSource == null) {
            if(username == null || password == null) {
                throw new RuntimeException("Username and/or password are mandatory");
            }
            MysqlDataSource ds = new MysqlDataSource();
            ds.setURL(url);
            ds.setUser(username);
            ds.setPassword(password);
            dataSource=ds;
        }
        return dataSource;
    }
}

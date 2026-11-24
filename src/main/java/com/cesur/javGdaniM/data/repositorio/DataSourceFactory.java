package com.cesur.javGdaniM.data.repositorio;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;
public class DataSourceFactory {
    private static final String url =
            "jdbc:mysql://localhost:3306/IntegradorJaviDani?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static String username = ("uma") ;
    private static String password = ("uma") ;
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

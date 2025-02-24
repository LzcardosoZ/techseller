package br.com.techseller.config;

import org.apache.commons.dbcp2.BasicDataSource;
import java.sql.Connection;
import java.sql.SQLException;


public class ConnectionpoolConfig {
    private ConnectionpoolConfig(){getDataSource();}

    private static BasicDataSource dataSource;

    private static BasicDataSource getDataSource(){

        if (dataSource == null){

            dataSource = new BasicDataSource();
            dataSource.setUrl("jdbc:mysql:~/tecsellerdb");
            dataSource.setUsername("Admin");
            dataSource.setPassword("4062*Amparo");
            dataSource.setMinIdle(5);
            dataSource.setMaxIdle(10);
            dataSource.setMaxTotal(50);

            System.out.println("Pool de conexão criado com sucesso");
        }else {
            System.out.println("pool de conexão não foi criado !!!");
        }
        return dataSource;

    }
    public static Connection getConnection() throws SQLException{

        return getDataSource().getConnection();
    }
}

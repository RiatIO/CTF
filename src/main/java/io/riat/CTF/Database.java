package io.riat.CTF;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private final String host, database, username, password;
    private final int port;

    private HikariDataSource dataSource;

    public Database(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;

        openConnection();
    }

    public void openConnection() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?useSSL=false");
        config.setUsername(this.username);
        config.setPassword(this.password);

        config.setMaximumPoolSize(5);

        dataSource = new HikariDataSource(config);
    }


    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}

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
    }

    public Connection openConnection() throws SQLException, ClassNotFoundException {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database);
        config.setUsername(this.username);
        config.setPassword(this.password);

        config.setMinimumIdle(3);
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(500);

        dataSource = new HikariDataSource(config);

        return dataSource.getConnection();
    }


    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }


}

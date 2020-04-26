package io.riat.CTF;

import java.sql.Connection;

public class DatabaseManager {

    private final Database db;
    private final Connection connection;

    public DatabaseManager() {
        db = new Database("localhost", 3306, "ctf_db", "deep", "test");
    }

}

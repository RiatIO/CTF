package io.riat.CTF;

import org.bukkit.entity.Player;

import java.sql.*;

public class DatabaseManager {

    private final Database db;

    public DatabaseManager(Database db) {
        this.db = db;
    }

    public void close() {
        db.closePool();
    }

    public Integer queryPlayerTeam(Player player) {

        try (Connection c = db.getConnection(); PreparedStatement statement = c.prepareStatement("SELECT * FROM users WHERE uuid = ?"))
        {
            statement.setString(1, player.getUniqueId().toString());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    Integer team = (Integer) result.getObject("team");

                    return team;
                }
            }

        } catch (SQLException e) {
            e.getStackTrace();
        }

        return null;
    }

    public boolean queryTeamColor(String color) {
        try (Connection c = db.getConnection(); PreparedStatement statement = c.prepareStatement("SELECT * FROM teams WHERE color = ?"))
        {
            statement.setString(1, color);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }

        } catch (SQLException e) {
            e.getStackTrace();
        }

        return false;
    }

    public boolean inertTeam(Player player, String color) {
        try (Connection c = db.getConnection(); PreparedStatement statement = c.prepareStatement(
                     "INSERT INTO teams(color, score, flag_placed) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, color);
            statement.setInt(2, 0);
            statement.setInt(3, 0);

            if (statement.executeUpdate() > 0) {
                System.out.println("IM HERE PROMISE");
                try (ResultSet rs = statement.getGeneratedKeys()) {
                    rs.next();

                    int teamPK = rs.getInt(1);

                    System.out.println("PK " + teamPK);

                    try (PreparedStatement insertStatement = c.prepareStatement(
                            "UPDATE users SET team = ? WHERE uuid = ?")) {
                        insertStatement.setInt(1, teamPK);
                        insertStatement.setString(2, player.getUniqueId().toString());

                        System.out.println("HERE");
                        return insertStatement.executeUpdate() > 0;
                    }
                }
            }

        } catch (SQLException e) {
            e.getStackTrace();
        }

        return false;
    }

    public boolean insertPlayerToTeam(Integer team, String player) {
        try (Connection c = db.getConnection(); PreparedStatement statement = c.prepareStatement(
                "SELECT * FROM users where name = ?"
        )) {
            statement.setString(1, player);

            try (ResultSet userResult = statement.executeQuery()) {
                int userPK = userResult.getInt(1); // New player ID

                Integer userTeam = (Integer) userResult.getObject("team");

                if (userTeam != null) return false;

                try (PreparedStatement updatePlayerStatement = c.prepareStatement(
                        "UPDATE users SET team = ? WHERE id = ?"
                )) {
                    updatePlayerStatement.setInt(1, team);
                    updatePlayerStatement.setInt(2, userPK);

                    return updatePlayerStatement.executeUpdate() > 0;
                }
            }

        } catch (SQLException e) {
            e.getStackTrace();
        }

        return false;
    }

    public boolean queryInsertPlayer(Player player) {

        try (Connection c = db.getConnection(); PreparedStatement statement = c.prepareStatement(
                "SELECT * FROM users WHERE uuid = ?"
        )) {
            statement.setString(1, player.getUniqueId().toString());

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    try (PreparedStatement insertStatement = c.prepareStatement(
                            "INSERT INTO users (uuid, name) VALUES (?, ?)"
                    )) {
                        insertStatement.setString(1, player.getUniqueId().toString());
                        insertStatement.setString(2, player.getDisplayName());

                        return insertStatement.executeUpdate() > 0;
                    }
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String queryPlayerTeamColor(Player player) {
        try (Connection c = db.getConnection(); PreparedStatement statement = c.prepareStatement(
                "SELECT t.color FROM teams t, users u WHERE u.uuid = ? AND u.team = t.id"
        )) {
            statement.setString(1, player.getUniqueId().toString());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getString(1);
                }
            }

        } catch (SQLException e) {
            e.getStackTrace();
        }

        return null;
    }

    public boolean updateTeamScore(String color, int points) {
        try (Connection c = db.getConnection(); PreparedStatement statement = c.prepareStatement(
                "UPDATE teams SET score = score + ? WHERE color = ?"
        )) {
            statement.setInt(1, points);
            statement.setString(2, color);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.getStackTrace();
        }

        return false;
    }

    public boolean updateTeamScore(Integer team, int points) {

        try (Connection c = db.getConnection(); PreparedStatement statement = c.prepareStatement(
                "SELECT * FROM teams where id = ?"
        )) {
            statement.setInt(1, team);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String color = result.getString("color");

                    return updateTeamScore(color, points);
                }
            }

        } catch (SQLException e) {
            e.getStackTrace();
        }

        return false;
    }
}
package io.riat.CTF.Events;

import io.riat.CTF.ScoreboardManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerAttack implements Listener {

    private Connection connection;
    private ScoreboardManager scoreboardManager;

    public PlayerAttack(Connection connection, ScoreboardManager scoreboardManager) {
        this.connection = connection;
        this.scoreboardManager = scoreboardManager;
    }

    /*
    TODO: Store players in cache (instead of querying the database) - and have an
    maintain a cache of players + teams.
    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e) {
        // Friendly-Fire is off.

        if (e.getDamager() instanceof Player) {
            Player attacker = (Player) e.getDamager();
            Player defender = (Player) e.getEntity();

            //attacker.sendMessage("ATTACKING");
            //defender.sendMessage("GETTING ATTACKED");
        }
    }*/

    @EventHandler
    public void onPlayerKill(EntityDeathEvent e) {
        // Player who killed (so we can get the id and update the team score)
        LivingEntity dead = e.getEntity();
        Player killer = dead.getKiller();

        if (killer == null) return;

        if (dead instanceof Player) {
            Player killed = (Player) dead;

            // Update the team score (check if the player killing has team).
            if (!updateTeamScore(killer, killed)) {
                killer.sendMessage("[CTF] Your not in a team, or something went wrong...");
            }
        }
    }

    private boolean updateTeamScore(Player killer, Player killed) {
        try {

            // Check if the killer and killed is on the same team.
            if (isKillerAndKilledOnSameTeam(killer, killed)) {
                killer.sendMessage("[CTF] You can't get points from killing your teammates, mate");
                return false;
            }

            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM users WHERE uuid = ?"
            );
            statement.setString(1, killer.getUniqueId().toString());
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                Integer color = (Integer) result.getObject("team");

                if (color == null) return false;

                PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE teams SET score = score + 1 WHERE id = ?"
                );
                updateStatement.setInt(1, color);

                if (updateStatement.executeUpdate() > 0) {
                    killer.sendMessage("[CTF] You just scored a point for your team!");
                    scoreboardManager.updateScore(color);
                    return true;
                }
            }


        } catch (SQLException e) {
            e.getStackTrace();
        }

        return false;
    }

    private boolean isKillerAndKilledOnSameTeam(Player killer, Player killed) {
        try {
            // Check if killer and killed is on the same team, if so, dont give any points
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT DISTINCT u1.team FROM users u1, users u2 WHERE u1.uuid = ? AND u2.uuid = ? AND u1.team = u2.team AND u1.id != u2.id"
            );
            statement.setString(1, killer.getUniqueId().toString());
            statement.setString(2, killed.getUniqueId().toString());
            ResultSet result = statement.executeQuery();

            return result.next();

        } catch (SQLException e) {
            e.getStackTrace();
        }

        return false;
    }
}

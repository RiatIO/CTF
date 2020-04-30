package io.riat.CTF.Events;

import io.riat.CTF.DatabaseManager;
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

    private final ScoreboardManager scoreboardManager;
    private final DatabaseManager databaseManager;

    public PlayerAttack(DatabaseManager databaseManager, ScoreboardManager scoreboardManager) {
        this.databaseManager = databaseManager;
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

        Integer team = databaseManager.queryPlayerTeam(killer);

        if (team == null) {
            return false;
        }

        String flagLocation = databaseManager.queryFlagPlaced(team);
        if (flagLocation != null) {
            if (databaseManager.updateTeamScore(team, 1)) {
                killer.sendMessage("[CTF] You just scored a point for your team!");
                scoreboardManager.updateScore(team);
                return true;
            }
        } else {
            killer.sendMessage("[CTF] You didn't score any points! Your team hasn't placed their flag, yet...");
        }

        return false;
    }
}

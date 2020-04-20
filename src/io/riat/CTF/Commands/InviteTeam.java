package io.riat.CTF.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;

public class InviteTeam implements CommandExecutor {

    private Connection connection;

    public InviteTeam(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;



        }

        return false;
    }
}

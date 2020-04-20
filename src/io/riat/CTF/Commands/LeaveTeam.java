package io.riat.CTF.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.Connection;

public class LeaveTeam implements CommandExecutor {

    private Connection connection;

    public LeaveTeam(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        return false;
    }
}

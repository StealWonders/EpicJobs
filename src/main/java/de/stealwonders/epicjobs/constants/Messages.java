package de.stealwonders.epicjobs.constants;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum Messages {

    SPECIFY_JOB("Please specify a job id to continue."),
    JOB_DOESNT_EXIST("This job does not exist. Please check again if you entered the correct id."),
    JOB_NOT_OPEN("This job is not open to be claimed."),
    PLAYER_NO_JOBS("You have no claimed jobs."),
    PLAYER_MORE_JOBS("You have more than one job. Please specify a job it to continue."),
    ANNOUNCE_JOB_TAKEN("%s has claimed job #%s");

    private String message;

    Messages(String message) {
        this.message = message;
    }

    public String toString() {
        return message;
    }

    public String toString(Object... parts) {
        return String.format(message, parts);
    }

    public void send(Player player) {
        player.sendMessage(message);
    }

    public void send(CommandSender sender) {
        sender.sendMessage(message);
    }

    public void send(Player player, String replacement) {
        player.sendMessage(message.replace("%s", replacement));
    }

    public void send(Player player, Object... replacements) {
        player.sendMessage(String.format(message, replacements));
    }

    public void broadcast() {
        Bukkit.broadcastMessage(message);
    }

    public void broadcast(String replacement) {
        Bukkit.broadcastMessage(message.replace("%s", replacement));
    }

    public void broadcast(Object... replacements) {
        Bukkit.broadcastMessage(String.format(message, replacements));
    }
}

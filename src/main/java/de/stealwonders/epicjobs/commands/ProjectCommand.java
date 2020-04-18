package de.stealwonders.epicjobs.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.*;
import de.stealwonders.epicjobs.EpicJobs;
import de.stealwonders.epicjobs.project.Project;
import de.stealwonders.epicjobs.project.ProjectStatus;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.adapter.bukkit.TextAdapter;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.stealwonders.epicjobs.constants.Messages.*;

@CommandAlias("project|projects")
public class ProjectCommand extends BaseCommand {

    private EpicJobs plugin;

    public ProjectCommand(final EpicJobs plugin) {
        this.plugin = plugin;
    }

    @Default
    @HelpCommand
    public void onHelp(final CommandSender commandSender, final CommandHelp commandHelp) {
        commandHelp.showHelp();
    }

    @Subcommand("list")
    public void onList(final CommandSender sender) {
        final List<Project> projects = plugin.getProjectManager().getProjects().stream()
            .filter(project -> project.getProjectStatus().equals(ProjectStatus.ACTIVE))
            .collect(Collectors.toList());
        if (projects.size() >= 1) {
            List<TextComponent> textComponents = new ArrayList<>();
            projects.forEach(project -> {
                TextComponent textComponent = TextComponent.builder(project.getName()).color(TextColor.AQUA)
                    .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click to teleport!")))
                    .clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND, "/project teleport " + project.getName())).build();
                textComponents.add(textComponent);
            });
            TextComponent message = TextComponent.join(TextComponent.of(", ").color(TextColor.GOLD), textComponents);
            sender.sendMessage("");
            TextAdapter.sendComponent(sender, message);
            sender.sendMessage("");
        } else {
            NO_PROJECTS_AVAILABLE.send(sender);
        }
    }

    @Subcommand("list all")
    @CommandPermission("epicjobs.command.project.listall")
    public void onListAll(final CommandSender sender) {
        final List<Project> projects = plugin.getProjectManager().getProjects();
        if (projects.size() >= 1) {
            List<TextComponent> textComponents = new ArrayList<>();
            projects.forEach(project -> {
                TextComponent textComponent = TextComponent.builder()
                    .append(TextComponent.of(project.getName()).color(TextColor.AQUA)
                    .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click to teleport!")))
                    .clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND, "/project teleport " + project.getName())))
                    .append(TextComponent.of(" (" + project.getProjectStatus() + ")").color(TextColor.GOLD))
                    .build();
                textComponents.add(textComponent);
            });
            TextComponent message = TextComponent.join(TextComponent.of(", ").color(TextColor.GOLD), textComponents);
            sender.sendMessage("");
            TextAdapter.sendComponent(sender, message);
            sender.sendMessage("");
        } else {
            NO_PROJECTS_AVAILABLE.send(sender);
        }
    }

    @Subcommand("create")
    @CommandPermission("epicjobs.command.project.create")
    public void onCreate(final Player player, @Single final String name, @Optional final Player leader) {
        EpicJobs.newSharedChain("EpicJobs")
            .syncFirst(() -> {
                if (plugin.getProjectManager().getProjectByName(name) == null) {
                    CREATING_PROJECT.sendActionbar(player, name);
                    return true;
                } else {
                    CANT_CREATE_PROJECT.send(player);
                    return false;
                }
            })
            .abortIf(false)
            .asyncFirst(() -> {
                final Project project = (leader == null) ? plugin.getStorageImplementation().createAndLoadProject(name, player.getUniqueId(), player.getLocation(), ProjectStatus.ACTIVE) : plugin.getStorageImplementation().createAndLoadProject(name, leader.getUniqueId(), leader.getLocation(), ProjectStatus.ACTIVE);
                plugin.getProjectManager().addProject(project);
                return project;
            })
            .syncLast((project) -> {
                final String message = (project == null) ? "§cError while creating project. Please contact an administrator." : SUCCESSFULLY_CREATED_PROJECT.toString(project.getId());
                player.sendMessage(message);
            })
            .execute();
    }

    //todo: implement, finalize & test

    @Subcommand("edit")
    @CommandCompletion("@project *")
    @CommandPermission("epicjobs.command.project.edit")
    public void onEdit(final Player player, final Project project, final String context, final @Optional String option) {
        switch (context.toUpperCase()) {
            case "NAME":
                project.setName(option);
                player.sendMessage("Set name of project to " + option);
                break;
            case "LOCATION":
                project.setLocation(player.getLocation());
                player.sendMessage("The project site has been set to your position");
                break;
            case "LEADER":
                final Player leader = Bukkit.getPlayer(option);
                if (leader != null) {
                    project.setLeader(leader);
                    player.sendMessage("Project leader has been set to " + leader.getName());
                } else {
                    PLAYER_NOT_FOUND.send(player, option);
                }
                break;
            default:
                throw new InvalidCommandArgument();
        }
    }

    @Subcommand("teleport|tp")
    @CommandCompletion("@project")
    public void onTeleport(final Player player, final Project project) {
        project.teleport(player);
    }

    @Subcommand("complete")
    @CommandCompletion("@active-project")
    @CommandPermission("epicjobs.command.project.complete")
    public void onComplete(final Player player, final Project project) {
        EpicJobs.newSharedChain("EpicJobs")
            .syncFirst(() -> {
                if (!project.getProjectStatus().equals(ProjectStatus.COMPLETE)) {
                    project.setProjectStatus(ProjectStatus.COMPLETE);
                    ANNOUNCE_PROJECT_COMPLETION.broadcast(project.getName());
                    return true;
                } else {
                    PROJECT_ALREADY_COMPLETE.send(player);
                    return false;
                }
            })
            .abortIf(false)
            .async(() -> plugin.getStorageImplementation().updateProject(project))
            .execute();
    }

}

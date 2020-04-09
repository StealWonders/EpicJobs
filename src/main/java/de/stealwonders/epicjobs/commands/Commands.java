package de.stealwonders.epicjobs.commands;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.PaperCommandManager;
import de.stealwonders.epicjobs.EpicJobs;
import de.stealwonders.epicjobs.constants.Messages;
import de.stealwonders.epicjobs.job.Job;
import de.stealwonders.epicjobs.project.Project;
import de.stealwonders.epicjobs.project.ProjectStatus;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Commands {

    private PaperCommandManager commandManager;

    public Commands(EpicJobs plugin) {
        this.commandManager = new PaperCommandManager(plugin);
        registerCommandContexts(plugin);
        registerCommandCompletions(plugin);
        registerCommands(plugin);
    }

    //todo put into other class
    private void registerCommandContexts(EpicJobs plugin) {
        commandManager.getCommandContexts().registerContext(Job.class, c -> {
            String number = c.popFirstArg();
            try {
                int id = Integer.parseInt(number);
                Job job = plugin.getJobManager().getJobById(id);
                if (job != null) {
                    return job;
                } else {
                    throw new InvalidCommandArgument(Messages.JOB_DOESNT_EXIST.toString(), false);
                }
            } catch (NumberFormatException e) {
                throw new InvalidCommandArgument(MessageKeys.MUST_BE_A_NUMBER, "{num}", number);
            }
        });

        commandManager.getCommandContexts().registerContext(Project.class, c -> {
            Project project = plugin.getProjectManager().getProjectByName(c.popFirstArg());
            if (project != null) {
                return project;
            } else {
                throw new InvalidCommandArgument(Messages.PROJECT_DOESNT_EXIST.toString(), false);
            }
        });
    }

    private void registerCommandCompletions(EpicJobs plugin) {
        commandManager.getCommandCompletions().registerAsyncCompletion("open-job", c -> {
            List<String> jobs = new ArrayList<>();
            plugin.getJobManager().getOpenJobs().forEach(job -> jobs.add(String.valueOf(job.getId())));
            return jobs;
        });

        commandManager.getCommandCompletions().registerAsyncCompletion("player-job", c -> {
            List<String> jobs = new ArrayList<>();
            Player player = c.getPlayer();
            plugin.getEpicJobsPlayer(player.getUniqueId()).ifPresent(epicJobsPlayer -> epicJobsPlayer.getJobs().forEach(job -> jobs.add(String.valueOf(job.getId()))));
            return jobs;
        });

        commandManager.getCommandCompletions().registerAsyncCompletion("project", c -> {
            List<String> projects = new ArrayList<>();
            plugin.getProjectManager().getProjects().forEach(project -> projects.add(project.getName()));
            return projects;
        });

        commandManager.getCommandCompletions().registerAsyncCompletion("active-project", c -> {
            List<String> projects;
            projects = plugin.getProjectManager().getProjects().stream()
                .filter(project -> project.getProjectStatus() == ProjectStatus.ACTIVE)
                .map(Project::getName)
                .collect(Collectors.toList());
            return projects;
        });
    }

    private void registerCommands(EpicJobs plugin) {
        commandManager.enableUnstableAPI("help");
        commandManager.registerCommand(new JobCommand(plugin));
        commandManager.registerCommand(new ProjectCommand(plugin));
    }

}

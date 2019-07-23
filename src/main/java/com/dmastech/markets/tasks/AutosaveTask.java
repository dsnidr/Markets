package com.dmastech.markets.tasks;

import com.dmastech.markets.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;

public class AutosaveTask implements Runnable {
    private BukkitTask task;

    public void setTask(BukkitTask task) {
        this.task = task;
    }

    public void cancelTask() {
        task.cancel();
    }

    @Override
    public void run() {
        try {
            DataManager.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

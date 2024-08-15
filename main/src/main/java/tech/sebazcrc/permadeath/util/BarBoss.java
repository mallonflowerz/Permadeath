package tech.sebazcrc.permadeath.util;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BarBoss {

    private BossBar bar;

    public void addPlayers(List<Player> players) {
        players.forEach(p -> {
            this.bar.addPlayer(p);
        });
    }

    public BossBar getBasBoss() {
        return this.bar;
    }

    public void createBar(String title, BarColor color, BarStyle style) {
        this.bar = Bukkit.createBossBar(Utils.format(title), color, style);
        this.bar.setVisible(true);
    }

    public void setProgress(double p) {
        this.bar.setProgress(p);
    }

    public void setVisible(boolean v) {
        this.bar.setVisible(v);
    }

}

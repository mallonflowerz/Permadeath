package tech.sebazcrc.permadeath.world.ingame;

import java.time.LocalDate;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.data.DateManager;
import tech.sebazcrc.permadeath.data.PlayerDataManager;

public class DaysInGame {

    private static DaysInGame daysInGame;

    private Main main;
    private long currentDay;

    public DaysInGame() {
        this.main = Main.getInstance();
        this.currentDay = getDayInGame();
    }

    public long getDayInGame() {
        World world = Bukkit.getWorld(main.getConfig().getString("Worlds.MainWorld"));
        long time = world.getFullTime();

        long days = time / 24000;
        days = days + 1;
        // Bukkit.getConsoleSender().sendMessage("Tiempo del juego, osea dia es: " + days + " time: " + time);
        return days;
    }

    public void setRealDayByInGame(long day) {
        LocalDate add = DateManager.getInstance().currentDate.minusDays(day);
        DateManager.getInstance()
                .setNewDate(String.format(add.getYear() + "-%02d-%02d", add.getMonthValue(), add.getDayOfMonth()));

        // Bukkit.getConsoleSender().sendMessage("Día cambiado a: " + day);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pdc reload");
        if (Bukkit.getOnlinePlayers() != null && Bukkit.getOnlinePlayers().size() >= 1) {
            for (OfflinePlayer off : Bukkit.getOfflinePlayers()) {

                if (off == null)
                    return;

                if (off.isBanned())
                    return;

                PlayerDataManager manager = new PlayerDataManager(off.getName(), main);
                manager.setLastDay(DateManager.getInstance().getDay());
            }
        }
    }

    public void tick() {
        if (getDayInGame() != this.currentDay) {
            this.currentDay = getDayInGame();
        }

        if (DateManager.getInstance().getDay() != this.currentDay) {
            setRealDayByInGame(currentDay);
        }
    }

    public static DaysInGame getInstance() {
        if (daysInGame == null)
            daysInGame = new DaysInGame();
        return daysInGame;
    }
}

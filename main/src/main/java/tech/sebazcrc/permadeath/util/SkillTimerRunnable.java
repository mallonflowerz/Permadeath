package tech.sebazcrc.permadeath.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import tech.sebazcrc.permadeath.Main;

public class SkillTimerRunnable extends BukkitRunnable {

    private final Player player;
    private int remainingTime; // Tiempo restante en segundos

    public SkillTimerRunnable(Player player, int durationInSeconds) {
        this.player = player;
        this.remainingTime = durationInSeconds;
    }

    @Override
    public void run() {
        if (remainingTime <= 0) {
            // El tiempo ha terminado, detén el runnable
            this.cancel();
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(ChatColor.RED + "¡Habilidad terminada!"));
            return;
        }

        // Calcula los minutos y segundos restantes
        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;

        // Formatea el tiempo restante
        String timeString = String.format("%02d:%02d", minutes, seconds);

        // Envía el tiempo restante al ActionBar del jugador
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.GREEN + "Tiempo restante de la habilidad: " + timeString));

        // Decrementa el tiempo restante
        remainingTime--;

        // Opcional: Actualiza el ActionBar cada segundo
    }

    public static void startSkillTimer(Player player, Main main, int timeInSeconds) {
        // Crea una nueva instancia del runnable con 5 minutos (300 segundos)
        SkillTimerRunnable skillTimer = new SkillTimerRunnable(player, timeInSeconds);

        // Ejecuta el runnable cada segundo (20 ticks)
        skillTimer.runTaskTimer(main, 0L, 20L);
    }
}

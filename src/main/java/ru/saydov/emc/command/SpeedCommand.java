package ru.saydov.emc.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Исполнитель команды {@code /speed [walk|fly] <1-10>} — устанавливает скорость игрока.
 *
 * <p>Синтаксис:
 * <ul>
 *   <li>{@code /speed <1-10>} — скорость ходьбы (и полёта, если игрок летит)</li>
 *   <li>{@code /speed walk <1-10>} — только скорость ходьбы</li>
 *   <li>{@code /speed fly <1-10>} — только скорость полёта</li>
 * </ul>
 *
 * <p>Входной диапазон 1–10 отображается в Bukkit-диапазон 0.1–1.0 (умножение на 0.1f).
 * Значение 1 соответствует минимальной скорости, 10 — максимальной.
 * Стандартная ходьба — приблизительно уровень 2.
 *
 * <p>Команда доступна только игрокам; консоль получает сообщение об ошибке.
 */
public class SpeedCommand implements CommandExecutor, TabCompleter {

    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 10;

    private static final List<String> TYPES = List.of("walk", "fly");
    private static final List<String> LEVELS = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Команда доступна только игрокам.", NamedTextColor.RED));
            return true;
        }
        if (!sender.hasPermission(Permissions.SPEED)) {
            sender.sendMessage(Component.text("Нет доступа: " + Permissions.SPEED, NamedTextColor.RED));
            return true;
        }

        if (args.length == 1) {
            var level = parseLevel(player, args[0]);
            if (level < 0) return true;
            setWalkSpeed(player, level);
            if (player.isFlying()) {
                setFlySpeed(player, level);
            }
            return true;
        }

        if (args.length == 2) {
            var type = args[0].toLowerCase();
            var level = parseLevel(player, args[1]);
            if (level < 0) return true;
            switch (type) {
                case "walk" -> setWalkSpeed(player, level);
                case "fly" -> setFlySpeed(player, level);
                default -> player.sendMessage(Component.text(
                        "Тип должен быть 'walk' или 'fly'.", NamedTextColor.RED));
            }
            return true;
        }

        player.sendMessage(Component.text(usage(), NamedTextColor.YELLOW));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            var prefix = args[0].toLowerCase();
            var result = new ArrayList<String>();
            TYPES.stream().filter(t -> t.startsWith(prefix)).forEach(result::add);
            LEVELS.stream().filter(l -> l.startsWith(args[0])).forEach(result::add);
            return result;
        }
        if (args.length == 2 && TYPES.contains(args[0].toLowerCase())) {
            var prefix = args[1];
            return LEVELS.stream().filter(l -> l.startsWith(prefix)).toList();
        }
        return List.of();
    }

    private int parseLevel(Player player, String raw) {
        int level;
        try {
            level = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text(
                    "Скорость должна быть числом от " + MIN_LEVEL + " до " + MAX_LEVEL + ".", NamedTextColor.RED));
            return -1;
        }
        if (level < MIN_LEVEL || level > MAX_LEVEL) {
            player.sendMessage(Component.text(
                    "Скорость должна быть от " + MIN_LEVEL + " до " + MAX_LEVEL + ".", NamedTextColor.RED));
            return -1;
        }
        return level;
    }

    private void setWalkSpeed(Player player, int level) {
        player.setWalkSpeed(level * 0.1f);
        player.sendMessage(Component.text("Скорость ходьбы: " + level, NamedTextColor.GREEN));
    }

    private void setFlySpeed(Player player, int level) {
        player.setFlySpeed(level * 0.1f);
        player.sendMessage(Component.text("Скорость полёта: " + level, NamedTextColor.GREEN));
    }

    private String usage() {
        return "/speed [walk|fly] <" + MIN_LEVEL + "-" + MAX_LEVEL + "> — установить скорость";
    }
}

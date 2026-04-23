package ru.saydov.emc.command.handler;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.saydov.emc.command.Permissions;
import ru.saydov.emc.util.ManagedWorldNameCompleter;
import ru.saydov.emc.world.EmptyWorldService;

import java.util.List;

/**
 * Обработчик подкоманды {@code /emc tp <name>} — телепортирует игрока в управляемый мир.
 *
 * <p>Доступна только игрокам — консоли требуется физический объект игрока.
 * Если мир числится управляемым, но не загружен в память, сервис автоматически
 * загрузит его перед телепортацией.
 *
 * <p><b>Автодополнение:</b> предлагает имена только управляемых миров через
 * {@link ManagedWorldNameCompleter}.
 *
 * @see EmptyWorldService#teleport
 */
@RequiredArgsConstructor
public class TeleportSubcommandHandler implements SubcommandHandler {

    private final EmptyWorldService emptyWorldService;

    @Override
    public String name() {
        return "tp";
    }

    @Override
    public String permission() {
        return Permissions.TELEPORT;
    }

    @Override
    public String usage() {
        return "/emc tp <name> — телепортироваться на спавн управляемого мира";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Команда доступна только игрокам.", NamedTextColor.RED));
            return;
        }
        if (!requireArgs(sender, args, 1)) {
            return;
        }
        emptyWorldService.teleport(player, args[0]);
        sender.sendMessage(Component.text("Телепортация в мир: " + args[0], NamedTextColor.GREEN));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return ManagedWorldNameCompleter.complete(emptyWorldService, args);
    }
}

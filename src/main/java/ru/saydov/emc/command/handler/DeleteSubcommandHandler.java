package ru.saydov.emc.command.handler;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import ru.saydov.emc.command.Permissions;
import ru.saydov.emc.util.ManagedWorldNameCompleter;
import ru.saydov.emc.world.EmptyWorldService;

import java.util.List;

/**
 * Обработчик подкоманды {@code /emc delete <name>} — удаляет управляемый мир.
 *
 * <p>Перед удалением все игроки из целевого мира принудительно перемещаются
 * в основной мир сервера. Директория мира рекурсивно удаляется с диска;
 * запись из {@code worlds.yml} стирается.
 *
 * <p><b>Автодополнение:</b> предлагает имена только управляемых миров через
 * {@link ManagedWorldNameCompleter}.
 *
 * @see EmptyWorldService#delete(String)
 */
@RequiredArgsConstructor
public class DeleteSubcommandHandler implements SubcommandHandler {

    private final EmptyWorldService emptyWorldService;

    @Override
    public String name() {
        return "delete";
    }

    @Override
    public String permission() {
        return Permissions.DELETE;
    }

    @Override
    public String usage() {
        return "/emc delete <name> — удалить управляемый мир с диска";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!requireArgs(sender, args, 1)) {
            return;
        }
        emptyWorldService.delete(args[0]);
        sender.sendMessage(Component.text("Мир удалён: " + args[0], NamedTextColor.GREEN));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return ManagedWorldNameCompleter.complete(emptyWorldService, args);
    }
}

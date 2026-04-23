package ru.saydov.emc.command.handler;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import ru.saydov.emc.command.Permissions;
import ru.saydov.emc.world.EmptyWorldService;

/**
 * Обработчик подкоманды {@code /emc list} — выводит список управляемых миров.
 *
 * <p>Если список пуст — отправляется сообщение об этом вместо пустого перечня.
 * Аргументы подкоманды игнорируются.
 *
 * @see EmptyWorldService#listManaged()
 */
@RequiredArgsConstructor
public class ListSubcommandHandler implements SubcommandHandler {

    private final EmptyWorldService emptyWorldService;

    @Override
    public String name() {
        return "list";
    }

    @Override
    public String permission() {
        return Permissions.LIST;
    }

    @Override
    public String usage() {
        return "/emc list — показать все управляемые миры";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        var managed = emptyWorldService.listManaged();
        if (managed.isEmpty()) {
            sender.sendMessage(Component.text("Управляемых миров нет.", NamedTextColor.GRAY));
            return;
        }
        sender.sendMessage(Component.text("Управляемые миры (" + managed.size() + "):", NamedTextColor.GOLD));
        for (var world : managed) {
            sender.sendMessage(Component.text(
                    " - " + world.name() + " [" + world.environment().name() + "]", NamedTextColor.WHITE));
        }
    }
}

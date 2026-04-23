package ru.saydov.emc.command.handler;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import ru.saydov.emc.command.Permissions;
import ru.saydov.emc.world.EmptyWorldService;

/**
 * Обработчик подкоманды {@code /emc load <name>} — берёт существующий мир под управление.
 *
 * <p>Используется для миров, уже присутствующих в директории сервера
 * (импортированы вручную или созданы другим плагином). После загрузки
 * мир включается в список управляемых, получает void-генератор для новых
 * чанков и попадает под действие слушателей отключения физики.
 *
 * @see EmptyWorldService#load(String)
 */
@RequiredArgsConstructor
public class LoadSubcommandHandler implements SubcommandHandler {

    private final EmptyWorldService emptyWorldService;

    @Override
    public String name() {
        return "load";
    }

    @Override
    public String permission() {
        return Permissions.LOAD;
    }

    @Override
    public String usage() {
        return "/emc load <name> — взять существующий мир под управление";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!requireArgs(sender, args, 1)) {
            return;
        }
        emptyWorldService.load(args[0]);
        sender.sendMessage(Component.text("Мир загружен: " + args[0], NamedTextColor.GREEN));
    }
}

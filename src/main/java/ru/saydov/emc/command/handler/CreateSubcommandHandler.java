package ru.saydov.emc.command.handler;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import ru.saydov.emc.command.Permissions;
import ru.saydov.emc.util.WorldNameValidator;
import ru.saydov.emc.world.EmptyWorldService;

/**
 * Обработчик подкоманды {@code /emc create <name>} — создаёт новый пустой мир.
 *
 * <p>Ожидает ровно один аргумент — имя будущего мира. Имя проверяется
 * {@link WorldNameValidator} внутри сервиса; невалидные имена приводят
 * к {@code InvalidWorldNameException}, перехватываемому диспетчером.
 *
 * @see EmptyWorldService#create(String)
 */
@RequiredArgsConstructor
public class CreateSubcommandHandler implements SubcommandHandler {

    private final EmptyWorldService emptyWorldService;

    @Override
    public String name() {
        return "create";
    }

    @Override
    public String permission() {
        return Permissions.CREATE;
    }

    @Override
    public String usage() {
        return "/emc create <name> — создать новый пустой мир";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!requireArgs(sender, args, 1)) {
            return;
        }
        emptyWorldService.create(args[0]);
        sender.sendMessage(Component.text("Мир создан: " + args[0], NamedTextColor.GREEN));
    }
}

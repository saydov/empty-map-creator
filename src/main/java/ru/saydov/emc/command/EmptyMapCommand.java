package ru.saydov.emc.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.saydov.emc.command.handler.SubcommandHandler;
import ru.saydov.emc.exception.InvalidWorldNameException;
import ru.saydov.emc.exception.WorldAlreadyExistsException;
import ru.saydov.emc.exception.WorldDeletionException;
import ru.saydov.emc.exception.WorldNotFoundException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Диспетчер корневой команды {@code /emc} — выбирает обработчик по имени подкоманды.
 *
 * <p>Работает как точка входа, регистрируемая в Bukkit через {@code plugin.yml}.
 * По первому аргументу определяет {@link SubcommandHandler}, проверяет разрешение
 * и делегирует выполнение. Бизнес-исключения из обработчиков перехватываются
 * и переводятся в короткие пользовательские сообщения.
 *
 * <p><b>Табкомплит:</b>
 * <ul>
 *   <li>первый аргумент — имена подкоманд, отфильтрованные по разрешениям отправителя</li>
 *   <li>второй и далее — делегируется в {@link SubcommandHandler#tabComplete}</li>
 * </ul>
 *
 * <p><b>Сохранение порядка:</b> обработчики хранятся в {@link LinkedHashMap},
 * чтобы справка и табкомплит показывали подкоманды в порядке регистрации
 * ({@code create → load → delete → tp → list}). Обычный {@code Map.copyOf}
 * или {@code Collectors.toUnmodifiableMap} используют hash-based layout
 * и дают недетерминированный порядок итерации.
 *
 * <p><b>Обработка ошибок:</b> исключения, брошенные обработчиками, не пробрасываются
 * наверх в Bukkit — они логируются и конвертируются в сообщения отправителю.
 * Это предотвращает появление стектрейсов в чате и консоли клиента.
 *
 * <p><b>Создание:</b> через фабричный метод {@link #of(List)} — он принимает
 * список обработчиков в удобном внешнем формате и сам индексирует их по имени.
 * Прямой конструктор скрыт, чтобы исключить создание с рассинхронизированной Map.
 *
 * @see SubcommandHandler
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EmptyMapCommand implements CommandExecutor, TabCompleter {

    /**
     * Реестр обработчиков подкоманд, индексированный по имени.
     *
     * <p>Заполняется в фабричном методе {@link #of(List)} и далее не изменяется.
     * Тип {@link LinkedHashMap} — для детерминированного порядка итерации
     * в справке и табкомплите.
     */
    private final Map<String, SubcommandHandler> handlers;

    /**
     * Создаёт диспетчер с указанным набором обработчиков.
     *
     * <p>Порядок передачи обработчиков определяет порядок их вывода в справке
     * и табкомплите — {@link LinkedHashMap} сохраняет insertion order.
     * Имена обработчиков должны быть уникальными — при дубликатах merge-функция
     * бросит {@link IllegalStateException}.
     *
     * @param handlers список всех поддерживаемых обработчиков подкоманд
     * @return новый диспетчер с обработчиками, проиндексированными по имени
     */
    public static EmptyMapCommand of(List<SubcommandHandler> handlers) {
        var indexed = handlers.stream().collect(Collectors.toMap(
                SubcommandHandler::name,
                Function.identity(),
                EmptyMapCommand::rejectDuplicate,
                LinkedHashMap::new));
        return new EmptyMapCommand(indexed);
    }

    private static SubcommandHandler rejectDuplicate(SubcommandHandler first, SubcommandHandler second) {
        throw new IllegalStateException("duplicate subcommand: " + first.name());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        var handler = handlers.get(args[0].toLowerCase());
        if (handler == null) {
            sender.sendMessage(Component.text("Неизвестная подкоманда: " + args[0], NamedTextColor.RED));
            return true;
        }
        if (!sender.hasPermission(handler.permission())) {
            sender.sendMessage(Component.text("Нет доступа: " + handler.permission(), NamedTextColor.RED));
            return true;
        }
        var subArgs = Arrays.copyOfRange(args, 1, args.length);
        runSafely(sender, handler, subArgs);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 0) {
            return List.of();
        }
        if (args.length == 1) {
            return completeSubcommandNames(sender, args[0]);
        }
        var handler = handlers.get(args[0].toLowerCase());
        if (handler == null || !sender.hasPermission(handler.permission())) {
            return List.of();
        }
        var subArgs = Arrays.copyOfRange(args, 1, args.length);
        return handler.tabComplete(sender, subArgs);
    }

    private List<String> completeSubcommandNames(CommandSender sender, String rawPrefix) {
        var prefix = rawPrefix.toLowerCase();
        return handlers.values().stream()
                .filter(handler -> sender.hasPermission(handler.permission()))
                .map(SubcommandHandler::name)
                .filter(name -> name.startsWith(prefix))
                .toList();
    }

    private void runSafely(CommandSender sender, SubcommandHandler handler, String[] args) {
        try {
            handler.execute(sender, args);
        } catch (InvalidWorldNameException e) {
            sender.sendMessage(Component.text("Некорректное имя мира: " + e.getMessage(), NamedTextColor.RED));
        } catch (WorldNotFoundException e) {
            sender.sendMessage(Component.text("Мир не найден: " + e.getMessage(), NamedTextColor.RED));
        } catch (WorldAlreadyExistsException e) {
            sender.sendMessage(Component.text("Мир уже существует: " + e.getMessage(), NamedTextColor.RED));
        } catch (WorldDeletionException e) {
            sender.sendMessage(Component.text("Не удалось удалить мир: " + e.getMessage(), NamedTextColor.RED));
            log.error("World deletion failed: handler={}", handler.name(), e);
        } catch (RuntimeException e) {
            sender.sendMessage(Component.text("Неожиданная ошибка. Подробности в консоли.", NamedTextColor.RED));
            log.error("Unhandled command error: handler={}", handler.name(), e);
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("Команды плагина empty-map-creator:", NamedTextColor.GOLD));
        for (var handler : handlers.values()) {
            if (!sender.hasPermission(handler.permission())) {
                continue;
            }
            sender.sendMessage(Component.text("  " + handler.usage(), NamedTextColor.WHITE));
        }
    }
}

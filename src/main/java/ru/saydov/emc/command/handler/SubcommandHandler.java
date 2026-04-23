package ru.saydov.emc.command.handler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Обработчик одной подкоманды плагина.
 *
 * <p>Каждая реализация отвечает за одну подкоманду верхнего уровня
 * ({@code create}, {@code delete}, {@code load}, {@code tp}, {@code list}).
 * Диспетчеризация — в {@code EmptyMapCommand}, который выбирает обработчик
 * по имени первого аргумента пользователя.
 *
 * <p><b>Контракт:</b> {@link #name()} — ключ диспетчера, {@link #permission()} —
 * проверяется до {@link #execute}, {@link #usage()} — подсказка при ошибке.
 * Метод {@link #tabComplete} по умолчанию пуст; {@link #requireArgs} — хелпер
 * для единообразной проверки числа аргументов.
 */
public interface SubcommandHandler {

    /**
     * Возвращает имя подкоманды (нижний регистр, без пробелов).
     *
     * <p>Имена подкоманд должны быть уникальными в пределах плагина —
     * диспетчер индексирует обработчики по этому значению.
     *
     * @return имя, по которому диспетчер находит обработчик
     */
    String name();

    /**
     * Возвращает разрешение, требуемое для выполнения подкоманды.
     *
     * <p>Диспетчер вызывает {@link CommandSender#hasPermission} до {@link #execute}
     * и при отказе отправляет стандартное сообщение. Константы разрешений
     * сгруппированы в {@link ru.saydov.emc.command.Permissions}.
     *
     * @return строка разрешения в формате {@code emc.<action>}
     */
    String permission();

    /**
     * Возвращает строку-подсказку по использованию подкоманды.
     *
     * <p>Выводится при ошибке аргументов. Формат: {@code /emc <name> <args> — описание}.
     *
     * @return строка использования для отображения пользователю
     */
    String usage();

    /**
     * Выполняет подкоманду от имени указанного отправителя.
     *
     * <p>К моменту вызова разрешение проверено диспетчером. Бизнес-исключения
     * ({@code WorldNotFoundException} и т.п.) обрабатываются диспетчером
     * и преобразуются в пользовательские сообщения.
     *
     * @param sender отправитель команды — игрок или консоль
     * @param args   аргументы подкоманды, <b>без</b> имени самой подкоманды
     */
    void execute(CommandSender sender, String[] args);

    /**
     * Возвращает предложения автодополнения для текущего ввода.
     *
     * <p>По умолчанию — пустой список. Реализации с автодополнением
     * (например, по именам управляемых миров) переопределяют метод.
     *
     * @param sender отправитель команды
     * @param args   аргументы, включая незавершённый последний токен
     * @return список кандидатов; пустой, если автодополнение не поддерживается
     */
    default List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    /**
     * Проверяет, что передано ровно {@code expected} аргументов; при несовпадении
     * отправляет отправителю {@link #usage()} и возвращает {@code false}.
     *
     * <p>Используется в начале {@link #execute} как guard clause:
     * {@code if (!requireArgs(sender, args, 1)) return;}.
     *
     * @param sender   отправитель команды
     * @param args     переданные аргументы
     * @param expected ожидаемое число аргументов
     * @return {@code true}, если число аргументов совпадает
     */
    default boolean requireArgs(CommandSender sender, String[] args, int expected) {
        if (args.length == expected) {
            return true;
        }
        sender.sendMessage(Component.text(usage(), NamedTextColor.YELLOW));
        return false;
    }
}

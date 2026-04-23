package ru.saydov.emc.util;

import lombok.experimental.UtilityClass;
import ru.saydov.emc.world.EmptyWorldService;
import ru.saydov.emc.world.ManagedWorld;

import java.util.List;

/**
 * Общий автокомплит для подкоманд, принимающих имя управляемого мира.
 *
 * <p>Инкапсулирует идентичную логику табкомплита, ранее продублированную
 * в {@code DeleteSubcommandHandler} и {@code TeleportSubcommandHandler}:
 * фильтрация списка управляемых миров по префиксу, введённому игроком.
 *
 * <p><b>Контракт:</b> предложения возвращаются только если ожидается ровно
 * один аргумент (имя мира). В остальных случаях возвращается пустой список —
 * это сигнал диспетчеру о том, что табкомплит в текущей позиции не поддерживается.
 *
 * @see EmptyWorldService#listManaged()
 * @see ManagedWorld
 */
@UtilityClass
public class ManagedWorldNameCompleter {

    private static final int SINGLE_NAME_ARGUMENT = 1;

    /**
     * Возвращает имена управляемых миров, начинающиеся с введённого префикса.
     *
     * <p>Сравнение регистронезависимое — префикс и имена нормализуются к нижнему
     * регистру перед сравнением. Пустой префикс возвращает полный список управляемых миров.
     *
     * @param service источник списка управляемых миров
     * @param args    аргументы подкоманды, где последний — вводимое имя
     * @return отфильтрованный список имён; пустой, если ожидается не один аргумент
     */
    public static List<String> complete(EmptyWorldService service, String[] args) {
        if (args.length != SINGLE_NAME_ARGUMENT) {
            return List.of();
        }
        var prefix = args[0].toLowerCase();
        return service.listManaged().stream()
                .map(ManagedWorld::name)
                .filter(name -> name.toLowerCase().startsWith(prefix))
                .toList();
    }
}

package ru.saydov.emc.util;

import lombok.experimental.UtilityClass;
import ru.saydov.emc.exception.WorldDeletionException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Рекурсивное удаление директории мира с файловой системы сервера.
 *
 * <p>Выделено в отдельный утилитный класс, чтобы изолировать работу с I/O
 * от бизнес-логики {@code EmptyWorldService}. Следует принципу SRP:
 * сервис знает про Bukkit-операции, утилита — про файловую систему.
 *
 * <p><b>Идемпотентность:</b> если директория не существует, удаление
 * молча завершается успехом — это ожидаемое состояние, если мир уже
 * был выгружен вручную или никогда не существовал на диске.
 *
 * <p><b>Ошибки:</b> любые ошибки I/O оборачиваются в {@link WorldDeletionException}
 * с указанием конкретного пути — checked exception не протекает наружу
 * (раздел 31 STYLE_GUIDE).
 *
 * <p><b>Защита от выхода за пределы контейнера:</b> удаление разрешено только
 * для путей, находящихся строго внутри {@code expectedParent} — сам родитель
 * удалить нельзя. Защищает от ошибок вызывающего кода и от случаев, когда
 * валидация имени была обойдена выше по стеку.
 *
 * @see WorldDeletionException
 */
@UtilityClass
public class WorldDirectoryDeleter {

    /**
     * Рекурсивно удаляет директорию вместе со всем содержимым.
     *
     * <p>Обход выполняется в обратном порядке ({@link Comparator#reverseOrder()}),
     * чтобы сначала удалить файлы и вложенные директории, а затем сами родительские
     * директории — иначе {@link Files#delete} не сработает на непустой директории.
     *
     * <p>Перед удалением {@code root} приводится к абсолютному нормализованному виду
     * и проверяется, что он строго вложен в {@code expectedParent}. При совпадении
     * с родителем или выходе за его пределы бросается {@link WorldDeletionException}.
     *
     * @param root           корневая директория удаляемого мира
     * @param expectedParent ожидаемая родительская директория (например, {@code world container})
     * @throws WorldDeletionException если путь вне {@code expectedParent}
     *                                или обход/удаление файла не удались
     */
    public static void delete(Path root, Path expectedParent) {
        var normalizedRoot = root.toAbsolutePath().normalize();
        var normalizedParent = expectedParent.toAbsolutePath().normalize();
        if (normalizedRoot.equals(normalizedParent) || !normalizedRoot.startsWith(normalizedParent)) {
            throw new WorldDeletionException("refuse to delete outside container: " + normalizedRoot);
        }
        if (!Files.exists(normalizedRoot)) {
            return;
        }
        try (var walk = Files.walk(normalizedRoot)) {
            walk.sorted(Comparator.reverseOrder()).forEach(WorldDirectoryDeleter::deleteSingle);
        } catch (IOException e) {
            throw new WorldDeletionException("walk: " + normalizedRoot, e);
        }
    }

    private static void deleteSingle(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new WorldDeletionException(path.toString(), e);
        }
    }
}

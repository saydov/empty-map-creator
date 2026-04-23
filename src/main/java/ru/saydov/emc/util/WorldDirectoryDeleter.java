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
     * @param root корневая директория удаляемого мира
     * @throws WorldDeletionException если обход или удаление файла не удались
     */
    public static void delete(Path root) {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder()).forEach(WorldDirectoryDeleter::deleteSingle);
        } catch (IOException e) {
            throw new WorldDeletionException("walk: " + root, e);
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

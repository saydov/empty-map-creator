package ru.saydov.emc.world;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.bukkit.World;

/**
 * Описание управляемого плагином пустого мира.
 *
 * <p>Хранит минимальный набор параметров, необходимых для восстановления мира
 * при перезапуске сервера: имя, окружение ({@link World.Environment}) и seed.
 * Сам объект мира ({@link World}) не ссылается — сервер пересоздаёт его
 * при каждом запуске через {@code Bukkit.createWorld}.
 *
 * <p><b>Равенство:</b> два описания считаются равными, если совпадают их имена —
 * имя является первичным ключом в хранилище управляемых миров. Остальные поля
 * ({@link #environment}, {@link #seed}) — сопутствующие метаданные.
 *
 * <p><b>Иммутабельность:</b> все поля {@code private final}, класс не предоставляет
 * сеттеров. Модификация выполняется через {@link #toBuilder()} — возвращает
 * новый builder с текущими значениями, допускающий точечные правки.
 *
 * @see EmptyWorldService
 */
@Getter
@Builder(toBuilder = true)
@ToString
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ManagedWorld {

    /**
     * Имя мира — первичный ключ.
     *
     * <p>Соответствует имени директории мира на файловой системе сервера
     * и используется как уникальный идентификатор в {@code Bukkit.getWorld(String)}.
     */
    @EqualsAndHashCode.Include
    private final String name;

    /**
     * Окружение мира — {@link World.Environment#NORMAL} по умолчанию.
     *
     * <p>Определяет, какие механики и базовые свойства применяются к миру
     * при создании: обычный, ад или Энд. На физику влияния не оказывает —
     * физика отключена слушателями.
     */
    @Builder.Default
    private final World.Environment environment = World.Environment.NORMAL;

    /**
     * Seed мира, используемый при первичной генерации.
     *
     * <p>Для пустого мира на практике не имеет значения — ландшафт не генерируется.
     * Сохраняется для совместимости с {@code WorldCreator} и воспроизводимости.
     */
    @Builder.Default
    private final long seed = 0L;
}

package ru.saydov.emc.world.spi;

import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Генератор чанков, создающий полностью пустой (void) мир.
 *
 * <p>Отключает все этапы ванильной генерации через {@code shouldGenerate*}-методы
 * и переопределяет {@link #getFixedSpawnLocation} с {@link #getBaseHeight},
 * чтобы сервер получил осмысленную высоту для пустого мира.
 *
 * <p><b>Существующие чанки:</b> если мир уже содержит сохранённые чанки на диске,
 * они загружаются как есть — генератор применяется только к новым чанкам,
 * к которым сервер обращается впервые.
 *
 * <p><b>Потокобезопасность:</b> класс не содержит изменяемого состояния,
 * все методы безопасны для одновременного вызова из нескольких потоков,
 * как того требует контракт {@link ChunkGenerator}. Один экземпляр переиспользуется
 * для всех управляемых миров — создаётся один раз при инициализации плагина.
 *
 * @see ChunkGenerator
 */
public class VoidChunkGenerator extends ChunkGenerator {

    /**
     * Высота точки спавна в пустом мире.
     *
     * <p>Публичная константа, используемая сервисом для размещения
     * телепортируемых игроков на той же высоте, что и встроенный спавн
     * генератора. Значение подобрано так, чтобы игрок не оказался
     * в абсолютной пустоте — соответствует типичному уровню поверхности.
     */
    public static final int DEFAULT_SPAWN_HEIGHT = 64;

    @Override
    public boolean shouldGenerateNoise() {
        return false;
    }

    @Override
    public boolean shouldGenerateSurface() {
        return false;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return false;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }

    /**
     * Возвращает фиксированную точку спавна на центре мира.
     *
     * <p>Координата Y ({@link #DEFAULT_SPAWN_HEIGHT}) подобрана так, чтобы игрок
     * не оказался в абсолютной пустоте — она соответствует типичному уровню
     * поверхности в ванильных мирах.
     *
     * @param world  информация о мире, для которого вычисляется спавн
     * @param random источник случайности; не используется — спавн детерминирован
     * @return точка спавна в указанном мире
     */
    @Override
    public @Nullable Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0.5, DEFAULT_SPAWN_HEIGHT, 0.5);
    }

    /**
     * Возвращает базовую высоту ландшафта, равную {@link #DEFAULT_SPAWN_HEIGHT}.
     *
     * <p>Значение используется серверными компонентами, которым нужна оценка
     * высоты без фактической генерации чанка — например, для предварительного
     * выбора точки спавна.
     *
     * @param worldInfo информация о мире
     * @param random    источник случайности; не используется
     * @param x         координата X запрашиваемой точки
     * @param z         координата Z запрашиваемой точки
     * @param heightMap тип запрашиваемой карты высот
     * @return постоянная базовая высота, равная {@value #DEFAULT_SPAWN_HEIGHT}
     */
    @Override
    public int getBaseHeight(WorldInfo worldInfo,
                             Random random,
                             int x,
                             int z,
                             HeightMap heightMap) {
        return DEFAULT_SPAWN_HEIGHT;
    }
}

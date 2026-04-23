package ru.saydov.emc.world.spi;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import ru.saydov.emc.world.ManagedWorld;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Хранилище описаний управляемых миров в YAML-файле.
 *
 * <p>Сохраняет список управляемых миров в файле {@code worlds.yml} внутри
 * директории данных плагина. Формат файла — плоский список записей,
 * каждая из которых содержит имя, окружение и seed:
 *
 * <pre>{@code
 * worlds:
 *   arena:
 *     environment: NORMAL
 *     seed: 0
 *   lobby:
 *     environment: NORMAL
 *     seed: 12345
 * }</pre>
 *
 * <p><b>Потокобезопасность:</b> класс рассчитан на однопоточный доступ
 * из главного серверного потока. Синхронизация снаружи не требуется,
 * но и не обеспечивается — параллельные модификации приведут к гонке.
 *
 * <p><b>Целостность:</b> при каждом изменении состояние полностью
 * перезаписывается в файл. Частичное сохранение не поддерживается —
 * это упрощает логику и исключает расхождение памяти с диском.
 *
 * @see ManagedWorld
 * @see BukkitEmptyWorldService
 */
@Slf4j
@RequiredArgsConstructor
public class YamlManagedWorldStorage {

    private static final String WORLDS_SECTION = "worlds";
    private static final String ENVIRONMENT_KEY = "environment";
    private static final String SEED_KEY = "seed";

    /**
     * Файл, в котором хранится YAML-представление списка миров.
     *
     * <p>Создаётся в директории данных плагина при первом сохранении.
     * До этого момента файл может не существовать — {@link #loadAll()}
     * корректно обрабатывает его отсутствие, возвращая пустой список.
     */
    private final File storageFile;

    /**
     * In-memory кэш управляемых миров, ключ — имя мира.
     *
     * <p>{@link LinkedHashMap} выбрана для сохранения порядка добавления,
     * чтобы {@code listManaged} возвращал миры в порядке создания.
     * Заполняется один раз при {@link #loadAll()}, далее синхронизируется
     * с диском через {@link #save(ManagedWorld)} / {@link #remove(String)}.
     */
    private final Map<String, ManagedWorld> cache = new LinkedHashMap<>();

    private static Optional<ManagedWorld> readWorld(String name, @Nullable String environmentName, long seed) {
        if (environmentName == null) {
            log.warn("Skipping world with missing environment: {}", name);
            return Optional.empty();
        }
        World.Environment environment;
        try {
            environment = World.Environment.valueOf(environmentName);
        } catch (IllegalArgumentException e) {
            log.warn("Skipping world with invalid environment: name={}, env={}", name, environmentName);
            return Optional.empty();
        }
        return Optional.of(ManagedWorld.builder()
                .name(name)
                .environment(environment)
                .seed(seed)
                .build());
    }

    @SneakyThrows({IOException.class})
    private void flush() {
        var yaml = new YamlConfiguration();
        for (var entry : cache.entrySet()) {
            var path = WORLDS_SECTION + "." + entry.getKey();
            yaml.set(path + "." + ENVIRONMENT_KEY, entry.getValue().environment().name());
            yaml.set(path + "." + SEED_KEY, entry.getValue().seed());
        }
        var parent = storageFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            log.warn("Failed to create storage directory: {}", parent);
        }
        yaml.save(storageFile);
    }

    /**
     * Загружает все ранее сохранённые миры из YAML-файла в кэш.
     *
     * <p>Если файл не существует (первый запуск плагина) — метод завершается
     * без ошибок, кэш остаётся пустым. Если файл повреждён или содержит
     * некорректные значения — невалидные записи пропускаются с предупреждением
     * в логе, валидные загружаются как обычно.
     *
     * <p>Метод следует вызывать один раз при инициализации сервиса.
     * Повторный вызов полностью перезапишет кэш содержимым с диска.
     *
     * @return снимок загруженных миров в порядке, в котором они присутствуют в файле
     */
    public List<ManagedWorld> loadAll() {
        cache.clear();
        if (!storageFile.exists()) {
            return List.of();
        }
        var yaml = YamlConfiguration.loadConfiguration(storageFile);
        var section = yaml.getConfigurationSection(WORLDS_SECTION);
        if (section == null) {
            return List.of();
        }
        for (var worldName : section.getKeys(false)) {
            var worldSection = section.getConfigurationSection(worldName);
            if (worldSection == null) {
                continue;
            }
            readWorld(worldName, worldSection.getString(ENVIRONMENT_KEY), worldSection.getLong(SEED_KEY, 0L))
                    .ifPresent(world -> cache.put(worldName, world));
        }
        return List.copyOf(cache.values());
    }

    /**
     * Сохраняет описание мира в кэш и немедленно записывает изменения на диск.
     *
     * <p>Если мир с таким именем уже существует — запись перезаписывается.
     * Порядок в {@link LinkedHashMap} сохраняется для существующих ключей,
     * новые ключи добавляются в конец.
     *
     * @param managedWorld описание мира для сохранения
     */
    public void save(ManagedWorld managedWorld) {
        cache.put(managedWorld.name(), managedWorld);
        flush();
    }

    /**
     * Удаляет описание мира из кэша и записывает изменения на диск.
     *
     * <p>Если мир с указанным именем не существует — метод ничего не делает
     * и возвращает {@code false}.
     *
     * @param name имя удаляемого мира
     * @return {@code true}, если запись существовала и была удалена
     */
    public boolean remove(String name) {
        var removed = cache.remove(name);
        if (removed == null) {
            return false;
        }
        flush();
        return true;
    }

    /**
     * Ищет описание мира в кэше по имени.
     *
     * <p>Не обращается к диску — работает только с in-memory состоянием,
     * загруженным ранее {@link #loadAll()} и обновляемым
     * через {@link #save(ManagedWorld)} / {@link #remove(String)}.
     *
     * @param name имя искомого мира; {@code null} трактуется как пустой результат
     * @return описание мира, если оно присутствует в кэше
     */
    public Optional<ManagedWorld> find(@Nullable String name) {
        if (name == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(cache.get(name));
    }

    /**
     * Возвращает снимок всех управляемых миров из кэша.
     *
     * <p>Возвращаемый список — защитная копия (через {@link List#copyOf});
     * изменения внутреннего состояния после вызова не отражаются в результате.
     *
     * @return неизменяемый список управляемых миров в порядке добавления
     */
    public List<ManagedWorld> all() {
        return List.copyOf(cache.values());
    }
}

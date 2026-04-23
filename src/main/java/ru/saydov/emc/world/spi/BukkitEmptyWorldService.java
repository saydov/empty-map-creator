package ru.saydov.emc.world.spi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.saydov.emc.exception.InvalidWorldNameException;
import ru.saydov.emc.exception.WorldAlreadyExistsException;
import ru.saydov.emc.exception.WorldDeletionException;
import ru.saydov.emc.exception.WorldNotFoundException;
import ru.saydov.emc.util.WorldDirectoryDeleter;
import ru.saydov.emc.util.WorldNameValidator;
import ru.saydov.emc.world.EmptyWorldService;
import ru.saydov.emc.world.ManagedWorld;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Bukkit-реализация {@link EmptyWorldService}, использующая {@link WorldCreator} и {@link VoidChunkGenerator}.
 *
 * <p>Создание и загрузка миров выполняются через стандартные Bukkit-API.
 * Список управляемых миров сохраняется в {@link YamlManagedWorldStorage}.
 * Правила тишины применяются через {@link EmptyWorldRules#applyTo(World)} —
 * единая точка, используемая также слушателем {@code WorldLoadEvent}.
 *
 * <p><b>Разделение ответственности:</b> сервис оркестрирует Bukkit-операции,
 * делегируя отдельные заботы специализированным утилитам:
 * <ul>
 *   <li>валидация имени — {@link WorldNameValidator}</li>
 *   <li>удаление директории мира — {@link WorldDirectoryDeleter}</li>
 *   <li>применение игровых правил — {@link EmptyWorldRules}</li>
 *   <li>персистенция описаний — {@link YamlManagedWorldStorage}</li>
 * </ul>
 *
 * <p><b>Поток выполнения:</b> методы, изменяющие состояние мира, обязаны вызываться
 * из главного серверного потока — это требование самого Bukkit API, не плагина.
 *
 * @see EmptyWorldService
 * @see VoidChunkGenerator
 * @see EmptyWorldRules
 */
@Slf4j
@RequiredArgsConstructor
public class BukkitEmptyWorldService implements EmptyWorldService {

    /**
     * Маркерный файл Minecraft-мира в корне директории.
     *
     * <p>Используется в {@link #load(String)} для отличия настоящего
     * сохранённого мира от случайной одноимённой папки в контейнере
     * ({@code plugins/}, {@code logs/}, {@code cache/} и т.п.) —
     * без этой проверки Bukkit начнёт писать region-файлы внутрь
     * произвольного каталога сервера.
     */
    private static final String LEVEL_MARKER = "level.dat";

    private final YamlManagedWorldStorage storage;
    private final File serverRoot;
    private final VoidChunkGenerator voidChunkGenerator;

    private static boolean looksLikeWorld(File directory) {
        return new File(directory, LEVEL_MARKER).isFile();
    }

    private static WorldCreator newEmptyCreator(ManagedWorld managedWorld, VoidChunkGenerator generator) {
        return new WorldCreator(managedWorld.name())
                .environment(managedWorld.environment())
                .generator(generator)
                .generateStructures(false)
                .seed(managedWorld.seed());
    }

    private static ManagedWorld defaultDescriptor(String name) {
        return ManagedWorld.builder().name(name).build();
    }

    private static Location defaultSpawn(World world) {
        return new Location(world, 0.5, VoidChunkGenerator.DEFAULT_SPAWN_HEIGHT, 0.5);
    }

    private static void evacuate(World from, World destination) {
        var spawn = destination.getSpawnLocation();
        from.getPlayers().forEach(player -> player.teleport(spawn));
    }

    private World openInBukkit(ManagedWorld descriptor) {
        var world = Bukkit.createWorld(newEmptyCreator(descriptor, voidChunkGenerator));
        if (world == null) {
            throw new WorldNotFoundException(descriptor.name());
        }
        EmptyWorldRules.applyTo(world);
        return world;
    }

    private File worldDirectoryOf(String name) {
        var root = serverRoot.toPath().toAbsolutePath().normalize();
        Path resolved = root.resolve(name).normalize();
        if (!resolved.startsWith(root) || resolved.equals(root)) {
            throw new InvalidWorldNameException(name);
        }
        return resolved.toFile();
    }

    private boolean existsAnywhere(String name) {
        return storage.find(name).isPresent()
                || Bukkit.getWorld(name) != null
                || worldDirectoryOf(name).exists();
    }

    private void unloadIfLoaded(String name) {
        var world = Bukkit.getWorld(name);
        if (world == null) {
            return;
        }
        evacuate(world, Bukkit.getWorlds().getFirst());
        if (!Bukkit.unloadWorld(world, false)) {
            throw new WorldDeletionException("unload: " + name);
        }
    }

    private void reloadOne(ManagedWorld descriptor) {
        var existing = Bukkit.getWorld(descriptor.name());
        if (existing != null) {
            EmptyWorldRules.applyTo(existing);
            return;
        }
        if (!worldDirectoryOf(descriptor.name()).exists()) {
            log.warn("Managed world missing on disk, skipping: {}", descriptor.name());
            return;
        }
        try {
            openInBukkit(descriptor);
        } catch (RuntimeException e) {
            log.warn("Failed to auto-load managed world: name={}", descriptor.name(), e);
        }
    }

    @Override
    public ManagedWorld create(String name) {
        WorldNameValidator.validate(name);
        if (existsAnywhere(name)) {
            throw new WorldAlreadyExistsException(name);
        }
        var descriptor = defaultDescriptor(name);
        openInBukkit(descriptor);
        storage.save(descriptor);
        log.info("Created empty world: {}", name);
        return descriptor;
    }

    @Override
    public ManagedWorld load(String name) {
        WorldNameValidator.validate(name);
        if (storage.find(name).isPresent() || Bukkit.getWorld(name) != null) {
            throw new WorldAlreadyExistsException(name);
        }
        var directory = worldDirectoryOf(name);
        if (!directory.isDirectory() || !looksLikeWorld(directory)) {
            throw new WorldNotFoundException(name);
        }
        var descriptor = defaultDescriptor(name);
        openInBukkit(descriptor);
        storage.save(descriptor);
        log.info("Loaded existing world under management: {}", name);
        return descriptor;
    }

    @Override
    public void delete(String name) {
        WorldNameValidator.validate(name);
        var descriptor = storage.find(name)
                .orElseThrow(() -> new WorldNotFoundException(name));
        unloadIfLoaded(name);
        WorldDirectoryDeleter.delete(worldDirectoryOf(name).toPath(), serverRoot.toPath());
        storage.remove(descriptor.name());
        log.info("Deleted managed world: {}", name);
    }

    @Override
    public void teleport(Player player, String worldName) {
        WorldNameValidator.validate(worldName);
        var descriptor = storage.find(worldName)
                .orElseThrow(() -> new WorldNotFoundException(worldName));
        var world = Bukkit.getWorld(worldName);
        if (world == null) {
            world = openInBukkit(descriptor);
        }
        player.teleport(defaultSpawn(world));
    }

    @Override
    public List<ManagedWorld> listManaged() {
        return storage.all();
    }

    @Override
    public boolean isManaged(@Nullable World world) {
        if (world == null) {
            return false;
        }
        return storage.find(world.getName()).isPresent();
    }

    @Override
    public boolean isManaged(@Nullable String name) {
        return storage.find(name).isPresent();
    }

    @Override
    public void reloadAll() {
        for (var descriptor : storage.all()) {
            reloadOne(descriptor);
        }
    }
}

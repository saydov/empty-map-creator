package ru.saydov.emc;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.saydov.emc.command.EmptyMapCommand;
import ru.saydov.emc.command.SpeedCommand;
import ru.saydov.emc.command.handler.*;
import ru.saydov.emc.listener.*;
import ru.saydov.emc.world.EmptyWorldService;
import ru.saydov.emc.world.spi.BukkitEmptyWorldService;
import ru.saydov.emc.world.spi.VoidChunkGenerator;
import ru.saydov.emc.world.spi.YamlManagedWorldStorage;

import java.io.File;
import java.util.List;

/**
 * Точка входа плагина — наследник {@link JavaPlugin}, управляющий жизненным циклом.
 *
 * <p>Последовательность {@link #onEnable()}:
 * <ol>
 *   <li>создание директории данных плагина</li>
 *   <li>загрузка {@code worlds.yml} в {@link YamlManagedWorldStorage}</li>
 *   <li>инициализация {@link BukkitEmptyWorldService} с shared {@link VoidChunkGenerator}</li>
 *   <li>регистрация слушателей (включая {@link ManagedWorldAutoLoader}, который
 *       вызовет {@link EmptyWorldService#reloadAll()} на {@code ServerLoadEvent})</li>
 *   <li>регистрация команд {@code /emc} и {@code /speed}</li>
 * </ol>
 *
 * <p><b>Почему reloadAll() не вызывается напрямую:</b> при {@code load: STARTUP}
 * {@code onEnable()} запускается до загрузки основного мира, а {@code Bukkit.createWorld()}
 * в этой фазе запрещён ({@code IllegalStateException: Cannot create additional worlds on STARTUP}).
 * Поэтому загрузка миров отложена до {@link org.bukkit.event.server.ServerLoadEvent}.
 *
 * <p><b>Shared генератор:</b> один экземпляр {@link VoidChunkGenerator} создаётся
 * при инициализации плагина и переиспользуется в двух местах:
 * <ul>
 *   <li>в {@link BukkitEmptyWorldService} — при создании/загрузке миров через {@code WorldCreator}</li>
 *   <li>в {@link #getDefaultWorldGenerator} — только для миров, числящихся
 *       управляемыми; для чужих миров возвращается {@code null}, чтобы не подменять
 *       ванильную генерацию</li>
 * </ul>
 * Класс без состояния, переиспользование безопасно и экономит память.
 *
 * <p><b>Field injection:</b> {@code emptyWorldService} — non-final поле, инициализируется
 * в {@link #onEnable()}. Это вынужденная уступка no-arg контракту {@link JavaPlugin}
 * (раздел 39 STYLE_GUIDE: допустимо только для framework-классов).
 *
 * @see EmptyWorldService
 */
@Slf4j
public final class EmptyMapCreatorPlugin extends JavaPlugin {

    private static final String WORLDS_FILE_NAME = "worlds.yml";
    private static final String ROOT_COMMAND_NAME = "emc";
    private static final String SPEED_COMMAND_NAME = "speed";

    private final VoidChunkGenerator voidChunkGenerator = new VoidChunkGenerator();
    private EmptyWorldService emptyWorldService;

    private static List<SubcommandHandler> buildHandlers(EmptyWorldService service) {
        return List.of(
                new CreateSubcommandHandler(service),
                new LoadSubcommandHandler(service),
                new DeleteSubcommandHandler(service),
                new TeleportSubcommandHandler(service),
                new ListSubcommandHandler(service));
    }

    private void ensureDataFolder() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            log.warn("Failed to create plugin data directory: {}", getDataFolder());
        }
    }

    private EmptyWorldService buildEmptyWorldService() {
        var storage = new YamlManagedWorldStorage(new File(getDataFolder(), WORLDS_FILE_NAME));
        storage.loadAll();
        return new BukkitEmptyWorldService(storage, Bukkit.getWorldContainer(), voidChunkGenerator);
    }

    private void registerListeners() {
        var pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new BlockPhysicsDisableListener(emptyWorldService), this);
        pluginManager.registerEvents(new FluidFlowDisableListener(emptyWorldService), this);
        pluginManager.registerEvents(new EntityActionDisableListener(emptyWorldService), this);
        pluginManager.registerEvents(new WorldRuleInitializationListener(emptyWorldService), this);
        pluginManager.registerEvents(new ManagedWorldAutoLoader(emptyWorldService), this);
    }

    private void registerRootCommand() {
        var rootCommand = getCommand(ROOT_COMMAND_NAME);
        if (rootCommand == null) {
            log.error("Root command '{}' is not declared in plugin.yml", ROOT_COMMAND_NAME);
            return;
        }
        var dispatcher = EmptyMapCommand.of(buildHandlers(emptyWorldService));
        rootCommand.setExecutor(dispatcher);
        rootCommand.setTabCompleter(dispatcher);
    }

    private void registerSpeedCommand() {
        var speedCommand = getCommand(SPEED_COMMAND_NAME);
        if (speedCommand == null) {
            log.error("Command '{}' is not declared in plugin.yml", SPEED_COMMAND_NAME);
            return;
        }
        var handler = new SpeedCommand();
        speedCommand.setExecutor(handler);
        speedCommand.setTabCompleter(handler);
    }

    @Override
    public void onEnable() {
        ensureDataFolder();
        emptyWorldService = buildEmptyWorldService();
        registerListeners();
        registerRootCommand();
        registerSpeedCommand();
        log.info("empty-map-creator enabled; managed worlds: {} (auto-load pending ServerLoadEvent)",
                emptyWorldService.listManaged().size());
    }

    @Override
    public @Nullable ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        if (emptyWorldService == null || !emptyWorldService.isManaged(worldName)) {
            return null;
        }
        return voidChunkGenerator;
    }
}

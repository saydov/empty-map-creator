package ru.saydov.emc.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import ru.saydov.emc.world.EmptyWorldService;

/**
 * Слушатель {@link ServerLoadEvent} — поднимает управляемые миры после старта сервера.
 *
 * <p><b>Зачем отдельный этап:</b> при {@code load: STARTUP} (в {@code plugin.yml})
 * {@link org.bukkit.plugin.java.JavaPlugin#onEnable()} вызывается ДО загрузки основного мира.
 * В этой фазе {@code Bukkit.createWorld()} запрещён серверной проверкой
 * ({@code Cannot create additional worlds on STARTUP}) и бросает {@code IllegalStateException}.
 *
 * <p>{@link ServerLoadEvent} срабатывает один раз по завершении инициализации (как при
 * обычном старте, так и при {@code /reload}). К этому моменту основной мир уже поднят
 * и создание дополнительных миров разрешено.
 *
 * <p>Фактическая работа делегируется {@link EmptyWorldService#reloadAll()} — сам по себе
 * метод не знает о фазах сервера; весь оркестр «когда вызывать» сосредоточен здесь.
 */
@Slf4j
@RequiredArgsConstructor
public class ManagedWorldAutoLoader implements Listener {

    private final EmptyWorldService emptyWorldService;

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        emptyWorldService.reloadAll();
        log.info("Auto-loaded managed worlds: count={}", emptyWorldService.listManaged().size());
    }
}

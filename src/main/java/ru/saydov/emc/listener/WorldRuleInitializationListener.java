package ru.saydov.emc.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import ru.saydov.emc.world.EmptyWorldService;
import ru.saydov.emc.world.spi.EmptyWorldRules;

/**
 * Слушатель загрузки миров, применяющий правила тишины к управляемым мирам.
 *
 * <p>Срабатывает при каждом {@link WorldLoadEvent} — неважно, создан мир
 * плагином или загружен сервером при старте. Если мир числится управляемым,
 * к нему немедленно применяется {@link EmptyWorldRules#applyTo}.
 *
 * <p><b>Зачем дублировать с сервисом:</b> {@code BukkitEmptyWorldService}
 * применяет правила при собственных операциях, но мир может быть загружен
 * и сторонним механизмом ({@code /reload}, другой плагин). Этот слушатель
 * гарантирует, что правила применятся в любом случае.
 *
 * <p><b>Почему не наследник {@code AbstractManagedWorldListener}:</b>
 * базовый класс отменяет события; этот слушатель их не отменяет, а применяет
 * побочный эффект — применение правил. Разные ответственности, разные иерархии.
 *
 * @see EmptyWorldService
 * @see EmptyWorldRules
 */
@Slf4j
@RequiredArgsConstructor
public class WorldRuleInitializationListener implements Listener {

    private final EmptyWorldService emptyWorldService;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        var world = event.getWorld();
        if (!emptyWorldService.isManaged(world)) {
            return;
        }
        EmptyWorldRules.applyTo(world);
        log.debug("Applied stillness rules to managed world: {}", world.getName());
    }
}

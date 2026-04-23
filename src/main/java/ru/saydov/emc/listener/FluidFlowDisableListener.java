package ru.saydov.emc.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import ru.saydov.emc.world.EmptyWorldService;

/**
 * Слушатель событий течения жидкостей, отменяющий их в управляемых мирах.
 *
 * <p>Охватывает все сценарии распространения воды и лавы:
 * <ul>
 *   <li>{@link BlockFromToEvent} — перетекание жидкости из одного блока в соседний</li>
 *   <li>{@link FluidLevelChangeEvent} — изменение уровня жидкости в источнике</li>
 * </ul>
 *
 * <p><b>Следствие:</b> поставленное игроком ведро воды или лавы останется на месте,
 * не растекаясь — источник сохраняется, но соседние пустые блоки не заполняются.
 * Игрок может использовать жидкости как декоративные блоки без риска затопить постройку.
 *
 * <p><b>Размещение через ведро:</b> не блокируется — {@link org.bukkit.event.player.PlayerBucketEmptyEvent}
 * здесь не обрабатывается, чтобы сохранить возможность ручного размещения жидкостей.
 *
 * @see AbstractManagedWorldListener
 * @see BlockPhysicsDisableListener
 */
public class FluidFlowDisableListener extends AbstractManagedWorldListener {

    public FluidFlowDisableListener(EmptyWorldService emptyWorldService) {
        super(emptyWorldService);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        cancelIfManaged(event, event.getBlock().getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFluidLevelChange(FluidLevelChangeEvent event) {
        cancelIfManaged(event, event.getBlock().getWorld());
    }
}

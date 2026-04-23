package ru.saydov.emc.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import ru.saydov.emc.world.EmptyWorldService;

/**
 * Слушатель действий сущностей, изменяющих блоки — отменяет их в управляемых мирах.
 *
 * <p>Обрабатываемые категории событий:
 * <ul>
 *   <li>{@link EntityChangeBlockEvent} — эндермены переносят блоки, песок/гравий падают,
 *       силверфиш зарываются, овцы едят траву</li>
 *   <li>{@link EntityExplodeEvent} — взрывы криперов, пожирателей, огненных шаров</li>
 *   <li>{@link BlockExplodeEvent} — взрывы блоков (TNT, якорь возрождения, респаун-якорь)</li>
 *   <li>{@link EntityBlockFormEvent} — снежный голем оставляет снежный след</li>
 *   <li>{@link SpongeAbsorbEvent} — губка впитывает воду</li>
 * </ul>
 *
 * <p><b>Отмена взрывов:</b> при отмене {@link EntityExplodeEvent} или
 * {@link BlockExplodeEvent} уничтоженные блоки не восстанавливаются,
 * но и не удаляются — событие отменяется до начала разрушения.
 * Звук и визуальный эффект взрыва остаются видимыми.
 *
 * @see AbstractManagedWorldListener
 * @see EmptyWorldService
 */
public class EntityActionDisableListener extends AbstractManagedWorldListener {

    public EntityActionDisableListener(EmptyWorldService emptyWorldService) {
        super(emptyWorldService);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        cancelIfManaged(event, event.getBlock().getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        cancelIfManaged(event, event.getEntity().getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        cancelIfManaged(event, event.getBlock().getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        cancelIfManaged(event, event.getBlock().getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        cancelIfManaged(event, event.getBlock().getWorld());
    }
}

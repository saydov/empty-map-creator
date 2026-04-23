package ru.saydov.emc.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import ru.saydov.emc.world.EmptyWorldService;

/**
 * Слушатель событий физики блоков, отменяющий их в управляемых мирах.
 *
 * <p>Перехватывает все события, относящиеся к «пассивной» физике блоков —
 * той, что возникает без явного действия игрока или сущности. Обрабатываемые
 * категории:
 * <ul>
 *   <li>апдейты физики ({@link BlockPhysicsEvent}) — проверка опоры для растений,
 *       падение гравитационных блоков, обрушение соседей</li>
 *   <li>угасание/фейд ({@link BlockFadeEvent}) — таяние льда и снега, выгорание</li>
 *   <li>распространение ({@link BlockSpreadEvent}) — травы, грибов, огня</li>
 *   <li>формирование ({@link BlockFormEvent}) — образование льда, снежного покрова</li>
 *   <li>рост ({@link BlockGrowEvent}, {@link StructureGrowEvent}) — посевы, саженцы</li>
 *   <li>распад листвы ({@link LeavesDecayEvent})</li>
 *   <li>горение и возгорание ({@link BlockBurnEvent}, {@link BlockIgniteEvent})</li>
 *   <li>поршни ({@link BlockPistonExtendEvent}, {@link BlockPistonRetractEvent})</li>
 *   <li>увлажнение грядок ({@link MoistureChangeEvent})</li>
 * </ul>
 *
 * <p><b>Фильтрация:</b> каждое событие сверяется с {@link EmptyWorldService#isManaged}
 * через {@link AbstractManagedWorldListener#cancelIfManaged} и отменяется только
 * для управляемых миров. Обычные миры сервера работают в ванильном режиме.
 *
 * <p><b>Приоритет:</b> {@link EventPriority#LOWEST} — чтобы отмена произошла до того,
 * как другие плагины выполнят реакцию на событие и впустую потратят ресурсы.
 *
 * @see AbstractManagedWorldListener
 * @see EmptyWorldService
 */
public class BlockPhysicsDisableListener extends AbstractManagedWorldListener {

    public BlockPhysicsDisableListener(EmptyWorldService emptyWorldService) {
        super(emptyWorldService);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        cancelIfManaged(event, event.getBlock().getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        cancelIfManaged(event, event.getBlock().getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        cancelIfManaged(event, event.getBlock().getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        cancelIfManaged(event, event.getBlock().getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        cancelIfManaged(event, event.getBlock().getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        cancelIfManaged(event, event.getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        cancelIfManaged(event, event.getBlock().getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        cancelIfManaged(event, event.getBlock().getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        cancelIfManaged(event, event.getBlock().getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        cancelIfManaged(event, event.getBlock().getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        cancelIfManaged(event, event.getBlock().getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMoistureChange(MoistureChangeEvent event) {
        cancelIfManaged(event, event.getBlock().getWorld());
    }
}

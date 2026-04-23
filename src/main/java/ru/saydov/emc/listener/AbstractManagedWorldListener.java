package ru.saydov.emc.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;
import ru.saydov.emc.world.EmptyWorldService;

/**
 * Базовый класс для слушателей, отменяющих события в управляемых мирах.
 *
 * <p>Инкапсулирует общий guard-шаблон {@link #cancelIfManaged} — проверку
 * управляемости мира и отмену события. До вынесения этот шаблон повторялся
 * девятнадцать раз в трёх разных слушателях и представлял собой истинное
 * дублирование бизнес-логики (раздел 41 STYLE_GUIDE).
 *
 * <p><b>Контракт для наследников:</b>
 * <ul>
 *   <li>каждый {@code @EventHandler} делегирует решение в {@link #cancelIfManaged}</li>
 *   <li>приоритет {@code LOWEST} и {@code ignoreCancelled = true} — конвенция плагина,
 *       но не навязывается базовым классом; задаётся на каждой аннотации {@code @EventHandler}</li>
 *   <li>мир извлекается из конкретного события наследником — базовый класс
 *       не знает о структуре событий Bukkit</li>
 * </ul>
 *
 * <p><b>Почему абстрактный класс, а не утилита:</b> {@link EmptyWorldService}
 * — константа для всего слушателя, а не параметр каждого вызова. Базовый класс
 * хранит ссылку один раз, наследники получают чистый двухаргументный API
 * {@code cancelIfManaged(event, world)}.
 *
 * @see EmptyWorldService
 */
@RequiredArgsConstructor
public abstract class AbstractManagedWorldListener implements Listener {

    private final EmptyWorldService emptyWorldService;

    /**
     * Отменяет событие, если затронутый мир управляется плагином.
     *
     * <p>Метод {@code final} — наследникам не требуется и не разрешается переопределять
     * логику отмены. Всё, что от них требуется — вызвать этот метод из
     * {@code @EventHandler}-обработчика конкретного события.
     *
     * @param event событие, поддерживающее отмену
     * @param world мир, в котором произошло событие
     */
    protected final void cancelIfManaged(Cancellable event, World world) {
        if (!emptyWorldService.isManaged(world)) {
            return;
        }
        event.setCancelled(true);
    }
}

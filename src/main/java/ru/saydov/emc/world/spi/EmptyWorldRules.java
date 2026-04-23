package ru.saydov.emc.world.spi;

import lombok.experimental.UtilityClass;
import org.bukkit.GameRule;
import org.bukkit.GameRules;
import org.bukkit.World;

/**
 * Набор игровых правил, принудительно применяемых к управляемым пустым мирам.
 *
 * <p>Единая точка включения «режима тишины»: правила из {@link GameRules},
 * подавляющие случайные тики, горение, спавн существ, смену погоды и дня,
 * взрывы и рейды. Дополнительно немедленно снимается активная гроза и шторм.
 *
 * <p><b>Единый источник истины:</b> вызывается из {@link BukkitEmptyWorldService}
 * при создании/загрузке мира и из слушателя {@code WorldLoadEvent} при
 * повторной загрузке мира сторонним механизмом ({@code /reload}, другой плагин).
 * Повторное применение идемпотентно — {@code World#setGameRule} перезаписывает
 * значение, повторные вызовы безопасны.
 *
 * @see GameRules
 */
@UtilityClass
public class EmptyWorldRules {

    /**
     * Применяет к указанному миру все правила, подавляющие автоматические процессы.
     *
     * <p>Безопасно вызывать многократно: все операции — прямые setter'ы
     * на {@link World}, без накапливающегося состояния.
     *
     * @param world мир, к которому применяются правила
     */
    public static void applyTo(World world) {
        world.setGameRule(GameRules.RANDOM_TICK_SPEED, 0);
        disableFireTick(world);
        world.setGameRule(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0);
        world.setGameRule(GameRules.SPREAD_VINES, false);
        world.setGameRule(GameRules.SPAWN_MOBS, false);
        world.setGameRule(GameRules.SPAWN_MONSTERS, false);
        world.setGameRule(GameRules.SPAWN_PATROLS, false);
        world.setGameRule(GameRules.SPAWN_WANDERING_TRADERS, false);
        world.setGameRule(GameRules.SPAWN_PHANTOMS, false);
        world.setGameRule(GameRules.SPAWN_WARDENS, false);
        world.setGameRule(GameRules.ADVANCE_WEATHER, false);
        world.setGameRule(GameRules.ADVANCE_TIME, false);
        world.setGameRule(GameRules.MOB_GRIEFING, false);
        world.setGameRule(GameRules.RAIDS, false);
        world.setGameRule(GameRules.TNT_EXPLODES, false);
        world.setStorm(false);
        world.setThundering(false);
    }

    // GameRule.DO_FIRE_TICK помечен @Deprecated(forRemoval=true), но в Paper 1.21.11
    // нет эквивалента в новом GameRules — используем старый API до появления замены.
    @SuppressWarnings("removal")
    private static void disableFireTick(World world) {
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
    }
}

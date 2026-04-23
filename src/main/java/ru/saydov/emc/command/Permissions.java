package ru.saydov.emc.command;

/**
 * Константы разрешений плагина empty-map-creator.
 *
 * <p>Единая точка правды для всех permission-нод. Раньше строки вида
 * {@code "emc.create"} были продублированы в каждом {@code SubcommandHandler}
 * и в {@code plugin.yml} — при переименовании легко было разъехаться.
 * Теперь Java-код ссылается на эти константы, а {@code plugin.yml} остаётся
 * единственным местом, где строки физически объявлены для Bukkit.
 *
 * <p><b>Соглашение:</b> имя константы — глагол в верхнем регистре,
 * значение — короткая форма в нижнем регистре без префикса {@code emc.use}.
 * Новое разрешение добавляется здесь и одновременно в {@code plugin.yml}.
 *
 * @see ru.saydov.emc.command.handler.SubcommandHandler#permission()
 */
public interface Permissions {

    /**
     * Базовый доступ к команде {@code /emc} — показ справки.
     */
    String USE = "emc.use";

    /**
     * Создание новых пустых миров через {@code /emc create <name>}.
     */
    String CREATE = "emc.create";

    /**
     * Загрузка существующих миров под управление через {@code /emc load <name>}.
     */
    String LOAD = "emc.load";

    /**
     * Удаление управляемых миров через {@code /emc delete <name>}.
     */
    String DELETE = "emc.delete";

    /**
     * Телепортация в управляемый мир через {@code /emc tp <name>}.
     */
    String TELEPORT = "emc.tp";

    /**
     * Просмотр списка управляемых миров через {@code /emc list}.
     */
    String LIST = "emc.list";

    /**
     * Установка скорости ходьбы или полёта через {@code /speed}.
     */
    String SPEED = "emc.speed";
}

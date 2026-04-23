package ru.saydov.emc.util;

import lombok.experimental.UtilityClass;
import ru.saydov.emc.exception.InvalidWorldNameException;

import java.util.regex.Pattern;

/**
 * Валидатор имён миров, допустимых для использования в плагине.
 *
 * <p>Централизованная точка проверки имени, предназначенная для вызова перед любой
 * операцией, затрагивающей файловую систему или Bukkit-реестр миров. Отделяет правила
 * валидации от бизнес-логики сервиса и делает их переиспользуемыми.
 *
 * <p><b>Правила валидации:</b>
 * <ul>
 *   <li>имя не пустое и не состоит из одних пробелов</li>
 *   <li>длина не превышает {@link #MAX_LENGTH} символов</li>
 *   <li>состоит только из латинских букв, цифр, дефиса и подчёркивания ({@link #ALLOWED_PATTERN})</li>
 *   <li>разделители пути ({@code /}, {@code \}) и последовательность {@code ..}
 *       исключены регуляркой — дополнительная защита от path traversal</li>
 * </ul>
 *
 * <p><b>Потокобезопасность:</b> класс без состояния — все методы чистые (pure),
 * безопасны для одновременного вызова из нескольких потоков.
 *
 * @see InvalidWorldNameException
 */
@UtilityClass
public class WorldNameValidator {

    /**
     * Максимальная длина имени мира — ограничение Minecraft на длину папки.
     *
     * <p>Значение с запасом совместимо с типичными ограничениями файловых систем
     * (Windows MAX_PATH и NTFS) и читаемо в консоли.
     */
    public static final int MAX_LENGTH = 64;

    /**
     * Разрешённый синтаксис имени мира.
     *
     * <p>Латинские буквы, цифры, дефис и подчёркивание — минимальный набор,
     * безопасный на всех файловых системах. Исключает пробелы, unicode, путевые
     * разделители и зарезервированные символы Windows ({@code < > : " | ? *}).
     */
    public static final Pattern ALLOWED_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");

    /**
     * Проверяет имя мира на соответствие правилам безопасности и синтаксиса.
     *
     * <p>Guard clause для использования в начале методов публичного API.
     * При несоответствии правилам бросает {@link InvalidWorldNameException}
     * с коротким идентификатором на английском.
     *
     * @param name проверяемое имя; не может быть {@code null}
     * @throws InvalidWorldNameException если имя пустое, слишком длинное
     *                                   или содержит запрещённые символы
     */
    public static void validate(String name) {
        if (name.isBlank()) {
            throw new InvalidWorldNameException("blank");
        }
        if (name.length() > MAX_LENGTH) {
            throw new InvalidWorldNameException("too long: " + name.length() + " chars");
        }
        if (!ALLOWED_PATTERN.matcher(name).matches()) {
            throw new InvalidWorldNameException(name);
        }
    }
}

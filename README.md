<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:1e3a8a,50:3b82f6,100:22d3ee&height=200&section=header&text=Empty%20Map%20Creator&fontSize=58&fontColor=ffffff&animation=fadeIn&fontAlignY=38&desc=Paper%201.21.11%20%E2%80%A2%20Void%20Worlds%20%E2%80%A2%20Zero%20Physics%20%E2%80%A2%20Zero%20Dependencies&descSize=15&descAlignY=62" width="100%" alt="Empty Map Creator" />

**Paper-плагин для идеально пустых миров без физики**

![Paper](https://img.shields.io/badge/Paper-1.21.11-E6552C?style=for-the-badge&logo=minecraft&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.13-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-4CAF50?style=for-the-badge)

</div>

---

## Быстрый старт

```
/emc create sandbox     → создаёт пустой мир "sandbox"
/emc tp sandbox         → телепорт на спавн
/speed fly 6            → увеличивает скорость полёта
/emc list               → список управляемых миров
/emc delete sandbox     → сносит мир с диска
```

Никаких падающих песков, ползущих вод и растущих деревьев — стерильная песочница для постройки карт.

## Установка

```bash
mvn clean package
cp target/empty-map-creator-1.0-SNAPSHOT.jar /path/to/server/plugins/
```

После рестарта сервера плагин создаст `plugins/EmptyMapCreator/worlds.yml` — там хранится список управляемых миров.

## Что делает плагин

**Void-генерация.** Новые чанки — пустые, без ландшафта, света и структур.

**Отключение физики.** В управляемых мирах не срабатывают:
- `BlockPhysicsEvent`, `BlockFromToEvent`, `FluidLevelChangeEvent` — вода и лава стоят на месте
- `EntityChangeBlockEvent` — песок, гравий и конкреционный порошок не падают
- `BlockSpreadEvent`, `BlockFormEvent`, `BlockGrowEvent` — огонь не распространяется, трава не растёт, лёд не образуется
- `LeavesDecayEvent`, `BlockFadeEvent` — листья не отмирают
- `StructureGrowEvent` — саженцы не вырастают в деревья
- `EntityExplodeEvent`, `BlockExplodeEvent` — никаких взрывов
- `BlockPistonExtendEvent`, `BlockPistonRetractEvent` — поршни не двигают блоки
- `BlockBurnEvent`, `BlockIgniteEvent` — ничего не горит

**Правила мира** (устанавливаются при загрузке):
```
randomTickSpeed = 0
doFireTick      = false
doVinesSpread   = false
doWeatherCycle  = false
doMobSpawning   = false
```

## Команды

### `/emc` — управление мирами

| Команда | Что делает |
|---|---|
| `create <name>` | Создаёт новый void-мир |
| `load <name>` | Берёт существующий мир под управление (без перегенерации) |
| `tp <name>` | Телепорт на спавн мира |
| `delete <name>` | Удаляет мир с диска; игроки из мира эвакуируются на главный спавн |
| `list` | Показывает список всех управляемых миров |

Алиасы корневой команды: `/emptymaps`, `/emap`.

### `/speed` — скорость игрока

| Форма | Действие |
|---|---|
| `/speed <1-10>` | Скорость ходьбы (и полёта, если в полёте) |
| `/speed walk <1-10>` | Только ходьба |
| `/speed fly <1-10>` | Только полёт |

Диапазон `1-10` линейно ложится на Bukkit `0.1-1.0`. Ванильная ходьба ≈ `2`.

## Permission-узлы

| Узел | Назначение |
|---|---|
| `emc.use` | Справка `/emc` |
| `emc.create` | `/emc create` |
| `emc.load` | `/emc load` |
| `emc.delete` | `/emc delete` |
| `emc.tp` | `/emc tp` |
| `emc.list` | `/emc list` |
| `emc.speed` | `/speed` |
| `emc.admin` | Всё вышеперечисленное (наследует) |

Значение по умолчанию везде — `op`.

## Стек

- **Java 21** — records, pattern matching, `var`
- **Paper API 1.21.11** — `paper-api` (scope: `provided`)
- **Lombok 1.18.34** — `@RequiredArgsConstructor`, `@Slf4j`, `@UtilityClass`
- **JetBrains Annotations** — `@Nullable` на контрактных границах

## Архитектура

<details>
<summary>Развернуть дерево пакетов</summary>

```
ru.saydov.emc
├── EmptyMapCreatorPlugin            — onEnable, регистрация команд и слушателей
│
├── command
│   ├── EmptyMapCommand              — диспетчер /emc
│   ├── SpeedCommand                 — обработчик /speed
│   ├── Permissions                  — константы permission-нод
│   └── handler
│       ├── SubcommandHandler        — интерфейс подкоманды
│       ├── CreateSubcommandHandler
│       ├── LoadSubcommandHandler
│       ├── DeleteSubcommandHandler
│       ├── TeleportSubcommandHandler
│       └── ListSubcommandHandler
│
├── listener
│   ├── AbstractManagedWorldListener     — базовый фильтр "только управляемые миры"
│   ├── BlockPhysicsDisableListener      — гашение физики блоков
│   ├── FluidFlowDisableListener         — стоячая вода/лава
│   ├── EntityActionDisableListener      — сущности не меняют блоки
│   └── WorldRuleInitializationListener  — установка GameRule при загрузке
│
├── world
│   ├── EmptyWorldService            — публичный API (create/load/delete/tp/list)
│   ├── ManagedWorld                 — модель управляемого мира
│   └── spi
│       ├── BukkitEmptyWorldService  — реализация поверх Bukkit
│       ├── VoidChunkGenerator       — генератор пустых чанков
│       ├── YamlManagedWorldStorage  — персистенция в worlds.yml
│       └── EmptyWorldRules          — правила применения GameRule
│
├── exception
│   ├── InvalidWorldNameException
│   ├── WorldAlreadyExistsException
│   ├── WorldNotFoundException
│   └── WorldDeletionException
│
└── util
    ├── WorldNameValidator           — валидация имён (@UtilityClass)
    ├── WorldDirectoryDeleter        — рекурсивное удаление папки мира
    └── ManagedWorldNameCompleter    — таб-комплит имён управляемых миров
```

</details>

## Сборка

```bash
mvn compile          # проверка компиляции
mvn clean package    # сборка JAR
```

Результат — `target/empty-map-creator-1.0-SNAPSHOT.jar`.

## Разработка

Код-стайл, правила JavaDoc и соглашения об именовании описаны в [`STYLE_GUIDE.md`](STYLE_GUIDE.md).

## Лицензия

MIT

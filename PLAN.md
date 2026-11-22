# План работ

Каждая фаза: изучение → согласование → реализация → проверка → коммит.

## Фаза 0 — Baseline ✅

Дата: 2026-11-22, ветка `homework`, коммит `09be4d9`.

### Что есть в проекте

| Файл | Содержимое |
|---|---|
| `build.gradle` | плагин `java-library`, Maven Central, `useJUnitPlatform()`, кодировка UTF-8 |
| `src/test/java/SearchTests.java` | 1 тест: поиск `qa.guru` на `https://github.com/search`, проверка текста `QA.GURU` в `[data-testid='results-list']` |
| `.gitignore` | `.gradle/`, `build/` (`.idea/` не игнорируется) |
| `gradlew`, `gradle/wrapper/*` | Gradle wrapper, сгенерирован в фазе 0 (ранее в репозитории не было) |

История: `3f20ce9 added code from lesson` → `16deb01 revert code` → `72f0ab8 fix junit launcher` → `299eca9 github search as the demo target + latest deps` → `09be4d9 Remove build artifacts from git and add .gitignore`.

### Фактические версии

Получено из `./gradlew --version` и `./gradlew dependencies --configuration testRuntimeClasspath`.

| Компонент | Версия |
|---|---|
| Java (toolchain не задан, используется `JAVA_HOME`) | OpenJDK 21.0.13 (Homebrew) |
| Gradle (wrapper) | 9.8.2 |
| Selenide | 7.18.2 |
| Selenium (транзитивно) | 4.49.0 |
| JUnit Jupiter / Platform | 6.1.3 |
| slf4j-api (транзитивно) | 2.0.19 |
| Браузер | Google Chrome (локально), Selenide по умолчанию |
| ОС | macOS 26.4, aarch64 |

### Результат запуска исходного теста (без изменений кода)

**Запуск 1 — как есть: ❌ FAILED**

```
./gradlew clean test
SearchTests > successfulSearchTest() FAILED
    org.openqa.selenium.TimeoutException at SearchTests.java:10
    timeout: Timed out receiving message from renderer
BUILD FAILED in 1m 6s
```

Причина: Chrome берёт системный прокси macOS (PAC `proxy-pac.…/proxy.pac`), который требует
авторизацию (HTTP 407). Страница `github.com/search` не загружается, `open()` падает по таймауту.
Код теста ни при чём.

**Запуск 2 — Chrome без прокси: ✅ PASSED**

```
JAVA_TOOL_OPTIONS="-Dchromeoptions.args=--no-proxy-server" ./gradlew clean test
SearchTests: tests=1, failures=0, errors=0, time=5.1s
BUILD SUCCESSFUL
```

### Ограничения окружения

- **Корпоративный прокси.** Переменные `http(s)_proxy`/`ALL_PROXY` в `~/.zshrc` и системный PAC требуют
  авторизацию (407). Сейчас интернет доступен напрямую, поэтому Gradle и тесты запускались
  с отключённым прокси (`env -u http_proxy -u https_proxy ...` и `--no-proxy-server` для Chrome).
  В корпоративной сети без учётки прокси сборка и тесты работать не будут.
- **Внешний github.com нестабилен.** Возможны HTTP 429 (rate limit), капча, изменения разметки
  (`aria-label`, `data-testid`). Падение теста не всегда означает регрессию.
- Тест ходит в реальный интернет, поэтому результат зависит от сети и поиска GitHub
  (выдача по `qa.guru` может измениться).
- `build.gradle` не пробрасывает системные свойства в тестовую JVM, поэтому для диагностики
  использовался `JAVA_TOOL_OPTIONS`.

### Замечания на будущие фазы

- Добавить `.idea/` в `.gitignore`.
- Пробрасывать `-D` свойства (браузер, прокси, remote) из Gradle в тесты.
- Явно задать Java toolchain 21 в `build.gradle`.

## Фаза 1 — TestBase и воспроизводимость ✅

### Что сделано

- **Gradle Wrapper** пересоздан штатной командой с проверкой официального SHA-256
  (взят с `https://services.gradle.org/distributions/gradle-9.8.2-bin.zip.sha256`):
  ```
  gradle wrapper --gradle-version 9.8.2 --distribution-type bin \
    --gradle-distribution-sha256-sum 21bd5da2de49b642904077f38c0efdc7778e92b3ed0889392dca61b97e389607
  ```
  В `gradle-wrapper.properties` появился `distributionSha256Sum`.
- **Java toolchain 21** в `build.gradle`. Сборка не зависит от `JAVA_HOME` и `java` в PATH.
- **Явная передача параметров** из Gradle в тестовую JVM: только `baseUrl`, `browser`, `browserSize`,
  `headless` и только если заданы. Остальные `-D` в тесты не попадают. Значения — входы задачи `test`,
  поэтому смена `-D` перезапускает тесты (не UP-TO-DATE).
- **`TestBase`**: `@BeforeEach` настраивает Selenide (`@BeforeAll` не используется: он срабатывает
  на каждый класс, а не один раз на JVM), `@AfterEach` вызывает `Selenide.closeWebDriver()`.
  Итоговая конфигурация печатается в stdout теста (видна в отчёте).
- `SearchTests extends TestBase`, `open("/search")` относительно `baseUrl`.

### Параметры

| `-D` | По умолчанию |
|---|---|
| `baseUrl` | `https://github.com` |
| `browser` | `chrome` |
| `browserSize` | `1920x1080` |
| `headless` | `false` |

Пример: `./gradlew test -Dheadless=true -DbrowserSize=1280x800`

### Проверка

Все запуски с `JAVA_HOME` не заданным, `java` в PATH — 27, Chrome без прокси
(`JAVA_TOOL_OPTIONS=-Dchromeoptions.args=--no-proxy-server`, только локально, в код не входит).
Для проверки закрытия браузера в `@AfterEach` временно выводились `hasWebDriverStarted()` и число
дочерних процессов chromedriver/Chrome; диагностика удалена.

| Запуск | Результат | Конфигурация в тестовой JVM | Браузер после `closeWebDriver()` |
|---|---|---|---|
| defaults | ✅ PASSED | `github.com, chrome, 1920x1080, headless=false`, `java.version=21.0.13` | started=false, процессов 0 (было 8) |
| `-DbrowserSize=1280x800 -Dheadless=true -DbaseUrl=… -Dbrowser=chrome -Dfoo=bar` | ✅ PASSED | `github.com, chrome, 1280x800, headless=true`; `foo=null` (отфильтрован) | started=false, процессов 0 |
| контролируемое падение (`text("NO_SUCH_TEXT_XYZ")`) | ❌ FAILED, `ElementShould` на строке 12 — ожидаемо | defaults | started=false, процессов 0; chromedriver после прогона: 0 |
| финальный, исходный assertion восстановлен | ✅ PASSED (3.8s) | defaults | — |

## Фаза 2 — Ещё два теста ✅ (с известным внешним ограничением)

### Разведка в реальном UI (до написания кода)

Временным probe-тестом (headless, удалён) проверено:

- `repo:selenide/selenide`, введённый на `/search`, GitHub открывает **во вкладке Issues** (`type=issues`).
  Если сначала открыть `/search?type=repositories`, тип сохраняется. Вкладки Repositories для
  `repo:`-запросов в фасетах нет, поэтому переключаться через неё нельзя.
- Семантика `stars:` проверена на одном репозитории (1.9k звёзд): `stars:>=0` и `stars:>100` → найден,
  `stars:<0` → не найден. Это квалификатор, а `<0` логически невозможен (не «очень большое число»).
- Empty state: `h3` «Your search did not match any repositories» (класс хешированный → ищем по тексту),
  `[data-testid='results-list']` отсутствует.
- Ссылка на результат: `a[href='/selenide/selenide']`, после клика URL `https://github.com/selenide/selenide`,
  `#repo-title-component` = «selenide Public».

### Тесты (`SearchTests`)

| Тест | Сценарий | Почему детерминирован |
|---|---|---|
| `searchByRepoQualifierOpensRepository` | `/search?type=repositories` → `repo:selenide/selenide` → клик по `a[href='/selenide/selenide']` → URL `baseUrl/selenide/selenide`, заголовок «selenide» | `repo:` даёт один репозиторий; ссылка ищется по href, не по позиции |
| `impossibleStarsFilterShowsEmptyState` | `/search?type=repositories` → `stars:<0` → видим empty state → `results-list` не существует | звёзд < 0 не бывает; `shouldNot(exist)` проверяется после появления empty state |

Только ожидания Selenide, без `sleep`, без числа результатов и порядка выдачи.

Добавлен параметр `-Dtimeout` (мс, по умолчанию `10000`) → `Configuration.timeout`: React-поиск GitHub
на медленной сети рендерится дольше дефолтных 4 с Selenide. Ретраев нет.

### Проверка

Прогоны с паузой 20 с, `--rerun` (иначе Gradle отдаёт UP-TO-DATE результат), Chrome без прокси.

Серия 1 (`timeout` = 4 с по умолчанию Selenide) — 11 прогонов, 3 падения: 2× `TimeoutException` в `open()`,
1× `NoSuchElementException` поля поиска (на скриншоте — незагруженный skeleton страницы). HTTP 429 не было
(совпадения «429» в HTML — данные SVG-иконок).

Серия 2 (`timeout` = 10 с):

| Прогон | Обычный режим | Headless |
|---|---|---|
| `successfulSearchTest` | ❌ `TimeoutException` в `open()` (31 с) | ✅ 13.9 с |
| `searchByRepoQualifierOpensRepository` | ❌ `TimeoutException` в `open()` (51 с) | ✅ 5.0 с |
| `impossibleStarsFilterShowsEmptyState` | ✅ 4.3 с | ✅ 3.7 с |
| Весь набор | ✅ 3.7 / 3.7 / 5.9 с | ✅ 3.4 / 3.5 / 6.7 с (`-Dtimeout=15000` дошёл в JVM) |

**Ни одна проверка (`shouldHave`, `url`, empty state) не упала.** Все падения — загрузка страницы
GitHub не уложилась в `pageLoadTimeout` (30 с).

### Известное ограничение: нестабильная сеть до GitHub

- Время одного и того же теста колеблется от 3 до 59 с; падения случаются на разных тестах,
  в обоих режимах, на `open()`.
- Гипотеза о зависающих сторонних ресурсах **не подтвердилась**: замер Performance API показал
  `load` ≈ `DOMContentLoaded`, медленных ресурсов в норме нет. `pageLoadStrategy=eager` не поможет.
- Вывод: внешний сетевой фактор (канал до github.com / githubassets.com, вероятно, корпоративная сеть).
  Не повод для ретраев. Повторить проверку в другой сети или в CI (фаза 7).

## Фаза 3 — Page Object ✅

### Согласованный стиль

- Fluent API: методы возвращают `this` или следующую страницу.
- Проверки — только в `should…`-методах страниц; имена остальных методов — действия пользователя.
- Локаторы — `private final SelenideElement`-поля (ленивые, безопасно объявлять полями).
- Компонентов нет: повторяющихся блоков в тестах нет.

### Структура

| Класс | Методы |
|---|---|
| `pages.SearchPage` | `open()`, `openRepositoriesSearch()`, `searchFor(query)` → `SearchResultsPage` |
| `pages.SearchResultsPage` | `shouldHaveResult(text)`, `shouldBeEmpty()`, `openRepository(fullName)` → `RepositoryPage` |
| `pages.RepositoryPage` | `shouldBeOpened(owner, name)` |
| `tests.TestBase`, `tests.SearchTests` | перенесены из корня в package `tests` |

Нюанс: в `SearchPage` вызывается `Selenide.open(...)` явно — собственный метод `open()` скрывает
статический импорт `open` с тем же именем, и `open("/search")` не скомпилировался бы.

### Проверка

- **Семантика.** Локаторы, условия и порядок проверок построчно сверены с `f19309e` — совпадают.
  Локатор ссылки `"[data-testid='results-list'] a[href='/" + fullName + "']"` при `selenide/selenide`
  даёт прежнюю строку. В `shouldBeEmpty()` порядок сохранён: сначала заглушка `visible`, потом `shouldNot(exist)`.
- **Прогон `tests.SearchTests`** (`--rerun`): ✅ 3/3 — `impossibleStarsFilterShowsEmptyState` 12.9 с,
  `successfulSearchTest` 47.4 с, `searchByRepoQualifierOpensRepository` 6.1 с (разброс времени — сеть, см. фазу 2).
- **Контрольные падения** (временный класс, удалён):

| Сценарий | Результат |
|---|---|
| `shouldHaveResult("NO_SUCH_TEXT_XYZ")` после поиска `qa.guru` | ❌ `ElementShould`: «Element should have text» на `results-list` — ожидаемо |
| `shouldBeEmpty()` после поиска `repo:selenide/selenide` | ❌ `ElementNotFound`: `h3.findBy(text "Your search did not match…")` — падает на первой проверке, ожидаемо |

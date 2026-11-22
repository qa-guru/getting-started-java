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

## Фаза 4 — Allure-отчёты ✅

### Два слоя — разные версии

| Слой | Что | Версия | Почему |
|---|---|---|---|
| JVM-адаптеры (пишут `allure-results`) | `allure-jupiter`, `allure-selenide` через `allure-bom` | **3.1.1** | `allure-jupiter` → `allure-junit-platform:3.1.1`, собран под **JUnit 6.1.0** (у нас 6.1.3). `allure-junit5` — старая линия 2.x (последняя 2.35.6, собрана под JUnit 5.10 / Platform 1.10), не подходит. `allure-selenide` не тянет свою Selenide — используется наша 7.18.2, совместимость подтверждена прогоном |
| CLI-генератор (собирает HTML) | Allure Report 2 commandline (brew) | **2.46.1** | Стандартный Allure-плагин Jenkins (фаза 7) работает с commandline Report 2; уже установлен, не требует node/npm. Allure Report 3 (npm `allure@3.33.0`) — другой инструмент с другими командами |

### Решения

- **Шаги — `@Step` + `-javaagent:aspectjweaver:1.9.27`.** Шаги на методах Page Object
  (`Open GitHub search`, `Search for "{query}"`, `Results should be empty`, …). Агент подключается
  в задаче `test` из отдельной конфигурации `aspectjAgent` (не попадает в classpath).
  Без weaving `@Step` молча не работает — наличие шагов проверено в результатах.
- **`AllureSelenide`** с дефолтами: действия Selenide — вложенные шаги, при падении — Screenshot + Page source.
  Регистрируется в `TestBase.@BeforeEach` по имени (без дублей).
- **Результаты** — `build/allure-results` (`src/test/resources/allure.properties`).
  Задача `test` очищает каталог перед запуском — прогоны не смешиваются.

### Команды

```bash
./gradlew test
allure generate --clean -o build/allure-report build/allure-results   # флаги — по `allure --help` 2.46.1
allure open build/allure-report
```

### Проверка

| Проверка | Результат |
|---|---|
| Очистка: подложен `STALE-old-run-result.json` перед прогоном | удалён; в `allure-results` ровно 3 `result.json` |
| Зелёный прогон → отчёт | ✅ total 3, passed 3; в каждом тесте шаги `@Step` с вложенными шагами Selenide `$(...)` |
| Контролируемое падение (`shouldHaveResult("NO_SUCH_TEXT_XYZ")`) | `successfulSearchTest` — **failed**, причина «Element should have text…», failed step `Results should contain "NO_SUCH_TEXT_XYZ"` → вложенный `$(...)` failed, вложения Screenshot (png) + Page source (html). Отчёт: `build/allure-report-failed` (failed 1, broken 1, passed 1) |
| Попутно: сетевой тайм-аут `open()` в `searchByRepoQualifierOpensRepository` | в отчёте **broken** с тем же набором вложений — сетевые сбои отличимы от падений проверок |
| Assertion восстановлен (diff с `803380a` пуст), финальный прогон | ✅ total 3, passed 3; вложений прошлого прогона нет |

## Фаза 5 — GitHub Actions

Workflow: `.github/workflows/ui-tests.yml`, hosted runner `ubuntu-24.04`, без Selenoid.

### Согласованные решения

- **События:** `push` в `master`/`homework`, `pull_request` в `master` (не `pull_request_target`:
  код fork PR не получает секретов и write-токена), `workflow_dispatch`. Push-триггер заодно
  гарантирует регистрацию workflow — подтверждено через API: `UI tests`, state `active`.
- **Артефакты, без GitHub Pages:** `junit-xml`, `allure-results` (с вложениями), `allure-report`, хранение 14 дней.
- **Параметры `workflow_dispatch`:** `baseUrl`, `browserSize`, `timeout`. В CI всегда `headless=true`, `browser=chrome`.
  Inputs передаются в shell через `env`, а не подстановкой `${{ }}` в `run` (защита от инъекций).

### Подготовка окружения (явная)

| Что | Как |
|---|---|
| Java 21 | `actions/setup-java` (temurin) |
| Gradle | `gradle/actions/setup-gradle`, `cache-provider: basic` (по умолчанию — коммерческий `enhanced`); валидирует wrapper jar |
| Chrome + ChromeDriver | `browser-actions/setup-chrome`, `stable` + `install-chromedriver`; отдельный шаг сверяет `MAJOR.MINOR.BUILD` Chrome и ChromeDriver (совместимость CDP) и падает при расхождении |
| Allure CLI 2.46.1 | zip с Maven Central, проверка `sha256sum --check` по зашитому хешу |

Путь к Chrome, к драйверу и флаги Chrome передаются в тесты явно: в список проброса Gradle добавлены
`selenide.browserBinary`, `webdriver.chrome.driver`, `chromeoptions.args` (локально теперь
`-Dchromeoptions.args=--no-proxy-server` вместо `JAVA_TOOL_OPTIONS`).

### Безопасность

- Все actions закреплены по commit SHA (у annotated-тегов — peeled-коммит):
  `checkout` v7.0.1, `setup-java` v6.3.0, `setup-gradle` v6.4.1, `setup-chrome` v2.2.0, `upload-artifact` v7.0.2.
- `permissions: contents: read`, `persist-credentials: false`, секретов нет.
- **`--no-sandbox` — осознанный компромисс только для CI.** На Ubuntu 24.04 ограничены user namespaces,
  нужные sandbox Chrome. Риск: эксплойт в рендерере не изолирован от процесса браузера. Принимаем,
  т.к. VM раннера одноразовая, секретов в job нет, тесты открывают только целевой сайт. Локально флаг не используется.

### Логика статусов

- `Compile tests` (`./gradlew testClasses`) — отдельный шаг: ошибка компиляции падает тут, тесты и отчёт пропускаются.
- `Check test results exist` — если нет ни одного Allure-результата или JUnit XML, шаг падает: «0 тестов» ≠ успех.
- Отчёт и загрузка артефактов — `if: always()` + результаты есть; job при этом остаётся failure от шага тестов.

### Проверка на реальном GitHub

| Ветка | Run | Итог | Шаги | Артефакты |
|---|---|---|---|---|
| `homework` | 45035148592 | ✅ success | все 13 шагов success | allure-report 1.1 MB, junit-xml 1.6 KB, allure-results 3 KB |
| `ci-check/test-failure` (temp, `shouldHaveResult("NO_SUCH_TEXT_XYZ")`) | 45035392881 | ❌ failure | `Run UI tests` failure → проверка результатов, отчёт, 3 загрузки — success | allure-report 1.4 MB, junit-xml 3.8 KB, **allure-results 227 KB** (скриншот + page source) |
| `ci-check/compile-error` (temp, пропущена `;`) | 45035392605 | ❌ failure | `Compile tests` failure → остальные 6 шагов skipped | нет — отчёт не генерируется |

Временные ветки получали триггер `ci-check/**` только в своей копии workflow; обе удалены
(локально и на GitHub). Логи и содержимое артефактов не скачивались: `gh` не авторизован, а API
отдаёт их только с токеном — проверено по статусам шагов и размерам артефактов.

## Фаза 6 — Уведомления в Telegram (⏳ реальная отправка — после добавления секретов)

### Согласованные решения

- **Реализация:** `ci/TelegramNotifier.java` — single-file Java-программа без зависимостей
  (`java ci/TelegramNotifier.java`), переиспользуется в Jenkins (фаза 7). JDK: XML-парсер + `HttpClient`.
- **Из каких CI:** GitHub Actions (шаг `Notify Telegram`), далее Jenkins.
- **Исходы:**

| Исход | Уведомление |
|---|---|
| все тесты прошли | ✅ PASSED |
| есть failure / error, или XML зелёный, но шаг тестов упал | ❌ FAILED |
| ошибка компиляции / шаги до тестов упали (`BUILD_OUTCOME` ≠ success) | 🛑 BUILD ERROR — tests did not run (даже если лежит старый зелёный XML) |
| нет XML, битый XML, 0 тестов, все skipped | ⚠️ NO TEST RESULTS |
| отмена (новый push отменил старый run) | не отправляется (`if: !cancelled()`) |

- **Secrets:** `TELEGRAM_BOT_TOKEN`, `TELEGRAM_CHAT_ID` — только из окружения; не печатаются, токен и chat id
  вырезаются из любых текстов ошибок (`sanitize`). Нет секретов → `::warning::` «skipped», exit 0.
- **Ошибки API:** успех = HTTP 2xx **и** `"ok": true`; иначе exit 1 + warning с кодом и `description`.
  Таймауты: connect 10 с, запрос 15 с (`TELEGRAM_TIMEOUT_SECONDS`), шаг — `timeout-minutes: 2`.
  Шаг — `continue-on-error: true`: статус job определяют только тесты.
- **Статистика — из JUnit XML** Gradle (`build/test-results/test/TEST-*.xml`, формат сверен с реальным файлом):
  считаются элементы `<testcase>` во всех файлах (не атрибуты `<testsuite>` → вложенные suite не дублируются);
  failed = `<failure>`, errors = `<error>`, skipped = `<skipped>`. XML парсится с запретом DOCTYPE (XXE).

Сообщение (plain text):
```
✅ PASSED — AlinaKotlyarova/getting-started-java
CI: GitHub Actions
Branch: homework @ a33b300
Tests: 3 total · 3 passed · 0 failed · 0 skipped
Run: https://github.com/AlinaKotlyarova/getting-started-java/actions/runs/…
```

### Проверка без реальной отправки

`./gradlew notifierTest` — отдельный source set, JUnit + mock HTTP-сервер (`com.sun.net.httpserver`),
адрес API подменяется `TELEGRAM_API_URL`. **19/19 ✅.** Каждый тест дополнительно проверяет, что токен и chat id
не попали в вывод.

| Группа | Сценарии |
|---|---|
| Статистика | success, failure, error, skipped, несколько файлов + вложенные `<testsuites>` (5 тестов, а не 11 по атрибутам) |
| Нет результатов | XML отсутствует, битый XML (без `[Fatal Error]` в выводе), 0 тестов, все skipped |
| До тестов | ошибка компиляции при старом зелёном XML → BUILD ERROR; шаги до компиляции skipped → BUILD ERROR; зелёный XML + упавший шаг тестов → FAILED |
| Отправка | нет секретов (запроса нет, exit 0), HTTP 500, HTTP 400 `ok=false` + description, HTTP 200 `ok=false`, timeout (1 с, укладывается < 3.5 с), connection refused |
| Утечки | токен с пробелом → `URI.create` кладёт URL с токеном в текст исключения — вырезается. **Мутационная проверка:** с отключённым `sanitize` этот тест падает |

Запуск как single-file программы без секретов на реальном XML: текст сформирован, warning «skipped», exit 0.

### Проверка в GitHub Actions: ошибка уведомления не маскирует статус

Временные ветки с поддельным токеном и `TELEGRAM_API_URL=http://127.0.0.1:9` (закрытый порт) — в Telegram ничего не уходит:

| Ветка | Run | Тесты | Notify Telegram | Job |
|---|---|---|---|---|
| `ci-check/notify-error-on-success` | 45041584245 | success | exit 1, warning «Telegram notification failed: ConnectException» | ✅ success |
| `ci-check/notify-error-on-failure` | 45041583811 | failure | exit 1, тот же warning | ❌ failure (от тестов) |

Нюанс: при `continue-on-error` API показывает `conclusion: success` у самого шага — фактическое падение видно
по annotations (`Process completed with exit code 1` + warning). Временные ветки удалены.

Инцидент при проверке: первый push временных веток ушёл без файлов нотификатора (zsh не разбил список путей
в переменной для `git add`). Обнаружено до анализа результатов, ветки пересозданы в отдельных worktree
с явными путями и перезапушены, рабочая копия `homework` восстановлена и сверена.

### Осталось

- Реальная отправка: после добавления секретов в репозиторий и согласования — сверить фактическое сообщение.

### Найдено попутно (к фазе 5)

В stderr тестов: «Unable to find an exact match for CDP version 157 … found: 153» — Selenium 4.49 поддерживает
CDP до 153, локальный Chrome — 157. Тесты не затронуты (Selenide не использует CDP для базовых действий),
но это расхождение «браузер ↔ поддержка CDP», которое фаза 5 просила отслеживать.

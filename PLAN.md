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

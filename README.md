# Podatności Android Deep Links — demo na prezentację

Projekt demonstracyjny pokazujący najczęstsze podatności związane z deep
linkami w Androidzie oraz poprawny sposób ich zabezpieczenia. Przygotowany
na zajęcia studenckie.

## Spis treści prezentacji

1. Czym są deep linki (Custom URI Schemes vs. Android App Links)
2. **Deep Link Hijacking** — przejęcie schematu przez złośliwą aplikację
3. **Auth Bypass** — ślepe zaufanie danym z linku, pominięcie uwierzytelniania
4. **Open Redirect i WebView XSS** — niewalidowane parametry przekazane do WebView
5. Obrona: Android App Links + walidacja parametrów po stronie kodu
6. Demo na żywo (emulator + komendy `adb`)

## Zawartość repozytorium

- `VictimApp/` — aplikacja-ofiara z celowo wprowadzonymi podatnościami
  oraz ich zabezpieczoną wersją
- `AttackerApp/` — aplikacja-atakujący demonstrująca Deep Link Hijacking
- `prezentacja/` — plik prezentacji (PDF)

## Jak uruchomić demo

Aplikacje otwierasz w Android Studio i uruchamiasz na emulatorze
(API 24+). Ataki wyzwalasz komendami `adb`, które symulują kliknięcie
w deep link na urządzeniu — przykłady znajdą się w prezentacji.

## ⚠️ Zastrzeżenie

Repozytorium ma charakter **wyłącznie edukacyjny**. Aplikacje zawierają
celowo wprowadzone podatności i nie powinny być publikowane w sklepach
z aplikacjami ani instalowane poza kontrolowanym środowiskiem testowym.

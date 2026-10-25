# Podatności Android Deep Links — demo na prezentację

Projekt demonstracyjny pokazujący cztery podatności związane z deep linkami w Androidzie
oraz poprawne sposoby obrony. Ataki wyzwala się **skanując kody QR** (albo komendami `adb`).
Przygotowany na zajęcia studenckie.

## Spis treści prezentacji

1. Czym są deep linki (Custom URI Schemes vs. Android App Links)
2. **Auth Bypass** — ślepe zaufanie danym z linku, pominięcie sesji
3. **Open Redirect** — niewalidowany adres przekazany do zewnętrznej przeglądarki
4. **WebView XSS** — niezakodowany parametr wstrzyknięty do lokalnego WebView
5. **Deep Link Hijacking** — przejęcie custom scheme przez złośliwą aplikację
6. Obrona: kontrola sesji, allowlista, kodowanie wyjścia oraz zweryfikowane App Links
7. Demo na żywo (kody QR + emulator)

## Zawartość repozytorium

```
deeplink-security-demo/
├── VictimApp/        # aplikacja-ofiara: 4 podatne trasy + wersje bezpieczne
├── AttackerApp/      # aplikacja-atakujący: wyłącznie Deep Link Hijacking
├── demo-site/        # statyczna strona na Vercel (Open Redirect + App Links)
├── qr-codes/         # generator kodów QR (Python)
├── docs/screenshots/ # zrzuty z każdego scenariusza (na slajdy)
└── prezentacja/      # plik prezentacji (dodasz osobno)
```

## Mapa podatności

| Scenariusz | Deep link / adres | Aktywność (VictimApp) |
| --- | --- | --- |
| Auth Bypass | `victimapp://account` | `AccountActivity` |
| Open Redirect | `victimapp://redirect?url=<DEMO_BASE_URL>/phishing-demo` | `RedirectActivity` |
| WebView XSS | `victimapp://xss?name=<payload>` | `XssActivity` |
| Deep Link Hijacking | `victimapp://reset/confirm?token=...` | `ResetConfirmActivity` |
| Dobra praktyka (App Links) | `<DEMO_BASE_URL>/secure` | `SecureActivity` |

`AttackerApp` rejestruje **wyłącznie** `victimapp://reset/confirm` (ten sam filtr co ofiara),
więc nie koliduje z pozostałymi scenariuszami.

## Konfiguracja

Po wdrożeniu `demo-site` podmień poniższe w manifeście i generatorze QR:

| Stała | Wartość |
| --- | --- |
| `DEMO_BASE_URL` | `https://deeplink-security-demo.vercel.app` |
| `APP_LINK_HOST` | `deeplink-security-demo.vercel.app` (bez `https://`, do manifestu `SecureActivity`) |

## Przygotowanie

1. **Aplikacje** — otwórz `VictimApp/` i `AttackerApp/` w Android Studio, zbuduj i zainstaluj na
   emulatorze (API 24+, włączone KVM).
2. **demo-site** — wdróż na Vercel z **Root Directory = `demo-site`**, preset „Other" (statyczny).
3. **Kody QR** — `cd qr-codes && pip install "qrcode[pil]" && python3 generate_qr.py <DEMO_BASE_URL>`.

## Scenariusz pokazu (dwufazowy)

**Faza 1 — same podatności (zainstalowany tylko VictimApp):**

- *Auth Bypass:* wyloguj się, zeskanuj QR `account` → chronione konto otwiera się mimo braku sesji
  (czerwony baner).
- *Open Redirect:* zeskanuj QR `redirect` → przeglądarka otwiera stronę „ShopDemo".
- *WebView XSS:* zeskanuj QR `xss` → wyskakuje `alert` z wykradzionym z DOM numerem konta.

**Faza 2 — Hijacking (zainstaluj też AttackerApp):**

- zeskanuj QR `reset/confirm` → system pokazuje okno wyboru (VictimApp vs AttackerApp) → wybór
  AttackerApp → ekran „HACKED!" z przechwyconym URI i tokenem.

**Dobra praktyka (App Links):** po weryfikacji (sekcja niżej) QR `secure` otwiera `SecureActivity`
bez okna wyboru.

## Macierz: podatne vs bezpieczne

| Scenariusz | Zachowanie podatne | Obrona |
| --- | --- | --- |
| Auth Bypass | `AccountActivity` nie sprawdza sesji | kontrola sesji przed pokazaniem danych (`SafeDemoActivity`) |
| Open Redirect | forward do dowolnego `url` | allowlista dokładnych hostów, tylko HTTPS, bez userinfo/portu |
| WebView XSS | `name` wstrzyknięty do HTML bez kodowania | tekst w `TextView` / kodowanie, JS wyłączony gdy zbędny |
| Hijacking | custom scheme można przejąć | zweryfikowane App Links (HTTPS + `autoVerify`) |

Uwaga: App Links naprawiają **tylko** Hijacking. Pozostałe trzy wymagają własnych zabezpieczeń.

## Komendy adb (wariant awaryjny)

```bash
adb shell am start -a android.intent.action.VIEW -d "victimapp://account"
adb shell am start -a android.intent.action.VIEW -d "victimapp://redirect?url=https://deeplink-security-demo.vercel.app/phishing-demo"
adb shell am start -a android.intent.action.VIEW -d "victimapp://xss?name=%3Cimg%20src%3Dx%20onerror%3D%22alert(document.getElementById('secret').textContent)%22%3E"
adb shell am start -a android.intent.action.VIEW -d "victimapp://reset/confirm?token=DEMO_RESET_TOKEN_123"
```

Gdy aparat nie otwiera własnych schematów URI, otwórz stronę główną `<DEMO_BASE_URL>/` i użyj przycisków „Zobacz demo".

## Strony demo-site (adresy)

Strony `/phishing-demo` i `/secure` nie są podlinkowane ze strony głównej (są celami demo,
nie częścią nawigacji). W razie potrzeby wpisz adres ręcznie w przeglądarce:

| Adres | Rola |
| --- | --- |
| `<DEMO_BASE_URL>/` | strona główna — opis ataków + przyciski „Zobacz demo" (także wariant awaryjny) |
| `<DEMO_BASE_URL>/phishing-demo` | fałszywy „Sklep z leśnymi stworami" (cel Open Redirectu) |
| `<DEMO_BASE_URL>/secure` | strona zweryfikowanego App Linku |

Strona główna wymaga obrazków `demo-site/gnome.jpeg` (sklep) i `demo-site/wizard.jpeg`
(strona secure).

## Wdrożenie demo-site i weryfikacja App Links

Sam wpis `autoVerify="true"` w manifeście **nie wystarcza** — potrzebny jest hosting.

1. **Fingerprint** certyfikatu (debug): w `VictimApp/` uruchom `./gradlew signingReport` i odczytaj
   SHA-256.
2. Wstaw fingerprint do `demo-site/.well-known/assetlinks.json` (pole `sha256_cert_fingerprints`).
3. Wdróż `demo-site` na Vercel i **wyłącz Deployment Protection** (weryfikator wchodzi anonimowo).
4. Sprawdź hosting realnym żądaniem GET — oczekiwane `200`, `application/json`, bez przekierowania:
   ```bash
   curl -sS -i https://deeplink-security-demo.vercel.app/.well-known/assetlinks.json
   ```
5. Podmień `APP_LINK_HOST` w `VictimApp/app/src/main/AndroidManifest.xml` na realną domenę, zbuduj
   i zainstaluj aplikację.
6. Zweryfikuj na urządzeniu:
   ```bash
   adb shell pm verify-app-links --re-verify com.example.victimapp
   adb shell pm get-app-links com.example.victimapp
   ```
   `pm get-app-links` pokazuje stan weryfikacji **hosta** (nie ścieżki `/secure`); obsługę do
   `/secure` ogranicza manifest (`pathPrefix`).

## Przed pokazem

Wyczyść domyślne powiązania linków, inaczej wcześniejszy wybór pominie okno wyboru przy Hijackingu:
**Ustawienia → Aplikacje → (VictimApp / AttackerApp) → „Otwieranie linków" → wyczyść domyślne**
(albo przeinstaluj aplikacje).

## Screenshoty

W `docs/screenshots/` zapisz zrzut z KAŻDEGO scenariusza (wersja podatna i bezpieczna) — na
prezentacji kod nie jest pisany na żywo, więc slajdy opierają się na gotowych zrzutach.

## ⚠️ Zastrzeżenie

Repozytorium ma charakter **wyłącznie edukacyjny**. Aplikacje zawierają celowo wprowadzone
podatności, a strona „ShopDemo" niczego nie wysyła ani nie zapisuje. Nie publikuj tego w sklepach
z aplikacjami ani nie instaluj poza kontrolowanym środowiskiem testowym.

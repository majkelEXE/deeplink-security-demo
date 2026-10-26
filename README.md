# Podatności Android Deep Links — demo na prezentację

Projekt demonstracyjny pokazujący cztery podatności związane z deep linkami w Androidzie
oraz poprawne sposoby obrony. Ataki wyzwala się **skanując kody QR** (albo komendami `adb`).
Przygotowany na zajęcia studenckie.

Gotowe do instalacji APK (debug) są w **GitHub Releases `v1.0`** (`VictimApp.apk`, `AttackerApp.apk`);
kody QR do ich pobrania oraz do wszystkich scenariuszy znajdują się w `qr-codes/`.

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
└── docs/screenshots/ # zrzuty z każdego scenariusza (na slajdy)
```

## Mapa podatności

| Scenariusz | Deep link / adres | Aktywność (VictimApp) |
| --- | --- | --- |
| Auth Bypass | `victimapp://account` | `AccountActivity` |
| Open Redirect | `victimapp://redirect?url=https://deeplink-security-demo.vercel.app/phishing-demo` | `RedirectActivity` |
| WebView XSS | `victimapp://xss?name=<payload>` | `XssActivity` |
| Deep Link Hijacking | `victimapp://reset/confirm?token=...` | `ResetConfirmActivity` |
| Dobra praktyka (App Links) | `https://deeplink-security-demo.vercel.app/secure` | `SecureActivity` |

`AttackerApp` rejestruje **wyłącznie** `victimapp://reset/confirm` (ten sam filtr co ofiara),
więc nie koliduje z pozostałymi scenariuszami.

## Scenariusz pokazu (dwufazowy)

**Faza 1 — same podatności (zainstalowany tylko VictimApp):**

- *Auth Bypass:* wyloguj się, zeskanuj QR `account` → chronione konto otwiera się mimo braku sesji
  (czerwony baner).
- *Open Redirect:* zeskanuj QR `redirect` → przeglądarka otwiera stronę „ShopDemo".
- *WebView XSS:* zeskanuj QR `xss` → wyskakuje `alert` z wykradzionym z DOM numerem konta.

**Faza 2 — Hijacking (zainstaluj też AttackerApp):**

- zeskanuj QR `reset/confirm` → system pokazuje okno wyboru (VictimApp vs AttackerApp) → wybór
  AttackerApp → ekran „HACKED!" z przechwyconym URI i tokenem.

**Dobra praktyka (App Links):** QR `secure` otwiera `SecureActivity` bez okna wyboru
(App Link jest już zweryfikowany przez `assetlinks.json` na Vercel).

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

Gdy aparat nie otwiera własnych schematów URI, otwórz stronę główną
`https://deeplink-security-demo.vercel.app/` i użyj przycisków w sekcjach ataków
(każdy scenariusz ma przycisk „zwykły link" oraz „atak").

## Strony demo-site (adresy)

Strony `/phishing-demo` i `/secure` nie są podlinkowane ze strony głównej (są celami demo,
nie częścią nawigacji). W razie potrzeby wpisz adres ręcznie w przeglądarce:

| Adres | Rola |
| --- | --- |
| `https://deeplink-security-demo.vercel.app/` | strona główna — opis ataków + przyciski (zwykły link vs atak) |
| `https://deeplink-security-demo.vercel.app/phishing-demo` | fałszywy „Sklep z leśnymi stworami" (cel Open Redirectu) |
| `https://deeplink-security-demo.vercel.app/secure` | strona zweryfikowanego App Linku |

## Przed pokazem

Wyczyść domyślne powiązania linków, inaczej wcześniejszy wybór pominie okno wyboru przy Hijackingu:
**Ustawienia → Aplikacje → (VictimApp / AttackerApp) → „Otwieranie linków" → wyczyść domyślne**
(albo przeinstaluj aplikacje).

## ⚠️ Zastrzeżenie

Repozytorium ma charakter **wyłącznie edukacyjny**. Aplikacje zawierają celowo wprowadzone
podatności, a strona „ShopDemo" niczego nie wysyła ani nie zapisuje. Nie publikuj tego w sklepach
z aplikacjami ani nie instaluj poza kontrolowanym środowiskiem testowym.

# Kody QR

Generator kodów QR do pokazu podatności deep linków.

## Uruchomienie

```bash
pip install "qrcode[pil]"
python3 generate_qr.py                               # domyślny DEMO_BASE_URL
python3 generate_qr.py https://moja-domena.vercel.app  # własna domena
```

Po wdrożeniu `demo-site` na Vercel przekaż realną domenę (argument lub zmienna
`DEMO_BASE_URL`) i wygeneruj kody ponownie — dotyczy to Open Redirectu, App Linku
i strony awaryjnej.

## Generowane kody

| Plik | Zawartość |
| --- | --- |
| `qr_auth_bypass.png` | `victimapp://account` |
| `qr_open_redirect.png` | `victimapp://redirect?url=<DEMO_BASE_URL>/phishing-demo` |
| `qr_webview_xss.png` | `victimapp://xss?name=<payload XSS>` (lokalny, zakodowany) |
| `qr_hijacking.png` | `victimapp://reset/confirm?token=DEMO_RESET_TOKEN_123` |
| `qr_secure_app_link.png` | `<DEMO_BASE_URL>/secure` |
| `qr_demo_links.png` | `<DEMO_BASE_URL>/` (strona główna — wszystkie ataki z przyciskami „Zobacz demo") |

Kody `auth_bypass`, `webview_xss` i `hijacking` nie zależą od domeny.

## Wariant awaryjny

Gdy aparat nie otwiera własnych schematów URI, otwórz stronę główną `<DEMO_BASE_URL>/`
i użyj przycisków „Zobacz demo"
(projekt `demo-site`) — ma klikalne linki `victimapp://` oraz polecenia `adb`.

## Poza zakresem (na później)

QR do pobrania plików APK powstaną dopiero po wydaniu podpisanych APK (GitHub Releases).

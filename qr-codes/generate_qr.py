#!/usr/bin/env python3
"""Generate QR codes for the deep link security demo.

Each attack is a deep link; URL parameters are percent-encoded with urllib.parse
(quote_via=quote), so spaces become %20 and payloads with special characters stay
intact for Android's Uri.getQueryParameter().

Usage:
    pip install "qrcode[pil]"
    python3 generate_qr.py                              # default base URL
    python3 generate_qr.py https://my-app.vercel.app    # override base URL
    DEMO_BASE_URL=https://my-app.vercel.app python3 generate_qr.py
"""

import os
import sys
from urllib.parse import quote, urlencode

import qrcode

DEFAULT_BASE_URL = "https://deeplink-security-demo.vercel.app"

# The XSS payload is executed locally by VictimApp; here it is only percent-encoded.
XSS_PAYLOAD = "<img src=x onerror=\"alert(document.getElementById('secret').textContent)\">"

OUTPUT_DIR = os.path.dirname(os.path.abspath(__file__))


def build_targets(base_url):
    """Map output file name -> deep link / URL encoded for the QR code."""
    phishing_url = f"{base_url}/phishing-demo"
    return {
        "qr_auth_bypass": "victimapp://account",
        "qr_open_redirect": "victimapp://redirect?" + urlencode({"url": phishing_url}, quote_via=quote),
        "qr_webview_xss": "victimapp://xss?" + urlencode({"name": XSS_PAYLOAD}, quote_via=quote),
        "qr_hijacking": "victimapp://reset/confirm?" + urlencode({"token": "DEMO_RESET_TOKEN_123"}, quote_via=quote),
        "qr_secure_app_link": f"{base_url}/secure",
        "qr_demo_links": f"{base_url}/demo-links",
    }


def main():
    base_url = (
        sys.argv[1] if len(sys.argv) > 1
        else os.environ.get("DEMO_BASE_URL", DEFAULT_BASE_URL)
    ).rstrip("/")

    targets = build_targets(base_url)
    print(f"Base URL: {base_url}")
    for name, content in targets.items():
        image = qrcode.make(content)
        path = os.path.join(OUTPUT_DIR, f"{name}.png")
        image.save(path)
        print(f"  {name}.png  <-  {content}")
    print(f"Done: {len(targets)} QR codes written to {OUTPUT_DIR}")


if __name__ == "__main__":
    main()

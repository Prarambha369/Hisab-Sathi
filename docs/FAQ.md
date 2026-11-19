# Hisab Sathi FAQ

Frequently asked questions about Hisab Sathi (हिसाब साथी), the private SMS expense tracker for Nepal. New here? Start with the [User Guide](USER_GUIDE.md).

## Basics

**What does Hisab Sathi do?**
It reads the transaction SMS your bank or wallet already sends you (eSewa, Khalti, Nabil, NMB…), turns them into a timeline of income and spending in NPR, and adds categories, budgets, subscriptions, analytics and an offline AI assistant.

**Is it free?**
Yes, free and open source (GPL-3.0). No ads, no subscriptions, no paid tier.

**Which phones does it support?**
Android 8.0 (Oreo) or newer. The AI assistant needs more memory: see [AI assistant](#ai-assistant).

**Do I need an account or internet?**
No account. Tracking works fully offline. The internet is only used to download an AI model you choose, to fetch exchange rates for foreign-currency transactions, and (standard build) to check Google Play for updates.

## Banks and SMS

**Which banks and wallets are supported?**
eSewa, Khalti, Nabil Bank, NMB Bank, Himalayan Bank, Laxmi Sunrise Bank, Siddhartha Bank and Everest Bank. Planned ones are listed in [Bank support](BANK_SUPPORT.md).

**My bank isn't supported. What can I do?**
1. Open Settings → SMS → **Unrecognized Messages** and **teach** the app the message format. It will then recognise similar messages.
2. [Request your bank](https://github.com/Prarambha369/Hisab-Sathi/issues/new?template=3-bank_support_request.yml) with a few SMS samples, with personal details masked.

**A transaction has the wrong amount, merchant or category.**
Edit it in place. For categories, add a **Smart Rule** so it's right next time. To help fix the parser, open the transaction and tap **Report**: a pre-filled GitHub form opens with long numbers masked, and nothing is posted until you press Submit.

**Old transactions are missing.**
The first scan reads the last few months. Change it in Settings → SMS → **Scan Settings** (more months, or all time) and scan again.

**Why does it need SMS permission?**
Bank and wallet alerts arrive as SMS; that's the only way to track automatically without your bank login. Messages are processed on the phone and never uploaded. You can also add transactions by hand without granting it.

**I get duplicates or an OTP/promo shown as a transaction.**
Delete it, and please [report it](https://github.com/Prarambha369/Hisab-Sathi/issues/new?template=2-sms_parsing.yml) so the parser can be fixed.

## Money and Nepal

**What currency does it use?**
NPR, shown as रु 1,23,456 with Nepali lakh grouping. Foreign-currency transactions are converted to NPR using public exchange rates.

**Does it follow the Nepali fiscal year?**
Yes. The "Current FY" filter starts on Shrawan 1 (approximated as 16 July).

**Can I track cash, wallets and credit cards?**
Yes. Add bank accounts, wallets and credit cards under Accounts, and update balances or outstanding amounts by hand at any time.

## AI assistant

**Which AI model should I download?**
Open Settings → AI Chat Assistant. The AI Models screen reads your phone's RAM and free storage and marks one model **Recommended for your phone**. Roughly:

| Phone RAM | Good choice |
|---|---|
| 2–3 GB | SmolLM 135M (167 MB) or Qwen 2.5 0.5B (547 MB) |
| 4 GB | Qwen 2.5 0.5B |
| 6–8 GB | Qwen 2.5 1.5B (1.6 GB); try DeepSeek R1 for step-by-step maths |
| 12 GB+ | Phi-4 mini (3.9 GB), the strongest |

These are estimates; a bigger model gives better answers but replies more slowly and uses more battery.

**Is the AI private?**
Yes. It runs entirely on your phone. Questions and your spending summary never leave the device.

**Why is there no Gemma or Qwen 3?**
Gemma's licence requires a Hugging Face login to download, which the app can't do for you. Qwen 3 is only published in a format the app's AI engine (MediaPipe) can't load yet.

**The chat says memory is full.**
Small models remember about 1,000 words of conversation. Clear the chat to start fresh, or use a model with a larger context (Qwen 1.5B, DeepSeek R1, Phi-4 mini).

**The download is slow or failed.**
Models are large; use Wi-Fi. Make sure you have the free space shown on the card. Cancel and retry if it stalls.

## Data, backup and security

**Where is my data stored?**
Only on your phone, in the app's private storage. See the [Privacy Policy](../PRIVACY.md).

**How do I move to a new phone?**
Settings → Data Privacy → export a backup (`.zip`), copy it to the new phone, then import it there. The backup is **not encrypted**, so keep it private.

**Can I export to Excel?**
Export transactions as CSV from the Transactions screen. Excel and Google Sheets open CSV files.

**Can I lock the app?**
Yes: Settings → Data Privacy → **App Lock** uses your fingerprint, face or screen lock.

**How do I delete everything?**
About → **Delete All Data**, or uninstall the app.

## Installing and updating

**Where do I download it?**
From [GitHub Releases](https://github.com/Prarambha369/Hisab-Sathi/releases). Use the `…-standard-release.apk`; the `fdroid` APK has no Google services. Builds are signed with the Hisab Sathi release key: see [Release verification](RELEASE.md).

**Android says "App not installed" when updating.**
You probably have an older debug build signed with a different key. Back up, uninstall it, then install the new APK.

## Help

- [Report a bug](https://github.com/Prarambha369/Hisab-Sathi/issues/new?template=1-bug_report.yml) · [Send feedback](https://github.com/Prarambha369/Hisab-Sathi/issues/new?template=5-feedback.yml) · [Suggest a feature](https://github.com/Prarambha369/Hisab-Sathi/issues/new?template=4-feature_request.yml)
- Security problems: [report privately](https://github.com/Prarambha369/Hisab-Sathi/security/advisories/new)

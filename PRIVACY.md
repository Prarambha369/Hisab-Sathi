# Privacy Policy

**Hisab Sathi (हिसाब साथी)** · Last updated: 20 November 2026

Hisab Sathi is an open-source expense tracker for Nepal. It reads your bank and wallet SMS and keeps your money records **on your phone**. There is no Hisab Sathi server, no account to create, and nothing about you is sent to us.

## In short

- Your transactions, SMS, chats and settings are stored **only on your phone**.
- The app has **no analytics, no ads, no crash reporting and no tracking**.
- The app only goes online to (1) download an AI model you choose, (2) fetch currency exchange rates, and (3) in the Play Store/standard build, check Google Play for updates. **None of these requests contain your financial data.**
- You can export, back up or delete everything at any time.

## What is stored on your phone

Everything below lives in the app's private storage (a Room/SQLite database and app files), protected by Android's app sandbox. Other apps can't read it.

| Data | Why |
|---|---|
| Transactions: amount, merchant, date, category, account, notes, tags | The core of the app |
| **The original SMS text** of each detected transaction or subscription | Lets you check what was detected and report a wrong one |
| **Unrecognized SMS** from banks and wallets (Settings → SMS → Unrecognized Messages) | So you can teach the app new formats or report them |
| Accounts and cards (bank name, last 4 digits, balances you enter) | Balances and filters |
| Categories, rules, budgets, subscriptions, merchant mappings | Your organisation of the data |
| AI chat messages, and the summary of your recent spending given to the AI | So the assistant can answer about your money |
| Attachments, profile picture and banner you add | Shown in the app |
| Downloaded exchange rates and AI model files | Offline use |
| Preferences (theme, app lock, scan period, selected AI model…) | Settings |

Full account numbers are not needed. The app keeps the last 4 digits that banks put in their SMS.

## Permissions

| Permission | Used for |
|---|---|
| **Read SMS / Receive SMS** | Find bank and wallet transaction messages, both existing ones (for the scan period you choose) and new ones as they arrive. All parsing happens on the phone. The app can't send or change messages. |
| **Notifications** | Payment and scan reminders, and new-transaction alerts |
| **Exact alarms, run at startup** | Deliver those reminders on time, including after a restart |
| **Biometrics** | Optional App Lock |
| **Internet** | Only the three uses listed in the next section |

Hisab Sathi does **not** ask for location, contacts, camera, microphone or phone-call access. The advertising ID permission is removed from the app.

## When the app uses the internet

1. **AI model download (only if you choose one).** Settings → AI Chat Assistant → AI Models downloads a model file from Hugging Face (`huggingface.co/litert-community`) through Android's Download Manager. Hugging Face sees a normal file download (your IP address and the file requested), as with any website. After that, the AI runs **completely offline** on your phone.
2. **Exchange rates.** To convert foreign-currency transactions to NPR, the app downloads public rate tables from `cdn.jsdelivr.net` (fallback `currency-api.pages.dev`). The request only names a currency code, such as `npr.json`.
3. **Google Play (standard build only).** The standard build uses Google Play's in-app update and review services. The F-Droid build contains no Google services.

The app makes no other network requests. You can check this yourself in the source code.

## AI assistant

- Runs on your phone with Google's MediaPipe LLM Inference. Your questions and your spending summary are **never sent anywhere**.
- Models come from their publishers under open licences (Qwen: Apache 2.0, DeepSeek R1 Distill: MIT, Phi-4 mini: MIT, SmolLM: Apache 2.0). See About → Licenses.
- Delete a model or clear the chat at any time.

## Things only you can share

The app never shares anything on its own. These leave your phone only when you do them:

- **Backups** (Settings → Data Privacy → export): a `.zip` with your data as JSON plus attachments and profile images. **The backup is not encrypted**, so store it somewhere private.
- **CSV export** of transactions, saved or shared wherever you pick.
- **Report buttons** (wrong transaction, unrecognized SMS, bug report, feedback) open a **public** GitHub issue form in your browser, already filled in. Long numbers (account, card, phone) are masked first, and nothing is posted until you review it and press Submit on GitHub.

## Deleting your data

- Delete single transactions, accounts, chats or AI models in the app.
- About → **Delete All Data** permanently clears everything.
- Uninstalling the app removes all of its data from the phone. Export a backup first if you want to keep it.

## Children

Hisab Sathi is not directed at children under 13 and collects no information from anyone.

## Open source

The full source is at [github.com/Prarambha369/Hisab-Sathi](https://github.com/Prarambha369/Hisab-Sathi) under the GPL-3.0 licence. Anyone can check these claims.

## Changes

Changes to this policy are made in the repository, dated above, and mentioned in the release notes.

## Contact

Questions or concerns: [open an issue](https://github.com/Prarambha369/Hisab-Sathi/issues/new/choose). For security problems, use a [private security report](https://github.com/Prarambha369/Hisab-Sathi/security/advisories/new).

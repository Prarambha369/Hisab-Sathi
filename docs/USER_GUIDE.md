# Hisab Sathi User Guide

How to set up and use Hisab Sathi (हिसाब साथी). Questions not covered here are probably in the [FAQ](FAQ.md).

## 1. Install

1. Download the latest `HisabSathi-…-standard-release.apk` from [GitHub Releases](https://github.com/Prarambha369/Hisab-Sathi/releases). The `fdroid` APK is the same app without Google services.
2. Open it and allow your browser or file manager to install apps when Android asks.
3. Updates install over the old version as long as they're signed with the same key ([how to verify](RELEASE.md)).

## 2. First launch

The onboarding takes about a minute:

1. **Profile**: enter the name the app should greet you by.
2. **Notifications**: allow them for bill reminders and scan reminders.
3. **SMS access**: allow it so the app can read transaction SMS from eSewa, Khalti and Nepali banks. Messages are processed on your phone and never uploaded. You can skip this and add transactions by hand.
4. **Verify accounts**: the app lists the banks and wallets it found in your SMS. Pick your **Main** account; manual entries default to it.
5. Or add your main account yourself: bank name, current balance and the last 4 digits of the account number.

## 3. Everyday use

### Home
Your balance, this month's spending and income, recent transactions, upcoming payments and budgets at a glance. Reorder or hide the widgets with the edit-widgets option on Home.

### Transactions
- New bank or wallet SMS are added automatically as they arrive.
- Tap a transaction to change its category, merchant, account or notes, attach a receipt, or mark it as **recurring**.
- **Add New** (the + button) records cash or anything without an SMS.
- Filter by date (this month, **Current FY** from Shrawan 1, custom range), account, category or type.
- Export the list as **CSV** for Excel or Google Sheets.

### Categories and Smart Rules
- Settings → **Categories**: edit expense and income categories and subcategories.
- Settings → **Smart Rules**: automatically categorise or rename transactions that match a merchant, keyword or amount, e.g. everything from "Bhatbhateni" goes to *Groceries*. Templates are available to start from.

### Budgets
Settings → **Budgets** (or the Home widget): set a monthly limit overall or per category and watch progress through the month.

### Subscriptions and bills
Recurring payments such as internet, Ncell/NTC recharge, Netflix or loan EMIs appear under **Subscriptions**, with reminders before they're due (Settings → Notifications).

### Accounts
Settings → **Accounts** (Bank Accounts, Wallets, Credit Cards): update balances or credit card outstanding, merge duplicates, and link cards to accounts.

### Analytics
Trends over time, top categories and top merchants for any period.

## 4. When a message isn't recognised

1. Open Settings → SMS → **Unrecognized Messages**.
2. Tap a message → **Teach**, and mark which parts are the amount, merchant, balance and so on. Similar messages are then recognised automatically.
3. Or tap **Report** to send it to the developers so your bank can be fully supported. Long numbers are masked, and you review everything on GitHub before submitting.

To scan older messages, use Settings → SMS → **Scan Settings** (choose the number of months or all time) and scan again.

## 5. AI assistant (offline)

1. Settings → **AI Chat Assistant** opens **AI Models**.
2. The screen shows your phone's RAM and free storage, and labels each model: **Recommended for your phone**, Runs well, May be slow, Too heavy or Not enough storage. Each also has a trait: Lightest, Fast, Balanced, Reasoning or Most powerful.
3. Tap **Download** on the recommended one (Wi-Fi is best; models are 167 MB to 3.9 GB).
4. Open the AI chat from the Home screen and ask things like *"How much did I spend on food this month?"* or *"Which subscriptions are due this week?"*.
5. Switch models any time with **Use for chat**, or **Delete** ones you don't need to free space.

The AI runs entirely on your phone; nothing you ask is sent anywhere. Answers come from small models and can be wrong, so check important numbers in your transactions.

## 6. Backup, privacy and security

- **Backup**: Settings → Data Privacy → export creates a `.zip` you can save to Drive, a computer or a USB stick. Import it on a new phone. It is **not encrypted**, so keep it private.
- **App Lock**: Settings → Data Privacy → App Lock asks for fingerprint, face or screen lock when the app opens, with a timeout you choose.
- **Delete everything**: About → Delete All Data.
- Read the full [Privacy Policy](../PRIVACY.md).

## 7. Personalise

Settings → **Appearance**: light, dark or AMOLED black theme, accent colours, fonts, navigation bar style, blur effects and an alternative app icon (Original, Anarchy, Zenith).

## 8. Get help or help out

- [Report a bug](https://github.com/Prarambha369/Hisab-Sathi/issues/new?template=1-bug_report.yml) (About → Report Bug fills in your app version and phone)
- [Request your bank or wallet](https://github.com/Prarambha369/Hisab-Sathi/issues/new?template=3-bank_support_request.yml)
- [Send feedback](https://github.com/Prarambha369/Hisab-Sathi/issues/new?template=5-feedback.yml)
- Developers: see [CONTRIBUTING.md](../CONTRIBUTING.md) and [adding a bank parser](CONTRIBUTING_BANK_PARSERS.md)

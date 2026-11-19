# Bank SMS Support Matrix — Hisab Sathi (हिसाब साथी)

> **Nepal-exclusive.** All parsers live in
> `parser-core/src/main/kotlin/com/prarambha/parser/core/bank/` and output **NPR**.
> This matrix is derived from the parser implementations, not assumptions.

## Supported Institutions (8)

### Digital Wallets

| Institution | Parser | Amount | Debit/Credit | Merchant | Account Last-4 | Reference | Notes |
|---|---|---|---|---|---|---|---|
| **eSewa** | `EsewaParser.kt` | ✅ | ✅ (expense/income) | ✅ | ✅ | ✅ | Wallet balance SMS filtered; transaction keywords like paid/received |
| **Khalti** | `KhaltiParser.kt` | ✅ | ✅ (expense/income) | ✅ | ✅ | ✅ | Khalti Digital sender IDs + DLT pattern |

### Commercial Banks

| Institution | Parser | Amount | Debit/Credit | Merchant | Account Last-4 | Reference | Notes |
|---|---|---|---|---|---|---|---|
| **Nabil Bank** | `NabilBankParser.kt` | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ See guard note below |
| **NMB Bank** | `NMBBankParser.kt` | ✅ | ✅ | ✅ | ✅ | ✅ | Matches `NMB` senders but excludes `NIMB` (Nepal Investment Mega Bank) |
| **Everest Bank** | `EverestBankParser.kt` | ✅ | ✅ | ✅ | ✅ | ✅ | Accepts ATM short codes (`CWRD`) and 7–10 digit numeric senders |
| **Laxmi Sunrise Bank** | `LaxmiBankParser.kt` | ✅ | ✅ | ✅ | ✅ | ✅ | LAXMI sender family + DLT pattern |
| **Siddhartha Bank** | `SiddharthaBankParser.kt` | ✅ | ✅ | ✅ | ✅ | ✅ | Matches SBL/SIDDHARTHA senders |
| **Himalayan Bank** | `HimalayanBankParser.kt` | ✅ | ✅ | ✅ | ✅* | ✅* | Overrides amount and merchant; account and reference use the shared base patterns |

`*` handled by the shared base-class patterns in `BankParser.kt`
(`CompiledPatterns.Account` / `CompiledPatterns.Reference` / `CompiledPatterns.Balance`).

### Sender IDs (from `canHandle()`)

| Institution | Sender IDs |
|---|---|
| eSewa | `ESEWA_ALERT`, `ESEWA`, `*ESEWALIMITED*`, `XX-ESEWA-X` (DLT) |
| Khalti | `KHALTI_ALERT`, `KHALTI`, `*KHALTIDIGITAL*`, `XX-KHALTI-X` (DLT) |
| Nabil Bank | `NABIL_ALERT`, `NABIL`, `*NABILBANK*`, `XX-NABIL-X` (DLT) |
| NMB Bank | `NMB_ALERT`, `NMBBANK`, `*NMB*` except `*NIMB*` |
| Everest Bank | `EVEREST`, `*EVERESTBANK*`, `*EBL*`, `UJJ SH`, `CWRD` (ATM), 7–10 digit numeric senders, `XX-EVEREST-X` |
| Laxmi Sunrise | `LAXMI_ALERT`, `*LAXMI*`, `*LAXMISUNRISE*`, `XX-LAXMI-X` |
| Siddhartha Bank | `SBL_ALERT`, `*SBL*`, `*SIDDHARTHA*` |
| Himalayan Bank | `HBL_ALERT`, `HBL`, `*HIMALAYANBANK*`, `XX-HBL-X` (DLT) |

### Universal capabilities (base class, all parsers)

- **OTP / verification-code filtering** — non-transaction SMS are never parsed
- **Card metadata** — card type, due date, minimum due, available limit (regex ready; Nepali card SMS formats on TODO)
- **Balance extraction** via `CompiledPatterns.Balance` (`NPR`, `Rs`, `रु` after Bal/Balance/Avl Bal/Available Balance)
- **Currency** — NPR on every parsed transaction

## Known Issues (tracked in `TODO.md`)

1. `EverestBankParser.canHandle()` matches any 7–10 digit numeric sender — overly broad for an SMS-parsing app.
2. Credit card transaction handling is not wired per-bank yet.

## Parser Registry API

```kotlin
// Singleton (used throughout the app)
val parser = BankParserFactory.getParser(senderAddress)
val transaction: ParsedTransaction? = parser?.parse(smsBody, senderAddress, timestamp)

// Injectable registry (recommended for DI / testing)
val registry = BankParserRegistry.default()
```

Parse order in `BankParserFactory` (first match wins):
`Esewa → Khalti → Nabil → NMB → Everest → Laxmi Sunrise → Siddhartha → Himalayan`

## Roadmap: every Nepali money institution

The full list of institutions a Nepali user's money might move through, so parser work can be planned. The list follows Nepal Rastra Bank's license classes as of mid-2025. Mergers change it often, so check NRB's list of licensed institutions (nrb.org.np) before starting one, and update this table when a merger happens.

**How to add one:** a parser needs real SMS samples. Collect 3–5 transaction SMS per institution (debit, credit, ATM/QR if possible) with account numbers and names masked. File them with the [Bank Support Request template](https://github.com/Prarambha369/Hisab-Sathi/issues/new?template=bank_support_request.md), then follow `docs/CONTRIBUTING_BANK_PARSERS.md` and `docs/parser-test-standards.md`. Never write a parser from a guessed SMS format. Until a parser exists, users can teach the app a message's shape from **Settings → Unrecognized SMS**.

Status: ✅ supported · ⬜ not started (needs SMS samples)

Priority: **P1** large customer base, build next · **P2** worth adding · **P3** rarely sends transaction SMS

### Commercial banks (NRB class A, 20)

| Institution | Ownership | Status | Priority | Notes |
|---|---|---|---|---|
| Nabil Bank | Private | ✅ | — | |
| NMB Bank | Private | ✅ | — | |
| Himalayan Bank | Private | ✅ | — | Merged with Civil Bank |
| Laxmi Sunrise Bank | Private | ✅ | — | Laxmi + Sunrise merger |
| Siddhartha Bank | Private | ✅ | — | |
| Everest Bank | Private | ✅ | — | |
| Global IME Bank | Private | ⬜ | P1 | One of the largest by customers |
| NIC Asia Bank | Private | ⬜ | P1 | |
| Nepal Investment Mega Bank (NIMB) | Private | ⬜ | P1 | NIBL + Mega merger; NMB parser already excludes `NIMB` senders |
| Prabhu Bank | Private | ⬜ | P1 | |
| Nepal Bank Ltd (NBL) | State | ⬜ | P1 | Oldest bank, very large customer base |
| Rastriya Banijya Bank (RBB) | State | ⬜ | P1 | Largest state bank |
| Kumari Bank | Private | ⬜ | P2 | |
| Prime Commercial Bank | Private | ⬜ | P2 | Logo already bundled (`ic_brand_prime_commercial_bank`) |
| Sanima Bank | Private | ⬜ | P2 | |
| Machhapuchchhre Bank | Private | ⬜ | P2 | |
| Citizens Bank International | Private | ⬜ | P2 | |
| Agricultural Development Bank (ADBL) | State | ⬜ | P2 | |
| Nepal SBI Bank | Joint venture | ⬜ | P2 | |
| Standard Chartered Bank Nepal | Foreign JV | ⬜ | P2 | Logo already bundled (`ic_brand_standard_chartered`) |

### Development banks (NRB class B, about 17; verify against NRB)

All ⬜, P2 unless noted: Jyoti Bikas Bank, Garima Bikas Bank, Muktinath Bikas Bank (P1 within this class, the largest), Shine Resunga Development Bank, Kamana Sewa Bikas Bank, Lumbini Bikas Bank, Mahalaxmi Bikas Bank, Shangrila Development Bank, Excel Development Bank, Miteri Development Bank, Narayani Development Bank, Karnali Development Bank, Sindhu Bikas Bank, Green Development Bank, Salapa Bikas Bank, Saptakoshi Development Bank, Corporate Development Bank.

### Finance companies (NRB class C, about 17; verify against NRB)

All ⬜, P3: ICFC Finance, Goodwill Finance, Guheshwori Merchant Banking & Finance, Gurkhas Finance, Manjushree Finance, Pokhara Finance, Progressive Finance, Reliance Finance, Samriddhi Finance, Shree Investment & Finance, Central Finance, Best Finance, Janaki Finance, Multipurpose Finance, Nepal Finance, Nepal Share Markets & Finance, Capital Merchant Banking & Finance.

### Microfinance (NRB class D, about 50)

P3. Most members don't receive SMS alerts. If samples appear, start with the largest: Nirdhan Utthan, Chhimek, Swabalamban, Deprosc, Forward, Sana Kisan Bikas and NMB Laghubitta.

### Digital wallets and payment apps (NRB-licensed PSPs)

| Institution | Status | Priority | Notes |
|---|---|---|---|
| eSewa | ✅ | — | |
| Khalti | ✅ | — | |
| IME Pay | ⬜ | P1 | |
| Prabhu Pay | ⬜ | P2 | |
| Namaste Pay (Nepal Telecom) | ⬜ | P2 | |
| CellPay | ⬜ | P2 | |
| MyPay | ⬜ | P3 | |
| QPay | ⬜ | P3 | |
| Hamro Pay | ⬜ | P3 | |

### Payment networks (no parser needed; recognise them in SMS text)

These move money between banks and appear in remarks and merchant fields rather than as SMS senders. Treat them as merchant or transfer keywords, not as accounts: ConnectIPS / NCHL (bank transfers), Fonepay (QR, run by F1Soft), NepalPay QR (national QR standard), NPS (Nepal Payment Solution) and SCT (Smart Choice Technologies) card/ATM switches, and Visa / Mastercard / UnionPay.

### Remittance (P2, credits only)

IME Ltd, Prabhu Money Transfer, City Express, Himal Remit; Western Union and MoneyGram through local agents. Most arrive as a credit SMS from the receiving bank, so bank parsers cover them first; a dedicated parser only helps for cash-pickup alerts.

### Other money flows (categorisation, not parsers)

Already handled by `CategoryMapping` keywords rather than parsers:
- **Insurance:** Nepal Life, National Life, LIC Nepal, MetLife, Sanima Life, Himalayan Life, Shikhar, Sagarmatha Lumbini, NECO.
- **Investment:** NEPSE, CDSC/MeroShare, bank capital arms (Nabil Invest, NIBL Ace, NMB Capital, Global IME Capital).
- **Utilities and telecom:** NEA, KUKL, NTC, Ncell, WorldLink, Vianet, DishHome.
- **Cooperatives:** thousands of saving & credit cooperatives, mostly without SMS alerts; out of scope unless samples arrive.

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
| **NMB Bank** | `NMBBankParser.kt` | ✅ | ✅ | ✅ | ✅ | ✅ | `canHandle()` currently over-matches `contains("NABIL")` — on TODO to fix; Nabil is matched first in `BankParserFactory`, so no current misattribution |
| **Everest Bank** | `EverestBankParser.kt` | ✅ | ✅ | ✅ | ✅ | ✅ | Accepts ATM short codes (`CWRD`) and 7–10 digit numeric senders |
| **Laxmi Sunrise Bank** | `LaxmiBankParser.kt` | ✅ | ✅ | ✅ | ✅ | ✅ | LAXMI sender family + DLT pattern |
| **Siddhartha Bank** | `SiddharthaBankParser.kt` | ✅ | ✅ | ✅ | ✅ | ✅ | Matches SBL/SIDDHARTHA senders |
| **Himalayan Bank** | `HimalayanBankParser.kt` | ✅ | ✅ | ✅ | ✅* | ✅* | No amount/account/reference overrides; falls back to `CompiledPatterns` shared patterns |

`*` handled by the shared base-class patterns in `BankParser.kt`
(`CompiledPatterns.Account` / `CompiledPatterns.Reference` / `CompiledPatterns.Balance`).

### Sender IDs (from `canHandle()`)

| Institution | Sender IDs |
|---|---|
| eSewa | `ESEWA_ALERT`, `ESEWA`, `*ESEWALIMITED*`, `XX-ESEWA-X` (DLT) |
| Khalti | `KHALTI_ALERT`, `KHALTI`, `*KHALTIDIGITAL*`, `XX-KHALTI-X` (DLT) |
| Nabil Bank | `NABIL_ALERT`, `NABIL`, `*NABILBANK*`, `XX-NABIL-X` (DLT) |
| NMB Bank | `NMB_ALERT`, `NMBBANK`, `*NMB*` (broad contains-match) |
| Everest Bank | `EVEREST`, `*EVERESTBANK*`, `*EBL*`, `UJJ SH`, `CWRD` (ATM), 7–10 digit numeric senders, `XX-EVEREST-X` |
| Laxmi Sunrise | `LAXMI_ALERT`, `*LAXMI*`, `*LAXMISUNRISE*`, `XX-LAXMI-X` |
| Siddhartha Bank | `SBL_ALERT`, `*SBL*`, `*SIDDHARTHA*` |
| Himalayan Bank | `HBL_ALERT`, `HBL`, `*HIMALAYANBANK*`, `XX-HBL-X` (DLT) |

### Universal capabilities (base class, all parsers)

- **OTP / verification-code filtering** — non-transaction SMS are never parsed
- **Card metadata** — card type, due date, minimum due, available limit (regex ready; Nepali card SMS formats on TODO)
- **Balance extraction** via `CompiledPatterns.Balance`
- **Currency** — NPR on every parsed transaction

## Known Issues (tracked in `TODO.md`)

1. `NMBBankParser.canHandle()` contains `normalizedSender.contains("NABIL")` — remove it so NMB can never claim Nabil Bank senders.
2. `EverestBankParser.canHandle()` matches any 7–10 digit numeric sender — overly broad for an SMS-parsing app.
3. Credit card transaction handling is not wired per-bank yet.

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

## Future Nepali Institutions (requests tracked via issue template)

- Nepal Bank Limited (NBL)
- Rastriya Banijya Bank (RBB)
- Nepal Investment Mega Bank (NIMB)
- NIC Asia Bank
- Global IME Bank
- Sanima Bank
- Machhapuchchhre Bank
- Kumari Bank
- Prabhu Bank

Request a bank → [Bank Support Request template](https://github.com/Prarambha369/Hisab-Sathi/issues/new?template=bank_support_request.md)

See `docs/CONTRIBUTING_BANK_PARSERS.md` and `docs/parser-test-standards.md` before adding a parser.

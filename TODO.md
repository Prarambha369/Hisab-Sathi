# TODO — Hisab Sathi (हिसाब साथी)

Nepal-first backlog. Upstream PennyWise leftovers (Indian banks, merchant logos) have been removed — do not re-add non-Nepali items here.

## Banks (core scope)
Full list with priorities: `docs/BANK_SUPPORT.md` → "Roadmap: every Nepali money institution". Every parser needs real, masked SMS samples first.

- [ ] P1 commercial banks: Global IME, NIC Asia, Nepal Investment Mega (NIMB), Prabhu, Nepal Bank Ltd, Rastriya Banijya
- [ ] P1 wallet: IME Pay
- [ ] P1 development bank: Muktinath Bikas
- [ ] P2 commercial banks: Kumari, Prime Commercial, Sanima, Machhapuchchhre, Citizens, ADBL, Nepal SBI, Standard Chartered Nepal
- [ ] P2 wallets: Prabhu Pay, Namaste Pay, CellPay
- [ ] Review `EverestBankParser.canHandle()` — it accepts bare numeric senders (`^\d{7,10}$`), which is overly broad
- [ ] Credit card transaction handling (base parser already reads card type / due date / min-due / available limit — wire real Nepali card SMS formats)
- [ ] Add chart for existing bank support and templates

## Parsing & Data Integrity
- [ ] Fix transaction hash collision issue (add more unique data)
- [ ] Cleanup deleted hashes older than 6 months
- [ ] Bulk delete feature
- [ ] Full rescan option in settings

## Smart SMS Scanning
- [ ] Test with week-old unscanned messages (workers do first scan ≈ 3 months, then incremental last-3-day scans)

## AI
- [ ] BYOK (bring-your-own-key) as an alternative to the on-device MediaPipe model

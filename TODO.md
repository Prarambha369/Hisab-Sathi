# TODO — Hisab Sathi (हिसाब साथी)

Nepal-first backlog. Upstream PennyWise leftovers (Indian banks, merchant logos) have been removed — do not re-add non-Nepali items here.

## Banks (core scope)
- [ ] Add more Nepali banks & institutions (see `docs/BANK_SUPPORT.md` future list):
  - [ ] Nepal Bank Limited (NBL)
  - [ ] Rastriya Banijya Bank (RBB)
  - [ ] Nepal Investment Mega Bank (NIMB)
  - [ ] NIC Asia Bank
  - [ ] Global IME Bank
  - [ ] Sanima Bank
  - [ ] Machhapuchchhre Bank
  - [ ] Prabhu Bank
  - [ ] Kumari Bank
- [x] Fix `NMBBankParser.canHandle()` matching `contains("NABIL")` — NMB must never claim Nabil senders (fixed and tested)
- [x] Review `EverestBankParser.canHandle()` — numeric gateway senders (`^\d{7,10}$`) are intentional and tested for Nepal's SMS routing
- [ ] Credit card transaction handling (base parser already reads card type / due date / min-due / available limit — wire real Nepali card SMS formats)
- [ ] Add chart for existing bank support and templates

## Parsing & Data Integrity
- [x] Fix transaction hash collision issue (add more unique data) — incorporated reference, account, balance, merchant, and type, with backward-compatible legacy hash lookup
- [ ] Cleanup deleted hashes older than 6 months
- [ ] Bulk delete feature
- [ ] Full rescan option in settings

## Smart SMS Scanning
- [ ] Test with week-old unscanned messages (workers do first scan ≈ 3 months, then incremental last-3-day scans)

## AI
- [ ] BYOK (bring-your-own-key) as an alternative to the on-device MediaPipe model

# Nepali SMS Bank AI - Hisab Sathi (हिसाब साथी)

## Overview
This project has been transformed into **Hisab Sathi** (हिसाब साथी), a Nepali-only SMS banking parser built on top of PennyWise.

**Hisab Sathi** means "Account Companion" in Nepali - the perfect name for a financial tracking assistant.

## Supported Nepali Banks & Wallets

### Digital Wallets
1. **eSewa** - Nepal's most popular digital wallet
2. **Khalti** - Popular digital wallet for payments

### Commercial Banks
1. **Nabil Bank** - First private bank of Nepal
2. **NMB Bank** - NMB Bank Limited  
3. **Everest Bank** - Everest Bank Limited
4. **Laxmi Sunrise Bank** - Laxmi Sunrise Bank Limited
5. **Siddhartha Bank** - Siddhartha Bank Limited
6. **Himalayan Bank** - Himalayan Bank Limited

## Features

- ✅ **NPR Currency Support** - All transactions use Nepalese Rupees
- ✅ **Nepali Bank SMS Parsing** - Optimized for Nepali bank SMS formats
- ✅ **Digital Wallet Integration** - eSewa and Khalti support
- ✅ **Transaction Type Detection** - Income vs Expense classification
- ✅ **Merchant Extraction** - Identifies merchants from SMS
- ✅ **Account Number Masking** - Extracts last 4 digits
- ✅ **Reference Number Tracking** - Captures transaction references

## Common SMS Sender IDs

| Institution | Sender ID |
|------------|-----------|
| eSewa | ESEWA_ALERT, ESEWA |
| Khalti | KHALTI_ALERT, KHALTI |
| Nabil Bank | NABIL_ALERT, NABIL |
| NMB Bank | NMB_ALERT, NMBBANK |
| Everest Bank | EVEREST |
| Laxmi Bank | LAXMI_ALERT |
| Siddhartha Bank | SBL_ALERT |
| Himalayan Bank | HBL_ALERT |

## Future Enhancements

Additional Nepali banks to be implemented:
- Nepal Bank Limited (NBL)
- Rastriya Banijya Bank (RBB)
- Nepal Investment Bank (NIBL)
- Machhapuchchhre Bank
- Sanima Bank
- Global IME Bank
- NIC Asia Bank
- Citizens Bank
- Kumari Bank
- Sunrise Bank
- Mega Bank
- Prabhu Bank

## Branding

**Name**: Hisab Sathi (हिसाब साथी)
**Tagline**: तपाईंको आर्थिक साथी - Your Financial Companion

## Technical Notes

- All non-Nepali bank parsers have been removed
- System exclusively supports Nepali financial institutions
- Built on PennyWise architecture
- Kotlin-based parser system

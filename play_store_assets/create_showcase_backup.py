# File: play_store_assets/create_showcase_backup.py
import zipfile
import json
import os
import time
from datetime import date, datetime, timezone

OUTPUT_DIR = r"C:\Users\sivas\AndroidStudioProjects\Folio\play_store_assets"
OUTPUT_ZIP = os.path.join(OUTPUT_DIR, "Folio_Showcase_USD_Demo.zip")

# Load base categories from the extracted backup
with zipfile.ZipFile(os.path.join(OUTPUT_DIR, "sample_existing.zip")) as z:
    with z.open("folio_backup_v1.json") as f:
        existing_data = json.load(f)

categories = existing_data.get("categories", [])

# User preferences
user_preferences = {
    "primaryCurrencyCode": "USD",
    "secondaryCurrencies": "[]",
    "numberGrouping": "INTERNATIONAL",
    "monthStartDay": 1,
    "weekStartDayIso": 1,
    "subcategoriesEnabled": True,
    "carryOverEnabled": False,
    "themeMode": "LIGHT",
    "dynamicColorEnabled": False,
    "accentPalette": "EMERALD",
    "biometricLockEnabled": False,
    "isSupporter": True,
    "supporterTier": "LIFETIME",
    "cardMilestonesJson": "[]",
    "iconPackTheme": "MATERIAL",
    "monthlySavingsTargetMinor": 150000,
    "savingsGoalsJson": "[]"
}

# Accounts
# 1: Chase Total Checking (Bank)
# 2: Vanguard High-Yield Savings (Savings)
# 3: Chase Sapphire Reserve (Credit Card)
# 4: Amex Gold Card (Credit Card)
# 5: Fidelity S&P 500 ETF (Investment)
# 6: Cash Wallet (Cash)
# 7: Auto Loan (Loan)
# 8: Chase Freedom Debit (Debit Card linked to 1)

accounts = [
    {
        "id": 1,
        "name": "Chase Total Checking",
        "groupType": "BANK_ACCOUNT",
        "currencyCode": "USD",
        "openingBalanceMinor": 842050,  # $8,420.50
        "initialUnbilledMinor": None,
        "creditLimitMinor": None,
        "settlementDayOfMonth": None,
        "paymentDueDayOfMonth": None,
        "parentAccountId": None,
        "includeInNetWorth": True,
        "isArchived": False,
        "colorHex": "#2563EB",
        "iconName": "account_balance",
        "sortOrder": 1,
        "isPinned": True,
        "createdAtEpochMillis": 1789000000000
    },
    {
        "id": 2,
        "name": "High-Yield Savings",
        "groupType": "SAVINGS",
        "currencyCode": "USD",
        "openingBalanceMinor": 2485000,  # $24,850.00
        "initialUnbilledMinor": None,
        "creditLimitMinor": None,
        "settlementDayOfMonth": None,
        "paymentDueDayOfMonth": None,
        "parentAccountId": None,
        "includeInNetWorth": True,
        "isArchived": False,
        "colorHex": "#059669",
        "iconName": "savings",
        "sortOrder": 2,
        "isPinned": True,
        "createdAtEpochMillis": 1789000000000
    },
    {
        "id": 3,
        "name": "Chase Sapphire Reserve",
        "groupType": "CREDIT_CARD",
        "currencyCode": "USD",
        "openingBalanceMinor": 0,
        "initialUnbilledMinor": 0,
        "creditLimitMinor": 1000000,  # $10,000.00
        "settlementDayOfMonth": 20,
        "paymentDueDayOfMonth": 10,
        "parentAccountId": None,
        "includeInNetWorth": True,
        "isArchived": False,
        "colorHex": "#1E3A8A",
        "iconName": "credit_card",
        "sortOrder": 3,
        "isPinned": True,
        "createdAtEpochMillis": 1789000000000
    },
    {
        "id": 4,
        "name": "Amex Gold Card",
        "groupType": "CREDIT_CARD",
        "currencyCode": "USD",
        "openingBalanceMinor": 0,
        "initialUnbilledMinor": 0,
        "creditLimitMinor": 1500000,  # $15,000.00
        "settlementDayOfMonth": 28,
        "paymentDueDayOfMonth": 18,
        "parentAccountId": None,
        "includeInNetWorth": True,
        "isArchived": False,
        "colorHex": "#D97706",
        "iconName": "credit_card",
        "sortOrder": 4,
        "isPinned": False,
        "createdAtEpochMillis": 1789000000000
    },
    {
        "id": 5,
        "name": "Fidelity S&P 500 ETF",
        "groupType": "INVESTMENT",
        "currencyCode": "USD",
        "openingBalanceMinor": 4520000,  # $45,200.00
        "initialUnbilledMinor": None,
        "creditLimitMinor": None,
        "settlementDayOfMonth": None,
        "paymentDueDayOfMonth": None,
        "parentAccountId": None,
        "includeInNetWorth": True,
        "isArchived": False,
        "colorHex": "#7C3AED",
        "iconName": "trending_up",
        "sortOrder": 5,
        "isPinned": False,
        "createdAtEpochMillis": 1789000000000
    },
    {
        "id": 6,
        "name": "Cash Wallet",
        "groupType": "CASH",
        "currencyCode": "USD",
        "openingBalanceMinor": 25000,  # $250.00
        "initialUnbilledMinor": None,
        "creditLimitMinor": None,
        "settlementDayOfMonth": None,
        "paymentDueDayOfMonth": None,
        "parentAccountId": None,
        "includeInNetWorth": True,
        "isArchived": False,
        "colorHex": "#10B981",
        "iconName": "wallet",
        "sortOrder": 6,
        "isPinned": False,
        "createdAtEpochMillis": 1789000000000
    },
    {
        "id": 7,
        "name": "Tesla Auto Loan",
        "groupType": "LOAN",
        "currencyCode": "USD",
        "openingBalanceMinor": -1250000,  # -$12,500.00
        "initialUnbilledMinor": None,
        "creditLimitMinor": None,
        "settlementDayOfMonth": None,
        "paymentDueDayOfMonth": None,
        "parentAccountId": None,
        "includeInNetWorth": True,
        "isArchived": False,
        "colorHex": "#DC2626",
        "iconName": "directions_car",
        "sortOrder": 7,
        "isPinned": False,
        "createdAtEpochMillis": 1789000000000
    },
    {
        "id": 8,
        "name": "Chase Sapphire Debit",
        "groupType": "DEBIT_CARD",
        "currencyCode": "USD",
        "openingBalanceMinor": 0,
        "initialUnbilledMinor": None,
        "creditLimitMinor": None,
        "settlementDayOfMonth": None,
        "paymentDueDayOfMonth": None,
        "parentAccountId": 1,  # Linked to Chase Checking
        "includeInNetWorth": False,
        "isArchived": False,
        "colorHex": "#0284C7",
        "iconName": "credit_card",
        "sortOrder": 8,
        "isPinned": False,
        "createdAtEpochMillis": 1789000000000
    }
]

# Tags
tags = [
    {"id": 1, "name": "Groceries", "colorHex": "#10B981"},
    {"id": 2, "name": "Work", "colorHex": "#3B82F6"},
    {"id": 3, "name": "Dining", "colorHex": "#F97316"},
    {"id": 4, "name": "Tech", "colorHex": "#8B5CF6"},
    {"id": 5, "name": "Utilities", "colorHex": "#06B6D4"}
]

# Budgets
budgets = [
    {
        "id": 1,
        "categoryId": None,  # Overall monthly budget
        "limitAmountMinor": 450000,  # $4,500.00
        "alertThresholdPercent": 80,
        "updatedAtEpochMillis": 1790000000000
    },
    {
        "id": 2,
        "categoryId": 1,  # Food & Dining
        "limitAmountMinor": 60000,  # $600.00
        "alertThresholdPercent": 80,
        "updatedAtEpochMillis": 1790000000000
    },
    {
        "id": 3,
        "categoryId": 2,  # Groceries
        "limitAmountMinor": 80000,  # $800.00
        "alertThresholdPercent": 80,
        "updatedAtEpochMillis": 1790000000000
    },
    {
        "id": 4,
        "categoryId": 5,  # Shopping
        "limitAmountMinor": 50000,  # $500.00
        "alertThresholdPercent": 80,
        "updatedAtEpochMillis": 1790000000000
    },
    {
        "id": 5,
        "categoryId": 3,  # Housing & Utilities
        "limitAmountMinor": 150000,  # $1,500.00
        "alertThresholdPercent": 80,
        "updatedAtEpochMillis": 1790000000000
    }
]

# Helpers for dates
def to_epoch_day(y, m, d):
    return (date(y, m, d) - date(1970, 1, 1)).days

def to_epoch_millis(y, m, d, hh=12, mm=0):
    dt = datetime(y, m, d, hh, mm, tzinfo=timezone.utc)
    return int(dt.timestamp() * 1000)

# Build Transactions
# November 2026 transactions (current month)
# Late October 2026 transactions (previous cycle for Chase CC statement balance payable!)
transactions = []
transaction_splits = []
transaction_tags = []

tx_id = 1
split_id = 1

def add_tx(type_str, y, m, d, amount_minor, account_id, to_acc=None, cat_id=None, payee="", note="", fee=0, is_split=False, tag_ids=None):
    global tx_id
    current_id = tx_id
    tx_id += 1
    t_obj = {
        "id": current_id,
        "type": type_str,
        "timestampEpochMillis": to_epoch_millis(y, m, d),
        "ledgerDateEpochDay": to_epoch_day(y, m, d),
        "amountMinor": amount_minor,
        "currencyCode": "USD",
        "accountId": account_id,
        "transferToAccountId": to_acc,
        "transferFeeMinor": fee,
        "categoryId": cat_id,
        "isSplit": is_split,
        "payeeOrDescription": payee,
        "note": note,
        "originalTransactionId": None,
        "installmentPlanId": None,
        "installmentIndex": None,
        "installmentTotalCount": None,
        "recurringRuleId": None
    }
    transactions.append(t_obj)
    if tag_ids:
        for tid in tag_ids:
            transaction_tags.append({"transactionId": current_id, "tagId": tid})
    return current_id

# --- October Transactions (Creates Statement Balance for Chase Sapphire which settled on Oct 20) ---
add_tx("EXPENSE", 2026, 10, 5, 24500, 3, cat_id=1, payee="Nobu Restaurant", note="Dinner with team", tag_ids=[3, 2])
add_tx("EXPENSE", 2026, 10, 10, 8920, 3, cat_id=2, payee="Trader Joe's", note="Weekly staples", tag_ids=[1])
add_tx("EXPENSE", 2026, 10, 14, 150000, 3, cat_id=5, payee="Delta Air Lines", note="Flight to conference", tag_ids=[2])
add_tx("EXPENSE", 2026, 10, 18, 58300, 3, cat_id=5, payee="Westin Hotel", note="Lodging", tag_ids=[2])
# Total October CC Spend = 245.00 + 89.20 + 1500.00 + 583.00 = $2,417.20 Statement Balance Payable!

# --- November Transactions (Current month up to Nov 6) ---
# Nov 1: Monthly Paycheck
add_tx("INCOME", 2026, 11, 1, 625000, 1, cat_id=201, payee="TechCorp Global Inc", note="Bi-weekly direct deposit salary", tag_ids=[2])
# Nov 1: Rent Payment
add_tx("EXPENSE", 2026, 11, 1, 120000, 1, cat_id=3, payee="Apartment Leasing Office", note="Monthly rent payment", tag_ids=[5])
# Nov 1: Vanguard Auto-Invest
add_tx("TRANSFER", 2026, 11, 1, 50000, 1, to_acc=5, payee="Vanguard Monthly DCA", note="Automatic investment into S&P 500")

# Nov 2: Whole Foods Market (Groceries)
add_tx("EXPENSE", 2026, 11, 2, 14280, 3, cat_id=2, payee="Whole Foods Market", note="Organic produce & groceries", tag_ids=[1])
# Nov 2: Blue Bottle Coffee
add_tx("EXPENSE", 2026, 11, 2, 750, 8, cat_id=1, payee="Blue Bottle Coffee", note="Morning cappuccino & pastry", tag_ids=[3])

# Nov 3: ConEdison Electric Bill
add_tx("EXPENSE", 2026, 11, 3, 11540, 1, cat_id=3, payee="ConEdison Electric", note="October residential power bill", tag_ids=[5])
# Nov 3: Uber Rides
add_tx("EXPENSE", 2026, 11, 3, 3420, 3, cat_id=4, payee="Uber Technologies", note="Ride to client headquarters", tag_ids=[2])

# Nov 4: Apple Store Split Transaction
apple_id = add_tx("EXPENSE", 2026, 11, 4, 32900, 3, cat_id=5, payee="Apple Fifth Avenue", note="Magic Keyboard & AirPods case", is_split=True, tag_ids=[4])
transaction_splits.append({
    "id": 1,
    "transactionId": apple_id,
    "categoryId": 5,  # Shopping
    "amountMinor": 24900,
    "note": "Magic Keyboard"
})
transaction_splits.append({
    "id": 2,
    "transactionId": apple_id,
    "categoryId": 142,  # Electronics & Gadgets
    "amountMinor": 8000,
    "note": "AirPods Protective Case"
})

# Nov 4: Freelance Client Milestone
add_tx("INCOME", 2026, 11, 4, 140000, 1, cat_id=202, payee="Apex Studio LLC", note="Design system consultation milestone", tag_ids=[2])

# Nov 5: Chipotle Mexican Grill
add_tx("EXPENSE", 2026, 11, 5, 1845, 4, cat_id=1, payee="Chipotle Mexican Grill", note="Burrito bowl & sparkling water", tag_ids=[3])
# Nov 5: Target Essentials
add_tx("EXPENSE", 2026, 11, 5, 6420, 4, cat_id=5, payee="Target Superstore", note="Home organizing supplies", tag_ids=[1])

# Nov 6 (Today): Equinox Fitness Monthly
add_tx("EXPENSE", 2026, 11, 6, 12000, 3, cat_id=6, payee="Equinox Sports Club", note="All-access club membership")
# Nov 6 (Today): Starbucks
add_tx("EXPENSE", 2026, 11, 6, 625, 8, cat_id=1, payee="Starbucks Reserve", note="Cold brew & almond croissant", tag_ids=[3])

# Bookmarks
bookmarks = [
    {
        "id": 1,
        "title": "Whole Foods Grocery",
        "type": "EXPENSE",
        "amountMinor": 12000,
        "accountId": 3,
        "transferToAccountId": None,
        "categoryId": 2,
        "payeeOrDescription": "Whole Foods Market",
        "note": "Weekly produce & dairy",
        "tagsCsv": "Groceries",
        "sortOrder": 1
    },
    {
        "id": 2,
        "title": "Morning Coffee",
        "type": "EXPENSE",
        "amountMinor": 650,
        "accountId": 8,
        "transferToAccountId": None,
        "categoryId": 1,
        "payeeOrDescription": "Local Roastery",
        "note": "Oat flat white",
        "tagsCsv": "Dining",
        "sortOrder": 2
    }
]

# Root backup dictionary
backup_payload = {
    "backupFormatVersion": 1,
    "createdAtEpochMillis": int(time.time() * 1000),
    "userPreferences": user_preferences,
    "accounts": accounts,
    "categories": categories,
    "tags": tags,
    "bookmarks": bookmarks,
    "installmentPlans": [],
    "recurringRules": [],
    "transactions": transactions,
    "transactionSplits": transaction_splits,
    "transactionTags": transaction_tags,
    "budgets": budgets
}

# Write to zip
with zipfile.ZipFile(OUTPUT_ZIP, "w", compression=zipfile.ZIP_DEFLATED) as zout:
    zout.writestr("folio_backup_v1.json", json.dumps(backup_payload, indent=2))

print(f"Created showcase backup: {OUTPUT_ZIP} ({os.path.getsize(OUTPUT_ZIP)} bytes)")

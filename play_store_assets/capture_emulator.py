# File: play_store_assets/capture_emulator.py
import subprocess
import os
import sys

ADB_PATH = os.path.expandvars(r"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe")
DEVICE_ID = "emulator-5554"
OUTPUT_DIR = os.path.abspath(r"C:\Users\sivas\AndroidStudioProjects\Folio\play_store_assets")

SLOT_MAP = {
    "1": ("screenshot_1_daily.png", "Daily Cashflow Ledger"),
    "2": ("screenshot_2_calendar.png", "Calendar View"),
    "3": ("screenshot_3_cards.png", "Credit Cards Hub"),
    "4": ("screenshot_4_budgets.png", "Budgets & Analytics"),
    "5": ("screenshot_5_accounts.png", "Net Worth & Accounts"),
    "6": ("screenshot_6_calculator_split.png", "Calculator & Split Bills"),
    "7": ("screenshot_7_payees_tags.png", "Payee & Tag Intelligence"),
    "8": ("screenshot_8_privacy_dark.png", "Biometric Lock & Privacy")
}

def capture_slot(slot_key):
    if slot_key not in SLOT_MAP:
        print(f"Unknown slot: {slot_key}. Choose 1 through 8.")
        return False
    
    file_name, desc = SLOT_MAP[slot_key]
    out_path = os.path.join(OUTPUT_DIR, file_name)
    
    with open(out_path, "wb") as f:
        res = subprocess.run([ADB_PATH, "-s", DEVICE_ID, "exec-out", "screencap", "-p"], stdout=f)
    
    if res.returncode == 0 and os.path.exists(out_path):
        size_kb = os.path.getsize(out_path) // 1024
        print(f"[OK] Captured Slot {slot_key} ({desc}) -> {file_name} ({size_kb} KB)")
        return True
    else:
        print(f"[ERROR] Failed to capture slot {slot_key}")
        return False

def main():
    if len(sys.argv) < 2:
        print("Usage: python capture_emulator.py <slot_number_1_to_5>")
        print("Example: python capture_emulator.py 1")
        sys.exit(1)
        
    slot = sys.argv[1].strip()
    capture_slot(slot)

if __name__ == "__main__":
    main()

# File: play_store_assets/generate_screens.py
import subprocess
import os
import sys

CHROME_PATH = r"C:\Program Files\Google\Chrome\Application\chrome.exe"
OUTPUT_DIR = r"C:\Users\sivas\AndroidStudioProjects\Folio\play_store_assets"

# Common SVG Icons
SVG_STATUS_BAR = """
<div style="height: 70px; display: flex; justify-content: space-between; align-items: center; padding: 0 40px; font-size: 32px; font-weight: 600; color: #0F172A;">
    <span>9:41</span>
    <div style="display: flex; align-items: center; gap: 16px;">
        <svg width="34" height="26" viewBox="0 0 24 24" fill="#0F172A"><path d="M12 3c-4.97 0-9 4.03-9 9 0 2.12.74 4.07 1.97 5.61L12 22l7.03-4.39C20.26 16.07 21 14.12 21 12c0-4.97-4.03-9-9-9zm0 14c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5z"/></svg>
        <svg width="32" height="26" viewBox="0 0 24 24" fill="#0F172A"><path d="M12 4C7.31 4 3.07 5.9 0 8.98L12 21 24 8.98C20.93 5.9 16.69 4 12 4zm0 3c3.48 0 6.64 1.35 9 3.55L12 18.5 3 10.55C5.36 8.35 8.52 7 12 7z"/></svg>
        <svg width="40" height="26" viewBox="0 0 24 24" fill="#0F172A"><path d="M17 5v14h2V5h-2zm-4 4v10h2V9h-2zm-4 4v6h2v-6H9zm-4 3v3h2v-3H5z"/></svg>
        <div style="display: flex; align-items: center; gap: 4px; border: 3px solid #0F172A; border-radius: 8px; padding: 2px 4px; height: 26px; width: 44px; box-sizing: border-box;">
            <div style="background: #10B981; height: 14px; width: 28px; border-radius: 3px;"></div>
        </div>
    </div>
</div>
"""

def make_nav_bar(active_index):
    tabs = [
        ("Trans.", """<svg width="38" height="38" viewBox="0 0 24 24" fill="currentColor"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/></svg>"""),
        ("Stats", """<svg width="38" height="38" viewBox="0 0 24 24" fill="currentColor"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/></svg>"""),
        ("Accounts", """<svg width="38" height="38" viewBox="0 0 24 24" fill="currentColor"><path d="M21 18v1c0 1.1-.9 2-2 2H5c-1.11 0-2-.9-2-2V5c0-1.1.89-2 2-2h14c1.1 0 2 .9 2 2v1h-9c-1.11 0-2 .9-2 2v8c0 1.1.89 2 2 2h9zm-9-2h10V8H12v8zm4-2.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z"/></svg>"""),
        ("Settings", """<svg width="38" height="38" viewBox="0 0 24 24" fill="currentColor"><path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>""")
    ]
    
    html = """
    <div style="position: absolute; bottom: 0; left: 0; right: 0; height: 190px; background: #FFFFFF; border-top: 1px solid #E2E8F0; display: flex; flex-direction: column; justify-content: space-between; align-items: center; padding-top: 18px; box-sizing: border-box; z-index: 100;">
        <div style="display: flex; width: 100%; justify-content: space-around;">
    """
    for i, (name, icon) in enumerate(tabs):
        is_active = (i == active_index)
        color = "#10B981" if is_active else "#64748B"
        pill_bg = "#ECFDF5" if is_active else "transparent"
        weight = "700" if is_active else "500"
        html += f"""
        <div style="display: flex; flex-direction: column; align-items: center; gap: 8px; width: 180px;">
            <div style="background: {pill_bg}; color: {color}; width: 110px; height: 60px; border-radius: 30px; display: flex; align-items: center; justify-content: center;">
                {icon}
            </div>
            <span style="font-size: 26px; font-weight: {weight}; color: {color};">{name}</span>
        </div>
        """
    html += """
        </div>
        <div style="width: 320px; height: 8px; background: #0F172A; border-radius: 4px; margin-bottom: 16px;"></div>
    </div>
    """
    return html

# ----------------- SCREEN 1: DAILY VIEW -----------------
def generate_screen1():
    return f"""<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<style>
* {{ box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Google Sans", sans-serif; }}
body {{ width: 1080px; height: 2400px; background: #F8FAFC; color: #0F172A; position: relative; overflow: hidden; }}
.card {{ background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 32px; box-shadow: 0 4px 16px rgba(15, 23, 42, 0.04); }}
</style>
</head>
<body>
{SVG_STATUS_BAR}

<!-- Top Header & Search -->
<div style="padding: 10px 40px 20px;">
    <div style="display: flex; justify-content: space-between; align-items: center; height: 80px;">
        <div style="display: flex; align-items: center; gap: 16px;">
            <div style="width: 56px; height: 56px; border-radius: 18px; background: #10B981; display: flex; align-items: center; justify-content: center; color: white;">
                <svg width="34" height="34" viewBox="0 0 24 24" fill="currentColor"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/></svg>
            </div>
            <div>
                <h1 style="font-size: 40px; font-weight: 800; letter-spacing: -0.5px;">Folio</h1>
                <p style="font-size: 22px; color: #64748B; font-weight: 500;">Offline Privacy Ledger</p>
            </div>
        </div>
        <div style="display: flex; gap: 16px;">
            <div style="width: 60px; height: 60px; border-radius: 50%; background: #FFFFFF; border: 1px solid #E2E8F0; display: flex; align-items: center; justify-content: center; color: #0F172A;">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
            </div>
            <div style="width: 60px; height: 60px; border-radius: 50%; background: #FFFFFF; border: 1px solid #E2E8F0; display: flex; align-items: center; justify-content: center; color: #0F172A;">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="currentColor"><path d="M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z"/></svg>
            </div>
        </div>
    </div>
</div>

<!-- Subtabs Bar -->
<div style="padding: 0 40px 24px; display: flex; gap: 14px;">
    <div style="background: #10B981; color: white; padding: 14px 34px; border-radius: 24px; font-size: 24px; font-weight: 700; box-shadow: 0 4px 12px rgba(16, 185, 129, 0.25);">Daily</div>
    <div style="background: #FFFFFF; color: #64748B; border: 1px solid #E2E8F0; padding: 14px 30px; border-radius: 24px; font-size: 24px; font-weight: 600;">Calendar</div>
    <div style="background: #FFFFFF; color: #64748B; border: 1px solid #E2E8F0; padding: 14px 30px; border-radius: 24px; font-size: 24px; font-weight: 600;">Weekly</div>
    <div style="background: #FFFFFF; color: #64748B; border: 1px solid #E2E8F0; padding: 14px 30px; border-radius: 24px; font-size: 24px; font-weight: 600;">Monthly</div>
    <div style="background: #FFFFFF; color: #64748B; border: 1px solid #E2E8F0; padding: 14px 30px; border-radius: 24px; font-size: 24px; font-weight: 600;">Summary</div>
</div>

<!-- Month Header Card -->
<div style="padding: 0 40px 28px;">
    <div class="card" style="padding: 28px 36px; background: linear-gradient(135deg, #064E3B 0%, #065F46 100%); color: white; border: none; box-shadow: 0 12px 30px rgba(6, 78, 59, 0.25);">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
            <div style="display: flex; align-items: center; gap: 14px;">
                <span style="font-size: 30px; font-weight: 800; letter-spacing: -0.5px;">November 2026</span>
                <span style="background: rgba(255,255,255,0.2); padding: 4px 16px; border-radius: 12px; font-size: 20px; font-weight: 600;">30 Days</span>
            </div>
            <div style="background: rgba(16, 185, 129, 0.4); border: 1px solid rgba(255,255,255,0.25); padding: 6px 20px; border-radius: 16px; font-size: 20px; font-weight: 700; color: #A7F3D0;">
                ★ 66.1% Saved
            </div>
        </div>
        <div style="display: flex; justify-content: space-between;">
            <div>
                <p style="font-size: 20px; color: #A7F3D0; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">Total Income</p>
                <p style="font-size: 38px; font-weight: 800; margin-top: 6px; font-variant-numeric: tabular-nums;">+$4,850.00</p>
            </div>
            <div style="width: 1px; background: rgba(255,255,255,0.15);"></div>
            <div>
                <p style="font-size: 20px; color: #FECDD3; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">Total Expense</p>
                <p style="font-size: 38px; font-weight: 800; margin-top: 6px; color: #FDA4AF; font-variant-numeric: tabular-nums;">-$1,642.50</p>
            </div>
            <div style="width: 1px; background: rgba(255,255,255,0.15);"></div>
            <div>
                <p style="font-size: 20px; color: #BAE6FD; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">Net Cashflow</p>
                <p style="font-size: 38px; font-weight: 800; margin-top: 6px; color: #E0F2FE; font-variant-numeric: tabular-nums;">+$3,207.50</p>
            </div>
        </div>
    </div>
</div>

<!-- Transactions Stream -->
<div style="padding: 0 40px; display: flex; flex-direction: column; gap: 24px;">

    <!-- Today Group -->
    <div>
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; padding: 0 8px;">
            <span style="font-size: 24px; font-weight: 700; color: #334155;">Today • Friday, Nov 6</span>
            <span style="font-size: 24px; font-weight: 800; color: #EF4444; font-variant-numeric: tabular-nums;">-$142.30</span>
        </div>
        <div class="card" style="padding: 6px 24px; display: flex; flex-direction: column;">
            <!-- Row 1 -->
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 22px 0; border-bottom: 1px solid #F1F5F9;">
                <div style="display: flex; align-items: center; gap: 20px;">
                    <div style="width: 68px; height: 68px; border-radius: 20px; background: #DBEAFE; color: #2563EB; display: flex; align-items: center; justify-content: center; font-size: 32px;">📱</div>
                    <div>
                        <p style="font-size: 28px; font-weight: 700; color: #0F172A;">Apple Store</p>
                        <p style="font-size: 22px; color: #64748B; margin-top: 4px;">Electronics • <span style="color: #6366F1; font-weight: 600;">Chase Sapphire (Credit)</span></p>
                    </div>
                </div>
                <div style="text-align: right;">
                    <p style="font-size: 30px; font-weight: 800; color: #EF4444; font-variant-numeric: tabular-nums;">-$89.00</p>
                    <span style="font-size: 18px; color: #64748B;">4:15 PM</span>
                </div>
            </div>
            <!-- Row 2 -->
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 22px 0; border-bottom: 1px solid #F1F5F9;">
                <div style="display: flex; align-items: center; gap: 20px;">
                    <div style="width: 68px; height: 68px; border-radius: 20px; background: #FEF3C7; color: #D97706; display: flex; align-items: center; justify-content: center; font-size: 32px;">☕</div>
                    <div>
                        <p style="font-size: 28px; font-weight: 700; color: #0F172A;">Blue Bottle Coffee</p>
                        <p style="font-size: 22px; color: #64748B; margin-top: 4px;">Food & Dining • <span style="color: #10B981; font-weight: 600;">Apple Pay (Debit)</span></p>
                    </div>
                </div>
                <div style="text-align: right;">
                    <p style="font-size: 30px; font-weight: 800; color: #EF4444; font-variant-numeric: tabular-nums;">-$6.50</p>
                    <span style="font-size: 18px; color: #64748B;">10:30 AM</span>
                </div>
            </div>
            <!-- Row 3 -->
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 22px 0;">
                <div style="display: flex; align-items: center; gap: 20px;">
                    <div style="width: 68px; height: 68px; border-radius: 20px; background: #DCFCE7; color: #16A34A; display: flex; align-items: center; justify-content: center; font-size: 32px;">🥦</div>
                    <div>
                        <p style="font-size: 28px; font-weight: 700; color: #0F172A;">Whole Foods Market</p>
                        <p style="font-size: 22px; color: #64748B; margin-top: 4px;">Groceries • <span style="color: #0284C7; font-weight: 600;">Chase Checking</span></p>
                    </div>
                </div>
                <div style="text-align: right;">
                    <p style="font-size: 30px; font-weight: 800; color: #EF4444; font-variant-numeric: tabular-nums;">-$46.80</p>
                    <span style="font-size: 18px; color: #64748B;">9:12 AM</span>
                </div>
            </div>
        </div>
    </div>

    <!-- Yesterday Group -->
    <div>
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; padding: 0 8px;">
            <span style="font-size: 24px; font-weight: 700; color: #334155;">Yesterday • Thursday, Nov 5</span>
            <span style="font-size: 24px; font-weight: 800; color: #10B981; font-variant-numeric: tabular-nums;">+$3,450.00</span>
        </div>
        <div class="card" style="padding: 6px 24px; display: flex; flex-direction: column;">
            <!-- Row 1 -->
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 22px 0; border-bottom: 1px solid #F1F5F9;">
                <div style="display: flex; align-items: center; gap: 20px;">
                    <div style="width: 68px; height: 68px; border-radius: 20px; background: #ECFDF5; color: #059669; display: flex; align-items: center; justify-content: center; font-size: 32px;">💰</div>
                    <div>
                        <p style="font-size: 28px; font-weight: 700; color: #0F172A;">Tech Corp Semi-Monthly Payroll</p>
                        <p style="font-size: 22px; color: #64748B; margin-top: 4px;">Salary • <span style="color: #0284C7; font-weight: 600;">Chase Checking</span></p>
                    </div>
                </div>
                <div style="text-align: right;">
                    <p style="font-size: 30px; font-weight: 800; color: #10B981; font-variant-numeric: tabular-nums;">+$3,500.00</p>
                    <span style="font-size: 18px; color: #10B981; font-weight: 600;">Cleared</span>
                </div>
            </div>
            <!-- Row 2 -->
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 22px 0; border-bottom: 1px solid #F1F5F9;">
                <div style="display: flex; align-items: center; gap: 20px;">
                    <div style="width: 68px; height: 68px; border-radius: 20px; background: #F3E8FF; color: #7C3AED; display: flex; align-items: center; justify-content: center; font-size: 32px;">🎬</div>
                    <div>
                        <p style="font-size: 28px; font-weight: 700; color: #0F172A;">Netflix Subscription</p>
                        <p style="font-size: 22px; color: #64748B; margin-top: 4px;">Entertainment • <span style="color: #6366F1; font-weight: 600;">Chase Sapphire</span></p>
                    </div>
                </div>
                <div style="text-align: right;">
                    <p style="font-size: 30px; font-weight: 800; color: #EF4444; font-variant-numeric: tabular-nums;">-$19.99</p>
                    <span style="font-size: 18px; color: #64748B;">Recurring</span>
                </div>
            </div>
            <!-- Row 3 -->
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 22px 0;">
                <div style="display: flex; align-items: center; gap: 20px;">
                    <div style="width: 68px; height: 68px; border-radius: 20px; background: #FEE2E2; color: #DC2626; display: flex; align-items: center; justify-content: center; font-size: 32px;">⛽</div>
                    <div>
                        <p style="font-size: 28px; font-weight: 700; color: #0F172A;">Chevron Gas Station</p>
                        <p style="font-size: 22px; color: #64748B; margin-top: 4px;">Transportation • <span style="color: #D97706; font-weight: 600;">Amex Gold</span></p>
                    </div>
                </div>
                <div style="text-align: right;">
                    <p style="font-size: 30px; font-weight: 800; color: #EF4444; font-variant-numeric: tabular-nums;">-$30.01</p>
                    <span style="font-size: 18px; color: #64748B;">8:40 AM</span>
                </div>
            </div>
        </div>
    </div>

</div>

<!-- Floating Action Button -->
<div style="position: absolute; right: 50px; bottom: 220px; width: 100px; height: 100px; border-radius: 32px; background: #10B981; color: white; display: flex; align-items: center; justify-content: center; box-shadow: 0 16px 36px rgba(16, 185, 129, 0.4); z-index: 10;">
    <svg width="48" height="48" viewBox="0 0 24 24" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
</div>

{make_nav_bar(0)}
</body>
</html>
"""

# ----------------- SCREEN 2: CALENDAR VIEW -----------------
def generate_screen2():
    cal_days = ""
    # Days 1 to 30 for Nov 2026 (Starts on Sunday)
    for d in range(1, 31):
        is_today = (d == 6)
        has_exp = d in [1, 2, 4, 5, 6, 8, 9, 12, 14, 18, 20, 22, 25, 27]
        has_inc = d in [1, 5, 15]
        
        bg = "#10B981" if is_today else "#FFFFFF"
        text_color = "#FFFFFF" if is_today else "#0F172A"
        border = "none" if is_today else "1px solid #F1F5F9"
        shadow = "0 8px 20px rgba(16, 185, 129, 0.35)" if is_today else "none"
        
        dots = ""
        if is_today:
            dots = '<div style="width: 8px; height: 8px; border-radius: 50%; background: #FFFFFF; margin-top: 6px;"></div>'
        else:
            dot_items = []
            if has_inc: dot_items.append('<div style="width: 8px; height: 8px; border-radius: 50%; background: #10B981;"></div>')
            if has_exp: dot_items.append('<div style="width: 8px; height: 8px; border-radius: 50%; background: #EF4444;"></div>')
            dots = f'<div style="display: flex; gap: 4px; margin-top: 6px;">{"".join(dot_items)}</div>'
            
        cal_days += f"""
        <div style="background: {bg}; border: {border}; border-radius: 20px; height: 104px; display: flex; flex-direction: column; align-items: center; justify-content: center; box-shadow: {shadow};">
            <span style="font-size: 26px; font-weight: {'800' if is_today else '600'}; color: {text_color};">{d}</span>
            {dots}
        </div>
        """

    return f"""<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<style>
* {{ box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Google Sans", sans-serif; }}
body {{ width: 1080px; height: 2400px; background: #F8FAFC; color: #0F172A; position: relative; overflow: hidden; }}
.card {{ background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 32px; box-shadow: 0 4px 16px rgba(15, 23, 42, 0.04); }}
</style>
</head>
<body>
{SVG_STATUS_BAR}

<!-- Top Header & Search -->
<div style="padding: 10px 40px 20px;">
    <div style="display: flex; justify-content: space-between; align-items: center; height: 80px;">
        <div style="display: flex; align-items: center; gap: 16px;">
            <div style="width: 56px; height: 56px; border-radius: 18px; background: #10B981; display: flex; align-items: center; justify-content: center; color: white;">
                <svg width="34" height="34" viewBox="0 0 24 24" fill="currentColor"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/></svg>
            </div>
            <div>
                <h1 style="font-size: 40px; font-weight: 800; letter-spacing: -0.5px;">Folio</h1>
                <p style="font-size: 22px; color: #64748B; font-weight: 500;">Interactive Calendar</p>
            </div>
        </div>
        <div style="display: flex; gap: 16px;">
            <div style="width: 60px; height: 60px; border-radius: 50%; background: #FFFFFF; border: 1px solid #E2E8F0; display: flex; align-items: center; justify-content: center; color: #0F172A;">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
            </div>
        </div>
    </div>
</div>

<!-- Subtabs Bar -->
<div style="padding: 0 40px 24px; display: flex; gap: 14px;">
    <div style="background: #FFFFFF; color: #64748B; border: 1px solid #E2E8F0; padding: 14px 30px; border-radius: 24px; font-size: 24px; font-weight: 600;">Daily</div>
    <div style="background: #10B981; color: white; padding: 14px 34px; border-radius: 24px; font-size: 24px; font-weight: 700; box-shadow: 0 4px 12px rgba(16, 185, 129, 0.25);">Calendar</div>
    <div style="background: #FFFFFF; color: #64748B; border: 1px solid #E2E8F0; padding: 14px 30px; border-radius: 24px; font-size: 24px; font-weight: 600;">Weekly</div>
    <div style="background: #FFFFFF; color: #64748B; border: 1px solid #E2E8F0; padding: 14px 30px; border-radius: 24px; font-size: 24px; font-weight: 600;">Monthly</div>
    <div style="background: #FFFFFF; color: #64748B; border: 1px solid #E2E8F0; padding: 14px 30px; border-radius: 24px; font-size: 24px; font-weight: 600;">Summary</div>
</div>

<!-- Month Header Navigator -->
<div style="padding: 0 40px 20px;">
    <div class="card" style="padding: 24px 32px; display: flex; justify-content: space-between; align-items: center;">
        <div style="width: 50px; height: 50px; border-radius: 50%; background: #F1F5F9; display: flex; align-items: center; justify-content: center; color: #64748B;">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/></svg>
        </div>
        <div style="text-align: center;">
            <h2 style="font-size: 32px; font-weight: 800;">November 2026</h2>
            <p style="font-size: 20px; color: #10B981; font-weight: 700; margin-top: 4px;">Net Cashflow: +$3,207.50</p>
        </div>
        <div style="width: 50px; height: 50px; border-radius: 50%; background: #F1F5F9; display: flex; align-items: center; justify-content: center; color: #64748B;">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/></svg>
        </div>
    </div>
</div>

<!-- Calendar Grid Card -->
<div style="padding: 0 40px 24px;">
    <div class="card" style="padding: 24px 28px;">
        <!-- Weekday Headers -->
        <div style="display: grid; grid-template-columns: repeat(7, 1fr); text-align: center; font-size: 20px; font-weight: 700; color: #94A3B8; margin-bottom: 16px;">
            <span>SUN</span><span>MON</span><span>TUE</span><span>WED</span><span>THU</span><span>FRI</span><span>SAT</span>
        </div>
        <!-- Days Grid -->
        <div style="display: grid; grid-template-columns: repeat(7, 1fr); gap: 10px;">
            {cal_days}
        </div>
    </div>
</div>

<!-- Selected Day Transactions Sheet -->
<div style="padding: 0 40px;">
    <div class="card" style="padding: 24px 30px; background: #FFFFFF; border: 2px solid #10B981;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px;">
            <div>
                <span style="background: #ECFDF5; color: #059669; font-size: 18px; font-weight: 700; padding: 4px 14px; border-radius: 10px;">SELECTED DATE</span>
                <h3 style="font-size: 28px; font-weight: 800; margin-top: 6px;">Friday, November 6</h3>
            </div>
            <div style="text-align: right;">
                <span style="font-size: 20px; color: #64748B; font-weight: 600;">3 Transactions</span>
                <p style="font-size: 28px; font-weight: 800; color: #EF4444; font-variant-numeric: tabular-nums;">-$142.30</p>
            </div>
        </div>
        <!-- Day Items -->
        <div style="display: flex; flex-direction: column; gap: 14px;">
            <div style="display: flex; justify-content: space-between; align-items: center; background: #F8FAFC; padding: 14px 20px; border-radius: 18px;">
                <div style="display: flex; align-items: center; gap: 16px;">
                    <span style="font-size: 26px;">📱</span>
                    <div>
                        <p style="font-size: 24px; font-weight: 700;">Apple Store</p>
                        <p style="font-size: 18px; color: #64748B;">Electronics • Chase Sapphire</p>
                    </div>
                </div>
                <span style="font-size: 24px; font-weight: 800; color: #EF4444;">-$89.00</span>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; background: #F8FAFC; padding: 14px 20px; border-radius: 18px;">
                <div style="display: flex; align-items: center; gap: 16px;">
                    <span style="font-size: 26px;">🥦</span>
                    <div>
                        <p style="font-size: 24px; font-weight: 700;">Whole Foods Market</p>
                        <p style="font-size: 18px; color: #64748B;">Groceries • Chase Checking</p>
                    </div>
                </div>
                <span style="font-size: 24px; font-weight: 800; color: #EF4444;">-$46.80</span>
            </div>
        </div>
    </div>
</div>

{make_nav_bar(0)}
</body>
</html>
"""

# ----------------- SCREEN 3: CREDIT CARDS COCKPIT -----------------
def generate_screen3():
    return f"""<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<style>
* {{ box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Google Sans", sans-serif; }}
body {{ width: 1080px; height: 2400px; background: #F8FAFC; color: #0F172A; position: relative; overflow: hidden; }}
.card {{ background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 32px; box-shadow: 0 4px 16px rgba(15, 23, 42, 0.04); }}
</style>
</head>
<body>
{SVG_STATUS_BAR}

<!-- Top Header -->
<div style="padding: 10px 40px 20px;">
    <div style="display: flex; justify-content: space-between; align-items: center; height: 80px;">
        <div style="display: flex; align-items: center; gap: 16px;">
            <div style="width: 56px; height: 56px; border-radius: 18px; background: #6366F1; display: flex; align-items: center; justify-content: center; color: white;">
                <svg width="34" height="34" viewBox="0 0 24 24" fill="currentColor"><path d="M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z"/></svg>
            </div>
            <div>
                <h1 style="font-size: 40px; font-weight: 800; letter-spacing: -0.5px;">Stats & Intelligence</h1>
                <p style="font-size: 22px; color: #64748B; font-weight: 500;">Credit Cards Cockpit</p>
            </div>
        </div>
        <div style="background: #F1F5F9; padding: 10px 24px; border-radius: 20px; font-size: 22px; font-weight: 700; color: #334155;">
            Nov 2026 ▾
        </div>
    </div>
</div>

<!-- Subtabs Bar -->
<div style="padding: 0 40px 24px; display: flex; gap: 14px;">
    <div style="background: #FFFFFF; color: #64748B; border: 1px solid #E2E8F0; padding: 14px 30px; border-radius: 24px; font-size: 24px; font-weight: 600;">Overview</div>
    <div style="background: #6366F1; color: white; padding: 14px 34px; border-radius: 24px; font-size: 24px; font-weight: 700; box-shadow: 0 4px 12px rgba(99, 102, 241, 0.25);">Cards</div>
    <div style="background: #FFFFFF; color: #64748B; border: 1px solid #E2E8F0; padding: 14px 30px; border-radius: 24px; font-size: 24px; font-weight: 600;">Budgets</div>
    <div style="background: #FFFFFF; color: #64748B; border: 1px solid #E2E8F0; padding: 14px 30px; border-radius: 24px; font-size: 24px; font-weight: 600;">Merchants & Tags</div>
</div>

<!-- Main Arc Utilization Cockpit Card -->
<div style="padding: 0 40px 28px;">
    <div class="card" style="padding: 36px; text-align: center; position: relative;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <span style="font-size: 24px; font-weight: 800; color: #1E293B;">Bureau Credit Utilization</span>
            <span style="background: #ECFDF5; color: #059669; font-size: 20px; font-weight: 700; padding: 6px 18px; border-radius: 14px; border: 1px solid #A7F3D0;">
                ✓ Safe Tier (&lt; 30%)
            </span>
        </div>

        <!-- Simulated Arc Gauge SVG -->
        <div style="position: relative; width: 340px; height: 200px; margin: 0 auto;">
            <svg width="340" height="200" viewBox="0 0 340 200">
                <!-- Background Arc -->
                <path d="M 30 180 A 140 140 0 0 1 310 180" fill="none" stroke="#E2E8F0" stroke-width="28" stroke-linecap="round"/>
                <!-- 30% Warning Tick -->
                <path d="M 114 47 A 140 140 0 0 1 170 40" fill="none" stroke="#FDE68A" stroke-width="28"/>
                <!-- Active Value Arc (18.4%) -->
                <path d="M 30 180 A 140 140 0 0 1 114 47" fill="none" stroke="#10B981" stroke-width="28" stroke-linecap="round"/>
            </svg>
            <div style="position: absolute; bottom: 10px; left: 0; right: 0;">
                <p style="font-size: 64px; font-weight: 900; color: #0F172A; letter-spacing: -1px;">18.4%</p>
                <p style="font-size: 22px; color: #64748B; font-weight: 600;">Across All 2 Cards</p>
            </div>
        </div>

        <div style="margin-top: 24px; padding-top: 24px; border-top: 1px solid #F1F5F9; display: flex; justify-content: space-around;">
            <div>
                <p style="font-size: 20px; color: #64748B; font-weight: 600;">Total Outstanding</p>
                <p style="font-size: 32px; font-weight: 800; color: #EF4444; margin-top: 4px;">$1,842.00</p>
            </div>
            <div style="width: 1px; background: #E2E8F0;"></div>
            <div>
                <p style="font-size: 20px; color: #64748B; font-weight: 600;">Smart Headroom (30%)</p>
                <p style="font-size: 32px; font-weight: 800; color: #10B981; margin-top: 4px;">+$1,158.00</p>
            </div>
            <div style="width: 1px; background: #E2E8F0;"></div>
            <div>
                <p style="font-size: 20px; color: #64748B; font-weight: 600;">Total Limit</p>
                <p style="font-size: 32px; font-weight: 800; color: #0F172A; margin-top: 4px;">$10,000.00</p>
            </div>
        </div>
    </div>
</div>

<!-- Smart Swipe Advisor Hero Card -->
<div style="padding: 0 40px 24px;">
    <div class="card" style="padding: 30px; background: linear-gradient(135deg, #1E1B4B 0%, #312E81 100%); color: white; border: none; box-shadow: 0 12px 30px rgba(49, 46, 129, 0.25);">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <div style="display: flex; align-items: center; gap: 12px; background: rgba(99, 102, 241, 0.4); border: 1px solid rgba(255,255,255,0.2); padding: 6px 18px; border-radius: 12px;">
                <span style="font-size: 20px;">💡</span>
                <span style="font-size: 20px; font-weight: 700; color: #C7D2FE;">SMART SWIPE ADVISOR</span>
            </div>
            <span style="color: #A5B4FC; font-size: 20px; font-weight: 600;">Optimal Cash Float</span>
        </div>
        <h3 style="font-size: 34px; font-weight: 800; margin-bottom: 8px;">Swipe Chase Sapphire Today</h3>
        <p style="font-size: 22px; color: #C7D2FE; line-height: 1.4; margin-bottom: 24px;">
            Statement just closed yesterday. Purchases made today get <strong style="color: #FFFFFF;">42 interest-free days</strong> until next due date (Dec 18).
        </p>
        <div style="display: flex; gap: 16px;">
            <div style="background: rgba(255,255,255,0.1); border-radius: 16px; padding: 14px 22px; flex: 1;">
                <p style="font-size: 18px; color: #A5B4FC;">Statement Due</p>
                <p style="font-size: 28px; font-weight: 800; margin-top: 4px;">$420.00</p>
                <span style="font-size: 18px; color: #FCA5A5;">Due in 12 days</span>
            </div>
            <div style="background: rgba(255,255,255,0.1); border-radius: 16px; padding: 14px 22px; flex: 1;">
                <p style="font-size: 18px; color: #A5B4FC;">Unbilled Spend</p>
                <p style="font-size: 28px; font-weight: 800; margin-top: 4px;">$215.00</p>
                <span style="font-size: 18px; color: #A7F3D0;">Cycles in 29 days</span>
            </div>
        </div>
    </div>
</div>

<!-- Card Rows List -->
<div style="padding: 0 40px; display: flex; flex-direction: column; gap: 16px;">
    <!-- Card 1 -->
    <div class="card" style="padding: 24px 28px; display: flex; justify-content: space-between; align-items: center;">
        <div style="display: flex; align-items: center; gap: 20px;">
            <div style="width: 72px; height: 50px; border-radius: 12px; background: linear-gradient(135deg, #1E3A8A 0%, #1E40AF 100%); color: white; display: flex; align-items: center; justify-content: center; font-size: 22px; font-weight: 800;">
                VISA
            </div>
            <div>
                <div style="display: flex; align-items: center; gap: 10px;">
                    <p style="font-size: 28px; font-weight: 800;">Chase Sapphire</p>
                    <span style="background: #FEF3C7; color: #92400E; font-size: 18px; font-weight: 700; padding: 2px 10px; border-radius: 8px;">DUE SOON</span>
                </div>
                <p style="font-size: 22px; color: #64748B; margin-top: 4px;">Statement Due: $420.00 • Nov 18</p>
            </div>
        </div>
        <div style="background: #10B981; color: white; padding: 14px 28px; border-radius: 18px; font-size: 22px; font-weight: 700;">
            Settle Bill
        </div>
    </div>

    <!-- Card 2 -->
    <div class="card" style="padding: 24px 28px; display: flex; justify-content: space-between; align-items: center;">
        <div style="display: flex; align-items: center; gap: 20px;">
            <div style="width: 72px; height: 50px; border-radius: 12px; background: linear-gradient(135deg, #B45309 0%, #D97706 100%); color: white; display: flex; align-items: center; justify-content: center; font-size: 22px; font-weight: 800;">
                AMEX
            </div>
            <div>
                <div style="display: flex; align-items: center; gap: 10px;">
                    <p style="font-size: 28px; font-weight: 800;">Amex Gold Card</p>
                    <span style="background: #ECFDF5; color: #065F46; font-size: 18px; font-weight: 700; padding: 2px 10px; border-radius: 8px;">✓ SETTLED</span>
                </div>
                <p style="font-size: 22px; color: #64748B; margin-top: 4px;">Statement: $0.00 • Unbilled: $310.00</p>
            </div>
        </div>
        <div style="text-align: right;">
            <p style="font-size: 28px; font-weight: 800; color: #0F172A;">$922.00</p>
            <span style="font-size: 18px; color: #64748B;">Total Debt</span>
        </div>
    </div>
</div>

{make_nav_bar(1)}
</body>
</html>
"""

# ----------------- SCREEN 4: BUDGETS & SAFE TO SPEND -----------------
def generate_screen4():
    return f"""<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<style>
* {{ box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Google Sans", sans-serif; }}
body {{ width: 1080px; height: 2400px; background: #F8FAFC; color: #0F172A; position: relative; overflow: hidden; }}
.card {{ background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 32px; box-shadow: 0 4px 16px rgba(15, 23, 42, 0.04); }}
</style>
</head>
<body>
{SVG_STATUS_BAR}

<!-- Top Header -->
<div style="padding: 10px 40px 20px;">
    <div style="display: flex; justify-content: space-between; align-items: center; height: 80px;">
        <div style="display: flex; align-items: center; gap: 16px;">
            <div style="width: 56px; height: 56px; border-radius: 18px; background: #059669; display: flex; align-items: center; justify-content: center; color: white;">
                <svg width="34" height="34" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 14h-2v-2h2v2zm0-4h-2V7h2v5z"/></svg>
            </div>
            <div>
                <h1 style="font-size: 40px; font-weight: 800; letter-spacing: -0.5px;">Budgets & Wealth</h1>
                <p style="font-size: 22px; color: #64748B; font-weight: 500;">Predictive Financial Radar</p>
            </div>
        </div>
        <div style="background: #F1F5F9; padding: 10px 24px; border-radius: 20px; font-size: 22px; font-weight: 700; color: #334155;">
            Nov 2026 ▾
        </div>
    </div>
</div>

<!-- Subtabs Bar -->
<div style="padding: 0 40px 24px; display: flex; gap: 14px;">
    <div style="background: #FFFFFF; color: #64748B; border: 1px solid #E2E8F0; padding: 14px 30px; border-radius: 24px; font-size: 24px; font-weight: 600;">Overview</div>
    <div style="background: #FFFFFF; color: #64748B; border: 1px solid #E2E8F0; padding: 14px 30px; border-radius: 24px; font-size: 24px; font-weight: 600;">Cards</div>
    <div style="background: #059669; color: white; padding: 14px 34px; border-radius: 24px; font-size: 24px; font-weight: 700; box-shadow: 0 4px 12px rgba(5, 150, 105, 0.25);">Budgets</div>
    <div style="background: #FFFFFF; color: #64748B; border: 1px solid #E2E8F0; padding: 14px 30px; border-radius: 24px; font-size: 24px; font-weight: 600;">Merchants & Tags</div>
</div>

<!-- Safe-To-Spend Today Hero Card -->
<div style="padding: 0 40px 24px;">
    <div class="card" style="padding: 34px; background: linear-gradient(135deg, #064E3B 0%, #065F46 100%); color: white; border: none; box-shadow: 0 12px 30px rgba(6, 78, 59, 0.25);">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <div style="display: flex; align-items: center; gap: 12px; background: rgba(16, 185, 129, 0.35); border: 1px solid rgba(255,255,255,0.2); padding: 6px 18px; border-radius: 12px;">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="#A7F3D0"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z"/></svg>
                <span style="font-size: 20px; font-weight: 700; color: #A7F3D0;">SAFE-TO-SPEND TODAY</span>
            </div>
            <span style="color: #A7F3D0; font-size: 22px; font-weight: 700;">24 Days Left</span>
        </div>
        <div style="display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 24px;">
            <div>
                <p style="font-size: 68px; font-weight: 900; letter-spacing: -1.5px; font-variant-numeric: tabular-nums;">$68.40</p>
                <p style="font-size: 22px; color: #A7F3D0; font-weight: 600;">Free Unallocated Daily Limit</p>
            </div>
            <div style="text-align: right; background: rgba(0,0,0,0.25); padding: 12px 20px; border-radius: 18px;">
                <p style="font-size: 18px; color: #A7F3D0;">Safe Surplus</p>
                <p style="font-size: 30px; font-weight: 800;">+$1,648.00</p>
            </div>
        </div>

        <!-- Formula Ring-Fence Bar -->
        <div style="background: rgba(0,0,0,0.3); border-radius: 20px; padding: 18px 24px; display: flex; justify-content: space-between; font-size: 20px; font-weight: 600;">
            <div><span style="color: #6EE7B7;">Liquid Cash:</span> $4,250.00</div>
            <div><span style="color: #FDA4AF;">− Card Debt:</span> $1,842.00</div>
            <div><span style="color: #FDE68A;">− 14D Bills:</span> $760.00</div>
        </div>
    </div>
</div>

<!-- Segmented Switcher -->
<div style="padding: 0 40px 20px; display: flex;">
    <div style="background: #E2E8F0; border-radius: 20px; padding: 6px; display: flex; width: 100%;">
        <div style="background: #FFFFFF; color: #0F172A; flex: 1; text-align: center; padding: 14px; border-radius: 16px; font-size: 22px; font-weight: 800; box-shadow: 0 2px 8px rgba(0,0,0,0.06);">
            🎯 Expense Budgets
        </div>
        <div style="background: transparent; color: #64748B; flex: 1; text-align: center; padding: 14px; border-radius: 16px; font-size: 22px; font-weight: 700;">
            🏦 Wealth & Savings (Vaults)
        </div>
    </div>
</div>

<!-- Budgets Progress Cards -->
<div style="padding: 0 40px; display: flex; flex-direction: column; gap: 18px;">

    <!-- Overall Budget -->
    <div class="card" style="padding: 26px 30px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
            <div style="display: flex; align-items: center; gap: 14px;">
                <span style="font-size: 32px;">📊</span>
                <div>
                    <h3 style="font-size: 28px; font-weight: 800;">Overall Monthly Budget</h3>
                    <p style="font-size: 20px; color: #10B981; font-weight: 700;">● Pacing Well: 54.7% spent vs 60.0% month elapsed</p>
                </div>
            </div>
            <div style="text-align: right;">
                <p style="font-size: 28px; font-weight: 800;">$1,642.50 <span style="font-size: 20px; color: #64748B; font-weight: 500;">/ $3,000.00</span></p>
                <span style="font-size: 18px; color: #10B981; font-weight: 700;">$1,357.50 Remaining</span>
            </div>
        </div>
        <!-- Progress Bar with Pacing Tick -->
        <div style="position: relative; height: 18px; background: #F1F5F9; border-radius: 9px; overflow: hidden; margin-top: 16px;">
            <div style="width: 54.7%; height: 100%; background: #10B981; border-radius: 9px;"></div>
        </div>
    </div>

    <!-- Category 1: Groceries -->
    <div class="card" style="padding: 24px 30px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
            <div style="display: flex; align-items: center; gap: 14px;">
                <span style="font-size: 30px;">🥦</span>
                <div>
                    <h4 style="font-size: 26px; font-weight: 800;">Groceries & Supermarket</h4>
                    <p style="font-size: 18px; color: #64748B;">48.2% of $600.00 limit</p>
                </div>
            </div>
            <div style="text-align: right;">
                <p style="font-size: 26px; font-weight: 800;">$289.40</p>
                <span style="font-size: 18px; color: #10B981; font-weight: 700;">$310.60 Left</span>
            </div>
        </div>
        <div style="height: 14px; background: #F1F5F9; border-radius: 7px; overflow: hidden;">
            <div style="width: 48.2%; height: 100%; background: #10B981; border-radius: 7px;"></div>
        </div>
    </div>

    <!-- Category 2: Dining Out -->
    <div class="card" style="padding: 24px 30px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
            <div style="display: flex; align-items: center; gap: 14px;">
                <span style="font-size: 30px;">🍽️</span>
                <div>
                    <h4 style="font-size: 26px; font-weight: 800;">Restaurants & Dining</h4>
                    <p style="font-size: 18px; color: #D97706; font-weight: 600;">● 78.5% of $400.00 limit (Near 80% mark)</p>
                </div>
            </div>
            <div style="text-align: right;">
                <p style="font-size: 26px; font-weight: 800;">$314.00</p>
                <span style="font-size: 18px; color: #D97706; font-weight: 700;">$86.00 Left</span>
            </div>
        </div>
        <div style="height: 14px; background: #F1F5F9; border-radius: 7px; overflow: hidden;">
            <div style="width: 78.5%; height: 100%; background: #F59E0B; border-radius: 7px;"></div>
        </div>
    </div>

</div>

{make_nav_bar(1)}
</body>
</html>
"""

# ----------------- SCREEN 5: ACCOUNTS & NET WORTH -----------------
def generate_screen5():
    return f"""<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<style>
* {{ box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Google Sans", sans-serif; }}
body {{ width: 1080px; height: 2400px; background: #F8FAFC; color: #0F172A; position: relative; overflow: hidden; }}
.card {{ background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 32px; box-shadow: 0 4px 16px rgba(15, 23, 42, 0.04); }}
</style>
</head>
<body>
{SVG_STATUS_BAR}

<!-- Top Header -->
<div style="padding: 10px 40px 20px;">
    <div style="display: flex; justify-content: space-between; align-items: center; height: 80px;">
        <div style="display: flex; align-items: center; gap: 16px;">
            <div style="width: 56px; height: 56px; border-radius: 18px; background: #0284C7; display: flex; align-items: center; justify-content: center; color: white;">
                <svg width="34" height="34" viewBox="0 0 24 24" fill="currentColor"><path d="M21 18v1c0 1.1-.9 2-2 2H5c-1.11 0-2-.9-2-2V5c0-1.1.89-2 2-2h14c1.1 0 2 .9 2 2v1h-9c-1.11 0-2 .9-2 2v8c0 1.1.89 2 2 2h9zm-9-2h10V8H12v8zm4-2.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z"/></svg>
            </div>
            <div>
                <h1 style="font-size: 40px; font-weight: 800; letter-spacing: -0.5px;">Accounts & Assets</h1>
                <p style="font-size: 22px; color: #64748B; font-weight: 500;">Double-Entry Portfolio</p>
            </div>
        </div>
        <div style="display: flex; gap: 14px;">
            <div style="width: 60px; height: 60px; border-radius: 50%; background: #FFFFFF; border: 1px solid #E2E8F0; display: flex; align-items: center; justify-content: center; color: #0F172A;">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
            </div>
        </div>
    </div>
</div>

<!-- Net Worth Hero Banner -->
<div style="padding: 0 40px 28px;">
    <div class="card" style="padding: 34px 38px; background: linear-gradient(135deg, #0F172A 0%, #1E293B 100%); color: white; border: none; box-shadow: 0 16px 36px rgba(15, 23, 42, 0.3);">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
            <p style="font-size: 22px; color: #94A3B8; font-weight: 700; text-transform: uppercase; letter-spacing: 1px;">Total Net Worth</p>
            <div style="background: rgba(16, 185, 129, 0.2); border: 1px solid rgba(16, 185, 129, 0.4); padding: 4px 16px; border-radius: 12px; font-size: 20px; font-weight: 700; color: #6EE7B7;">
                ▲ +$3,207.50 (+7.1%)
            </div>
        </div>
        <p style="font-size: 68px; font-weight: 900; letter-spacing: -1.5px; margin-bottom: 24px; font-variant-numeric: tabular-nums;">$48,650.00</p>
        
        <div style="background: rgba(255,255,255,0.06); border-radius: 20px; padding: 20px 24px; display: flex; justify-content: space-between;">
            <div>
                <p style="font-size: 18px; color: #6EE7B7; font-weight: 700;">TOTAL ASSETS</p>
                <p style="font-size: 32px; font-weight: 800; margin-top: 4px; color: #FFFFFF;">$54,820.00</p>
            </div>
            <div style="width: 1px; background: rgba(255,255,255,0.15);"></div>
            <div>
                <p style="font-size: 18px; color: #FDA4AF; font-weight: 700;">TOTAL LIABILITIES</p>
                <p style="font-size: 32px; font-weight: 800; margin-top: 4px; color: #FDA4AF;">-$6,170.00</p>
            </div>
            <div style="width: 1px; background: rgba(255,255,255,0.15);"></div>
            <div>
                <p style="font-size: 18px; color: #BAE6FD; font-weight: 700;">LIQUID CASH</p>
                <p style="font-size: 32px; font-weight: 800; margin-top: 4px; color: #BAE6FD;">$14,250.00</p>
            </div>
        </div>
    </div>
</div>

<!-- Account Groups -->
<div style="padding: 0 40px; display: flex; flex-direction: column; gap: 20px;">

    <!-- Bank Accounts -->
    <div class="card" style="padding: 24px 28px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <div style="display: flex; align-items: center; gap: 12px;">
                <span style="font-size: 26px;">🏦</span>
                <span style="font-size: 24px; font-weight: 800;">Bank Accounts (2)</span>
            </div>
            <span style="font-size: 24px; font-weight: 800; color: #10B981;">$14,250.00</span>
        </div>
        <div style="display: flex; flex-direction: column; gap: 12px;">
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 14px 18px; background: #F8FAFC; border-radius: 16px;">
                <div style="display: flex; align-items: center; gap: 12px;">
                    <span style="color: #D97706; font-size: 20px;">📌</span>
                    <div>
                        <p style="font-size: 22px; font-weight: 700;">Chase Premier Checking</p>
                        <p style="font-size: 18px; color: #64748B;">Primary Operational A/C</p>
                    </div>
                </div>
                <span style="font-size: 24px; font-weight: 800;">$8,420.00</span>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 14px 18px; background: #F8FAFC; border-radius: 16px;">
                <div>
                    <p style="font-size: 22px; font-weight: 700;">Ally High-Yield Savings</p>
                    <p style="font-size: 18px; color: #059669; font-weight: 600;">4.5% APY • Emergency Fund</p>
                </div>
                <span style="font-size: 24px; font-weight: 800;">$5,830.00</span>
            </div>
        </div>
    </div>

    <!-- Investments -->
    <div class="card" style="padding: 24px 28px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <div style="display: flex; align-items: center; gap: 12px;">
                <span style="font-size: 26px;">📈</span>
                <span style="font-size: 24px; font-weight: 800;">Investments & Portfolios</span>
            </div>
            <span style="font-size: 24px; font-weight: 800; color: #0284C7;">$34,740.00</span>
        </div>
        <div style="padding: 14px 18px; background: #F8FAFC; border-radius: 16px; display: flex; justify-content: space-between; align-items: center;">
            <div>
                <p style="font-size: 22px; font-weight: 700;">Vanguard S&P 500 Index</p>
                <p style="font-size: 18px; color: #10B981; font-weight: 700;">▲ +$4,210.00 (+13.8%) Unrealized Gain</p>
            </div>
            <div style="text-align: right;">
                <p style="font-size: 24px; font-weight: 800;">$34,740.00</p>
                <span style="font-size: 16px; color: #64748B;">Cost: $30,530.00</span>
            </div>
        </div>
    </div>

    <!-- Credit Cards & Loans -->
    <div class="card" style="padding: 24px 28px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <div style="display: flex; align-items: center; gap: 12px;">
                <span style="font-size: 26px;">💳</span>
                <span style="font-size: 24px; font-weight: 800;">Credit Cards & Liabilities</span>
            </div>
            <span style="font-size: 24px; font-weight: 800; color: #EF4444;">-$6,170.00</span>
        </div>
        <div style="display: flex; flex-direction: column; gap: 12px;">
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 14px 18px; background: #F8FAFC; border-radius: 16px;">
                <div>
                    <p style="font-size: 22px; font-weight: 700;">Chase Sapphire Preferred</p>
                    <p style="font-size: 18px; color: #64748B;">Due Nov 18 • Limit $5,000</p>
                </div>
                <span style="font-size: 24px; font-weight: 800; color: #EF4444;">-$920.00</span>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 14px 18px; background: #F8FAFC; border-radius: 16px;">
                <div>
                    <p style="font-size: 22px; font-weight: 700;">Auto Loan (Tesla Model 3)</p>
                    <p style="font-size: 18px; color: #64748B;">4.2% APR • 14 Months Left</p>
                </div>
                <span style="font-size: 24px; font-weight: 800; color: #EF4444;">-$4,328.00</span>
            </div>
        </div>
    </div>

</div>

{make_nav_bar(2)}
</body>
</html>
"""

def main():
    generators = [
        ("screen_1_daily.html", "screenshot_1_daily.png", generate_screen1()),
        ("screen_2_calendar.html", "screenshot_2_calendar.png", generate_screen2()),
        ("screen_3_cards.html", "screenshot_3_cards.png", generate_screen3()),
        ("screen_4_budgets.html", "screenshot_4_budgets.png", generate_screen4()),
        ("screen_5_accounts.html", "screenshot_5_accounts.png", generate_screen5()),
    ]
    
    for html_name, png_name, html_content in generators:
        html_path = os.path.join(OUTPUT_DIR, html_name)
        png_path = os.path.join(OUTPUT_DIR, png_name)
        
        with open(html_path, "w", encoding="utf-8") as f:
            f.write(html_content)
        print(f"Generated HTML: {html_name}")
        
        # Render via Chrome headless
        file_url = f"file:///{html_path.replace(os.sep, '/')}"
        cmd = [
            CHROME_PATH,
            "--headless=new",
            "--disable-gpu",
            "--hide-scrollbars",
            f"--window-size=1080,2400",
            f"--screenshot={png_path}",
            file_url
        ]
        res = subprocess.run(cmd, capture_output=True, text=True)
        if os.path.exists(png_path):
            size_kb = os.path.getsize(png_path) // 1024
            print(f"[OK] Rendered PNG: {png_name} ({size_kb} KB)")
        else:
            print(f"[ERROR] Failed to render {png_name}: {res.stderr}")

if __name__ == "__main__":
    main()

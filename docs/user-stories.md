# User stories — Blood Pressure Companion (rewrite)

Rewrite of a legacy app. New app: min API 36, EN + DE, not published, OSS on GitHub.

## Data model (reference)

One reading: date/time, systolic, diastolic (mmHg), pulse (1/min), optional measurement site, body position, device, sleep duration, comment. Pulse pressure (sys − dia) is derived.

- Site: right/left arm, right/left thigh, right/left wrist, right/left ankle, finger, toe, intra-arterial.
- Position: sitting, standing, reclining, lying, lying tilted left.

---

## Input

**IN-1 Record a reading**
As a user I want to record systolic, diastolic and pulse quickly so that logging takes seconds.
- Values are picked per field in 0–300; defaults 120/80/70 for the first reading.
- Save stores the reading and returns to the previous screen; cancel discards.

**IN-2 Pre-fill from last reading**
As a user I want a new reading pre-filled with my last values, site, position and device so that I only adjust what changed.
- New reading copies sys/dia/pulse, site, position, device from the latest reading; date/time is now.
- Comment and sleep are not copied.

**IN-3 Add context details**
As a user I want to add date/time, site, position, device, sleep duration and a comment so that readings are interpretable later.
- Date and time are editable via pickers (default: now).
- Site and position are optional single-choice lists ("no comment" = none).
- Sleep is entered as hours + minutes; empty means none.
- Comment is free text, trimmed.

**IN-4 Manage measuring devices**
As a user I want to maintain a list of my devices so that I can tag readings with the device used.
- Add a device by name; delete with confirmation.
- The device list is selectable while recording; "–" = none.
- A device found on an edited reading but missing from the list is added to it.

**IN-5 Edit a reading**
As a user I want to edit any stored reading so that I can correct mistakes.
- Opens the input form with all fields of that reading; title shows "Edit".
- Save overwrites the reading.

## List & detail

**LS-1 Browse readings**
As a user I want to see all readings newest first, grouped by month so that I can scan my history.
- Each row: date/time, sys/dia mmHg, pulse, comment (or "no comment").
- Each row is color-banded by category (see CL-1).
- Empty state explains how to add the first reading.
- A floating add button opens IN-1.

**LS-2 Delete with undo**
As a user I want to delete a reading by swiping and undo it so that mistakes are reversible.
- Swipe right deletes; a snackbar offers undo.
- Undo restores the reading and its month header.

**LS-3 Reading detail**
As a user I want to open a reading to see all its details so that I can review context.
- Shows date/time, sys/dia (large), pulse, pulse pressure, category label + color, site, position, device, sleep, comment (if any).
- Actions: edit (IN-5) and delete.

## Classification

**CL-1 Categorize readings**
As a user I want each reading categorized so that I see at a glance whether it's in range.
- Classification follows the guideline selected in ST-4; default is the most recent one.
- Supported guidelines (thresholds to be verified against the published sources during implementation):
  - **ESC 2024** (default): non-elevated <120/<70; elevated 120–139/70–89; hypertension ≥140/≥90.
  - **ESH 2023**: optimal <120/<80; normal 120–129/80–84; high normal 130–139/85–89; grade 1 140–159/90–99; grade 2 160–179/100–109; grade 3 ≥180/≥110.
  - **ACC/AHA 2017**: normal <120/<80; elevated 120–129/<80; stage 1 130–139/80–89; stage 2 ≥140/≥90; crisis >180/>120.
- The worse of the systolic and diastolic category wins.
- Low values (<90/<60) are flagged as "low" under every guideline.
- Categories can be evaluated on systolic only, diastolic only, or both (used by AN-4).
- Changing the guideline re-categorizes all readings; stored values are unchanged.

## Analysis

**AN-1 Choose period and time of day**
As a user I want to filter analysis by period and time of day so that I can compare e.g. mornings over the last 30 days.
- Periods: today, current week, last 7 days, current month, last 30 days, current year, last 365 days, all.
- Time of day: all, morning, afternoon, evening, night (boundaries from ST-3).
- Selected period, time of day and tab are remembered.

**AN-2 Readings chart**
As a user I want a time chart of systolic, diastolic and pulse so that I see trends.
- Lines for sys, dia, pulse over time; reference band for the personal target range (ST-4) and lines for the period max.
- Tapping a point shows that reading's comment.
- Full-screen view available.
- Shown only with ≥2 readings in the filter.

**AN-3 Statistics**
As a user I want max, mean and min values so that I can summarize a period.
- Tables of max/mean/min for sys, dia, pulse, by time of day and total.
- Charts of max/mean/min sys/dia per weekday (Mon–Sun).
- Full-screen view available.

**AN-4 Category distribution**
As a user I want to see how my readings split across categories so that I know how often I'm out of range.
- Pie chart + table with count and percent per category.
- Toggles: consider systolic, diastolic or both (at least one stays on).

## Export & share

**EX-1 Export a PDF report**
As a user I want a PDF report for a period so that I can give it to my doctor.
- Inputs: period (today, current week, last 7/30 days, current year, all, or custom from/to), time of day, paper size (A4/Letter).
- Content options (remembered): values table (+ details), max/mean/min tables, category distribution (all/sys/dia), and charts: readings, means, distribution.
- Header shows name, date of birth (ST-2) and period.
- The file is handed to the Android share sheet.

**EX-2 Export CSV**
As a user I want a CSV of my readings so that I can analyze them elsewhere.
- Same period and time-of-day filters as EX-1.
- Columns: date, time, sys, dia, pulse, site, position, device, sleep, comment; localized headers.
- File is handed to the share sheet.

## Home-screen widgets

**WG-1 Quick-add widget**
As a user I want a home-screen button that opens the input so that logging is one tap.
- Theme chosen on placement: dark, light, red.

## Settings

**ST-1 First day of week**
As a user I want to set the first day of the week so that "current week" matches my calendar.

**ST-2 Personal data for reports**
As a user I want to enter my name and date of birth so that they appear on PDF reports.
- Used only in the PDF header.

**ST-3 Time-of-day boundaries**
As a user I want to define when morning, afternoon, evening and night start so that time-of-day analysis fits my routine.
- Defaults: 05:00, 11:00, 17:00, 23:00.

**ST-4 Guideline and target range** _(new)_
As a user I want to choose the classification guideline and set my personal target range so that ratings match what my doctor uses.
- Guideline: one of CL-1's list; default ESC 2024.
- Target range: upper systolic and diastolic limit; default from the selected guideline's upper "normal" bound.
- Target range is shown in AN-2; readings above it are highlighted in LS-1 and LS-3.

## Info & support

**IF-1 Medical disclaimer**
As a user I must accept a disclaimer on first start so that I understand the app isn't a medical device.
- Shown until accepted; light/dark variant.

**IF-2 Help content**
As a user I want background on blood pressure and the classification table so that I can interpret my values.

---

## Backup & import

Replaces the legacy local and Dropbox backup. Dropbox is dropped; any cloud folder works through the system file picker.

**BK-1 Create a backup**
As a user I want to save all my data to a file so that I don't lose it.
- File contains all readings, devices and settings; versioned JSON (`schemaVersion`).
- User picks the location via the system file picker, or sends the file through the share sheet.
- Optional password (see BK-5).
- Shows a result: number of readings saved.

**BK-2 Restore a backup**
As a user I want to restore a backup file so that I get my data back on a new or reset phone.
- User picks a file via the system file picker.
- Preview before restoring: number of readings, date range, encrypted yes/no.
- Merge: readings with the same date/time as an existing one are skipped; devices are merged; settings are restored.
- Result: added, skipped, invalid.
- Unknown newer `schemaVersion` is rejected with a clear message.

**BK-3 Import a legacy `.bpc` file**
As a user of the old app I want to import my `.bpc` backup so that I keep my history.
- Same entry point as BK-2; format detected automatically.
- Legacy format: UTF-16, comma-separated, header line, rows end with `,$$$`; quoted fields may contain commas and line breaks.
- Columns: timestamp (ms), sys, dia, pulse, pulse pressure (ignored), site code, position code, device, sleep (hours, decimal), comment.
- Mapping: site/position codes per Data model; `--` → none; device `-` → none; sleep `-1.0` or `0` → none; comment `-` → none.
- Merge and result as BK-2.

**BK-4 Automatic backup**
As a user I want backups to happen without thinking so that my data is safe.
- Android's built-in backup (Google account) is enabled for the app's data.
- Optional: scheduled backup into a folder the user picks once (persisted folder access).
  - Runs daily when data changed; keeps the last 7 files.
  - Settings show last successful backup; failures show a notification.
  - Uses the BK-5 password if one is set.

**BK-5 Password-protect backups**
As a user I want to protect backup files with a password so that my health data stays private.
- Optional; set or remove in settings.
- Encrypted files use AES-GCM with a key derived from the password (PBKDF2 or Argon2, random salt).
- The password is kept in Android Keystore–protected storage so that scheduled backups (BK-4) work.
- Restore asks for the password; a wrong password shows an error; a forgotten password cannot be recovered — this is stated when setting it.

## Unclear

- **Classification standard.** Thresholds follow WHO 1999 with a custom "hypotension" band. Current guidelines (e.g. ESH 2023, ACC/AHA 2017) differ. Keep as is, switch, or make selectable? Make selectable.
- **Reference lines in AN-2** use the fixed "normal" threshold, not a personal target. Is a user-defined target range wanted? Yes.
- **Unused list context menu.** Edit/share/delete per item and "delete all in month" exist as resources but aren't wired. Wanted? No.

## Obsolete — replace

| Legacy | Why obsolete | Suggested replacement |
|---|---|---|
| Google Play licensing check | Not published, no monetization | Drop |
| Wheel pickers | Unmaintained, poor accessibility | Compose number input with steppers / keyboard |
| Vendored MPAndroidChart fork | Old fork, View-based | Compose charting (e.g. Vico) |
| iText PDF | AGPL/commercial licensing, heavy | Android `PdfDocument` API |
| Weight correlation via WeightCompanion | Couples to a legacy sibling app; Health Connect dropped | Drop (incl. PDF weight scatter in EX-1) |
| Sleep auto-fill via Sleep as Android | App-specific private provider; Health Connect dropped | Drop; sleep stays manual (IN-3) |
| Provenance (W3C PROV) export + ProvStore upload | Research feature, external service | Drop |
| MQTT forwarding (trusted all TLS certificates, plain-text password) | Security flaw; not wanted | Drop |
| Feedback email, store rating, other medando apps | Not published | Drop; link to the GitHub repo in About |
| WebView HTML help/disclaimer | Not themable, not accessible | Native Compose screens |
| Navigation drawer + separate activities | Outdated pattern | Single activity, bottom navigation |
| Storage permissions, `AsyncTask`, Loaders | Removed/deprecated APIs | Storage Access Framework / share sheet, coroutines |

## Open questions

1. **Migrating existing users' data.** How do existing users get their data into the new app? Options: same package ID with an in-place database migration, an import of the legacy `.bpc` backup file, or a fresh start.
There is no public user base anymore. The app is a fresh start. The app is for friends and family now on who installs it manually. They want to migrate their values. It is fine to migrate the legacy .bpc file they have to the new backup format, even once via a sed or whatever command and allow them to import the new backup file into the app.
2. **Package ID and store listing.** Replace the old app on Play (same ID, signing key needed) or publish as a new app?
No publishing at all, see above.
3. **Monetization.** Free, paid or in-app purchase? This decides what replaces the licensing module.
No monetization, see above. The code lives as OSS on GitHub.
4. **Health Connect.** Use it to read weight and sleep (replacing AN-5 and IG-1)? Should the app also write readings to it?
Drop health connect.
5. **Classification.** Which guideline? Should a personal target range be configurable?
Make it selectable, see above.
6. **MQTT.** Keep, harden or drop?
Drop.
7. **Widgets.** Keep both widgets? Use Material You dynamic colors instead of fixed themes?
Keep first, the design is your choice.
8. **Min SDK and languages.** Keep API 26 and EN + DE?
Make the min SDK the latest available, the app is a fresh start. Go with API 36.
9. **Scope of the first release.** Which stories are MVP? Suggested: IN-1…5, LS-1…3, CL-1, AN-1…4, EX-1…2, SH-1, ST-*, IF-*.
IN-1…5, LS-1…3, CL-1, AN-1…4, EX-1…2, WG-1, ST-1…4, IF-1…2, BK-1…3.

# IronVault

A local password and notes manager built with Java 21 + Swing. Features a dark
interface inspired by Bitwarden/macOS, with no cloud storage or external dependencies.

## Quick Start

1. Double-click **`IronVault.bat`** in the project folder.
2. If no master password is configured, the app opens directly. If one is configured,
   the lock screen appears.
3. Data is stored in **`%APPDATA%\\IronVault`** (usually
   `C:\\Users\\<username>\\AppData\\Roaming\\IronVault`):
   - `vault.iron` — encrypted vault (AES-256-GCM);
   - `auth.txt` — protection mode, salt, and PBKDF2 hash of the master password;
   - `vault.iron.key` — local key for the mode without a master password;
   - `README.txt` — a short note about the files.
4. On first launch, demo data is copied from
   `dist\\IronVault\\demo-vault.iron` (if the vault does not already exist).

The same app can be launched directly via **`dist\\IronVault\\IronVault.exe`**.
If the portable build does not exist yet, `IronVault.bat` will create it on first launch.

## Master Password

- **Set up:** Click the **“No Master Password”** card at the bottom of the sidebar
  and set a password (at least 8 characters).
- **Sign in:** The lock screen appears on the next launch. An incorrect password
  is rejected; the correct one unlocks the vault.
- **Storage:** The password itself is never saved. `auth.txt` contains only the salt,
  iteration count, and PBKDF2 hash. When the password is set or changed, `vault.iron`
  is re-encrypted with a new key.
- **Change or remove:** Use the same master password card. When the master password
  is removed, the app returns to local mode and creates `vault.iron.key` with the
  password `12345678`.
- **Lock now:** The lock button in the master password dialog immediately locks the
  vault and displays the sign-in screen.

## Build

Double-click **`build.bat`** or run the following in PowerShell:

```powershell
.\\build.ps1
```

The script creates:

- `dist\\IronVault.jar` — executable JAR (requires Java 21+);
- `dist\\IronVault\\IronVault.exe` — app with bundled Java
  (no JDK required on the target machine);
- `dist\\IronVault.ico` — application icon.

The build will not run while the app is open, and it preserves existing `*.iron*`
files in `dist\\IronVault` (your data will not be overwritten). Demo data from the
project root is copied only during the first portable build and serves as a template
for the first launch.

A JDK 21+ is required to build (the script checks `JAVA_HOME`, Temurin 21/25,
and `javac` in PATH).

## Features

- **Saved Passwords** and **Notes** sections, with search across all fields.
- Smoothly sliding details panel and clipboard copying.
- Create, edit, and delete entries.
- Upload a custom logo (PNG/JPG/GIF) with cropping and scaling.
- Password generator and strength indicator.
- Optional master password with a lock screen on startup.
- AES-256-GCM encryption; key derived using PBKDF2-HMAC-SHA256 (210,000 iterations).
- Atomic vault file writes and protection against data corruption.
- Keyboard shortcuts: `Ctrl+N` — new entry, `Ctrl+F` — search, `Esc` — close the panel.

## Data Storage

- Main vault: `%APPDATA%\\IronVault\\vault.iron`.
- `auth.txt` (master password settings) and `vault.iron.key`
  (local key for the mode without a master password) are created alongside it.
- `dist\\IronVault\\demo-vault.iron` is only a template for the first launch.
  It is not used if `%APPDATA%\\IronVault\\vault.iron` already exists.
- Override the location with: `--vault=path\\to\\file.iron`.
- The entire `%APPDATA%\\IronVault` folder can be copied to another computer.

## Security Note

Without a master password, the key `12345678` is stored in plain text next to the
vault (`vault.iron.key`). This protects against casual viewing, but not against
someone who has access to your computer or data folder.

With a master password, `vault.iron` is encrypted using a key derived from the
password, and only a hash is stored in `auth.txt`. Security in this mode depends
on the strength of the password you choose. Do not share the data folder with
others, and use a separate OS profile or disk encryption for truly important secrets.

## Project Structure

```text
src\\main\\java\\com\\ironvault\\
  model\\        vault entries and data
  security\\     encryption, master password, and vault file handling
  ui\\           Swing interface
  util\\         paths, formatting, clipboard
tools\\          demo data generator and helper utilities
dist\\           build output
```

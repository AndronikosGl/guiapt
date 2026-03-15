## <img src="src/aptcenter/icon.png" width="40px"> Apt Center
Apt Center is a lightweight GUI package manager for the APT backend written in Java. It aims to be a minimal, portable alternative to Synaptic, focusing on ease of use and portability across desktop environments

**The interface consists of 3 tabs:**
- One for finding available packages
- One for managing installed packages
- One for viewing apt log with timestamps

**App functions:**
- Option for installing/removing one or multiple packages
- Option for refreshing the package list
- Option for performing apt update/upgrade
- Options to search inside installed or available packages with the ability of filtering by name or description
- Option to quit the app
- Option to control the maximum number of packages to be listed by default from 100 up to 500. (Your choice is saved in the app's preferences)

**App reliability and safety**

- Once you try to exit the app through titlebar or by using the File>Exit guiapt option while an apt operation is running, you will be prompted for confirmation. If you insist on proceeding, you will be asked by pkexec to enter your password so the app can execute `dpkg --configure -a` to prevent any damage to the operating system
- The app can block your action if another process is holding the dpkg lock inform you about the process PID and lock file path. If both process are already running, the app will safely fail respecting the other process. 

**App Theming**
- By default, the app uses the modern FlatLaf Theme.
- By using the parameter `-legacyui` when launching the app from the terminal you can use it with the fallback motif theme so its guaranteed to work even on raw xorg
- By using the parameter `-forcegtk` when launching the app from the terminal you make it use the system gtk2 theme (not recomended by modern standards)

<img src="aptcenter3.png" width="500px">
<img src="aptcenter2.png" width="500px">
<img src="aptcenter1.png" width="500px">





## Copyright and licensing
Copyright © AndronikosGl 2026. All rights reserved.

This project is source-available. Modification and redistribution are not permitted. This project includes a modified asset based on Google Noto Emoji (SIL Open Font License 1.1).<br>
This project uses the FlatLaf look and feel library.<br>
https://www.formdev.com/flatlaf/<br>


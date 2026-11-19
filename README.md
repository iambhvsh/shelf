<div align="center">

<img src="assets/logo.png" width="112" alt="Shelf logo">

# Shelf

**The links you love. Beautifully organized. Entirely yours.**

A fast, private, offline-first bookmark manager for Android.<br>
Drop a link, grab the details, and move on.

<p>
  <a href="https://github.com/iambhvsh/shelf/releases/latest"><img src="https://img.shields.io/github/v/release/iambhvsh/shelf?style=flat-square&label=release" alt="Release"></a>
  <a href="https://github.com/iambhvsh/shelf/releases"><img src="https://img.shields.io/github/downloads/iambhvsh/shelf/total?style=flat-square&label=downloads" alt="Downloads"></a>
  <a href="https://github.com/iambhvsh/shelf/stargazers"><img src="https://img.shields.io/github/stars/iambhvsh/shelf?style=flat-square" alt="Stars"></a>
  <a href="https://github.com/iambhvsh/shelf/issues"><img src="https://img.shields.io/github/issues/iambhvsh/shelf?style=flat-square" alt="Issues"></a>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/iambhvsh/shelf?style=flat-square" alt="License: GPL-3.0"></a>
  <img src="https://img.shields.io/badge/platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Platform: Android">
  <img src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin, Jetpack Compose">
</p>

<p>
  <a href="https://github.com/iambhvsh/shelf/forks"><img src="https://img.shields.io/github/forks/iambhvsh/shelf?style=flat-square" alt="Forks"></a>
  <a href="https://github.com/iambhvsh/shelf/watchers"><img src="https://img.shields.io/github/watchers/iambhvsh/shelf?style=flat-square" alt="Watchers"></a>
  <a href="https://github.com/iambhvsh/shelf/commits"><img src="https://img.shields.io/github/last-commit/iambhvsh/shelf?style=flat-square" alt="Last commit"></a>
  <a href="https://github.com/iambhvsh/shelf/pulls"><img src="https://img.shields.io/github/issues-pr/iambhvsh/shelf?style=flat-square&label=PRs" alt="Pull requests"></a>
  <img src="https://img.shields.io/github/repo-size/iambhvsh/shelf?style=flat-square" alt="Repo size">
  <img src="https://img.shields.io/github/languages/top/iambhvsh/shelf?style=flat-square" alt="Top language">
  <img src="https://hits.sh/github.com/iambhvsh/shelf.svg?style=flat-square&label=views&color=555555" alt="Views">
</p>

<p>
  <a href="https://github.com/iambhvsh/shelf/releases/latest/download/shelf-release.apk"><img src="https://img.shields.io/badge/Download-APK-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Download APK"></a>
  <a href="https://rookieenough.github.io/Orion-Data/redirect.html?id=shelf"><img src="https://img.shields.io/badge/Get%20it%20on-Orion%20Store-8B5CF6?style=for-the-badge&logo=android&logoColor=white" alt="Get it on Orion Store"></a>
  <a href="https://shelf.iambhvsh.in"><img src="https://img.shields.io/badge/Website-shelf.iambhvsh.in-2563EB?style=for-the-badge&logo=googlechrome&logoColor=white" alt="Website"></a>
</p>

[Features](#features) &nbsp;|&nbsp; [Install](#install) &nbsp;|&nbsp; [Privacy](#privacy) &nbsp;|&nbsp; [Tech stack](#tech-stack) &nbsp;|&nbsp; [Build](#build-from-source) &nbsp;|&nbsp; [FAQ](#faq) &nbsp;|&nbsp; [Stats](#project-stats) &nbsp;|&nbsp; [Contributing](#contributing) &nbsp;|&nbsp; [Changelog](https://github.com/iambhvsh/shelf/releases)

</div>

---

## Overview

Most bookmark apps are either packed with features nobody asked for or gated behind a subscription. Shelf does one thing well: save a link, fetch its details, and stay out of your way.

- No accounts or sign-ups
- No cloud lock-in
- No ads or trackers
- Fully functional offline

Shelf is a fork of [Savr](https://github.com/qeiq/Savr) by [qeiq](https://github.com/qeiq), rebuilt with a refined Material 3 interface and a broader feature set.

## Features

**Capture**
- **Instant metadata.** Title, description and preview image are fetched automatically. Background crawling handles basic anti-bot protection.
- **Share-sheet saving.** Share from Chrome, X, Reddit or any app. The link is saved without opening Shelf.
- **Duplicate detection.** Saving a link that already exists in a collection warns you immediately.

**Organize**
- **Collections and tags.** Flat collections with tags for cross-referencing. No nested folders.
- **Pinning.** Keep important links at the top.
- **Bulk actions.** Select multiple bookmarks to delete or move them at once.
- **Sorting.** Order your library the way you prefer.
- **Personal notes.** Add private context directly on a bookmark card.

**Find and revisit**
- **Full-text search** across titles, URLs, descriptions and notes.
- **Custom reminders.** Choose the exact date and time Shelf should bring a link back.
- **Configurable tap action.** Open in browser, show a preview card, copy the URL, or share via the Android share sheet.
- **Grid and list views.**

**Customize**
- **Material 3** with Light, Dark and true OLED Black themes.
- **Eight accent colors**, or Dynamic Color (Material You) to follow your wallpaper.
- **Native splash screen** on Android 12 and later.

**Security and data**
- **App lock** using device biometrics.
- **Import and export** as JSON or HTML, including exports from desktop browsers.
- **Automatic daily backups** to internal storage and the Downloads folder.
- **In-app updates.** Shelf checks GitHub Releases and installs new versions over the air.

## Install

| Source | Link |
| :--- | :--- |
| GitHub Releases | [Download the latest APK](https://github.com/iambhvsh/shelf/releases/latest/download/shelf-release.apk) |
| Orion Store | [Get it on Orion Store](https://rookieenough.github.io/Orion-Data/redirect.html?id=shelf) |

1. Download the APK.
2. Allow installs from your browser or file manager when prompted.
3. Open Shelf. Future updates are delivered in-app.

## Privacy

| | |
| :--- | :--- |
| Accounts | None |
| Ads and trackers | None |
| Data location | Stored on your device only |
| Network use | Fetching link previews and checking GitHub for updates |
| Offline use | Fully supported |
| License | GPL-3.0 |

## Tech stack

| Layer | Technology |
| :--- | :--- |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVI with StateFlow |
| Database | Room |
| Networking | OkHttp, Jsoup |
| Image loading | Coil |

## Build from source

Requirements: latest stable Android Studio, JDK 17.

```bash
git clone https://github.com/iambhvsh/shelf.git
cd shelf
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/`.

## FAQ

<details>
<summary><b>Does Shelf sync across devices?</b></summary>
<br>
No. Data stays on your device. Use JSON or HTML export and import to move your library.
</details>

<details>
<summary><b>Does it need an internet connection?</b></summary>
<br>
Only to fetch link previews and check for updates. Everything else works offline.
</details>

<details>
<summary><b>Can I import bookmarks from my desktop browser?</b></summary>
<br>
Yes. Export your bookmarks as HTML from the browser and import the file in Shelf.
</details>

<details>
<summary><b>Where are backups stored?</b></summary>
<br>
Daily backups are saved to internal storage and the Downloads folder.
</details>

## Project stats

<p align="center">
  <a href="https://github.com/iambhvsh/shelf/releases"><img src="https://img.shields.io/github/downloads/iambhvsh/shelf/latest/total?style=for-the-badge&label=latest%20release%20downloads&color=3DDC84" alt="Latest release downloads"></a>
  <a href="https://github.com/iambhvsh/shelf/graphs/commit-activity"><img src="https://img.shields.io/github/commit-activity/m/iambhvsh/shelf?style=for-the-badge&label=commits%20per%20month&color=2563EB" alt="Commit activity"></a>
  <a href="https://github.com/iambhvsh/shelf/issues?q=is%3Aissue+is%3Aclosed"><img src="https://img.shields.io/github/issues-closed/iambhvsh/shelf?style=for-the-badge&label=issues%20closed&color=8B5CF6" alt="Closed issues"></a>
</p>

### Star history

<a href="https://star-history.com/#iambhvsh/shelf&Date">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=iambhvsh/shelf&type=Date&theme=dark">
    <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=iambhvsh/shelf&type=Date">
    <img src="https://api.star-history.com/svg?repos=iambhvsh/shelf&type=Date" alt="Star history chart" width="640">
  </picture>
</a>

### Contributors

<a href="https://github.com/iambhvsh/shelf/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=iambhvsh/shelf" alt="Contributors">
</a>

## Contributing

Bug reports and feature ideas are welcome. [Open an issue](https://github.com/iambhvsh/shelf/issues) with clear reproduction steps or a short description of the idea. For code changes, open an issue first to discuss the approach, then submit a pull request.

## Support

Shelf is free, open source and has no ads. If it helps keep your links in order, consider supporting development.

<a href="https://www.buymeacoffee.com/iambhvsh"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy me a coffee" height="50"></a>

A star on GitHub also helps.

## Acknowledgements

- [Savr](https://github.com/qeiq/Savr) by [qeiq](https://github.com/qeiq). The foundation Shelf is forked from.
- [Android-Link-Preview](https://github.com/vishalkumarsinghvi/Android-Link-Preview) by Vishal Kumar Singhvi. Powers metadata extraction.

## License

Licensed under the [GNU General Public License v3.0](LICENSE).
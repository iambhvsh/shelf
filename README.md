# Shelf — Website

> The marketing & landing site for **Shelf**, a beautiful, ad-free bookmark manager for Android.

Live at **[shelf.iambhvsh.in](https://shelf.iambhvsh.in)**

---

## About

This repository hosts the **`web` branch** — the source code for the Shelf website. The **`app` branch** contains the Android app source code.

Shelf is a free, open-source Android bookmark manager built with Material 3. Drop a URL, get a rich preview, and organise everything into collections — entirely on your device, with zero cloud dependency.

---

## Pages

| Route | Description |
|---|---|
| `/` | Landing page — hero, screenshots, features, tech stack, acknowledgements |
| `/privacy` | Privacy policy |
| `/terms` | Terms of service |

---

## App Features (showcased on the site)

- **Instant Metadata** — paste a URL and Shelf auto-fills title, description, and preview image
- **Direct Share Integration** — share links from any app straight into Shelf
- **Collections & Tags** — organise bookmarks without nested folder chaos
- **Pin & Hide** — surface your most important links, tuck away the rest
- **Bulk Actions & Sorting** — multi-select, move, delete, sort
- **Material You Theming** — Light / Dark / OLED Black + 8 accent colours + Dynamic Color
- **App Lock** — biometrics or PIN to protect your bookmarks
- **Lightning Fast Actions** — tap to open in browser, quick-preview, or copy URL
- **Grid & List View** — switch your browse layout on the fly
- **Full-Text Search** — search across titles, URLs, descriptions, and notes instantly
- **Personal Notes** — inline private notes on every bookmark card
- **Exact Reminders** — schedule a notification to revisit a link later
- **Import / Export** — JSON and HTML, fully offline, no cloud required
- **Auto Backup** — daily backups to device storage automatically

---

## Android App Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVI + StateFlow |
| Database | Room |
| Networking | OkHttp, Jsoup |
| Images | Coil |

---

## Website Tech Stack

| Layer | Technology |
|---|---|
| Framework | [SvelteKit](https://kit.svelte.dev) |
| Language | Svelte 5 + TypeScript (strict) |
| Rendering | SSG via `@sveltejs/adapter-static` |
| Routing | MPA (client-side router disabled) |
| Package Manager | pnpm |
| Linting | ESLint + Knip |

---

## Project Structure

```
shelf-web/
├── src/
│   ├── app.html                  # HTML shell (fonts, meta)
│   ├── lib/
│   │   ├── components/
│   │   │   ├── Hero.svelte       # Above-the-fold headline + download CTA
│   │   │   ├── ScreenshotGallery.svelte
│   │   │   ├── FeatureGrid.svelte
│   │   │   ├── TechStack.svelte
│   │   │   ├── Credits.svelte    # Open-source acknowledgements
│   │   │   ├── Header.svelte
│   │   │   ├── Footer.svelte     # Links + live version from GitHub API
│   │   │   ├── Card.svelte
│   │   │   └── Icon.svelte       # Material Symbols wrapper
│   │   ├── ripple.ts             # Material ripple effect
│   │   └── theme.ts              # Theme/dark-mode logic
│   └── routes/
│       ├── +layout.svelte
│       ├── +page.svelte          # Home
│       ├── privacy/+page.svelte
│       └── terms/+page.svelte
└── static/
    ├── favicon.ico
    ├── og-image.png
    ├── sitemap.xml
    ├── robots.txt
    └── screenshots/
```

---

## Getting Started

**Prerequisites:** Node.js ≥ 18, pnpm

```bash
# Install dependencies
pnpm install

# Start dev server
pnpm dev

# Type-check
pnpm check

# Lint
pnpm lint

# Find unused exports / dead code
pnpm knip

# Production build (outputs to build/)
pnpm build

# Preview production build locally
pnpm preview
```

---

## Download the App

[![Download APK](https://img.shields.io/github/v/release/iambhvsh/shelf?label=Download%20APK&color=6750A4&style=for-the-badge)](https://github.com/iambhvsh/shelf/releases/latest/download/shelf-release.apk)

---

## Links

- **App repo (Android source):** [github.com/iambhvsh/shelf/tree/app](https://github.com/iambhvsh/shelf/tree/app)
- **Issues:** [github.com/iambhvsh/shelf/issues](https://github.com/iambhvsh/shelf/issues)
- **Developer:** [iambhvsh.in](https://iambhvsh.in)

---

## Acknowledgements

- **[Savr by qeiq](https://github.com/qeiq/Savr)** — the Android app is a fork of this fantastic open-source project
- **[Android-Link-Preview by Vishal Kumar Singhvi](https://github.com/vishalkumarsinghvi/Android-Link-Preview)** — powers the link metadata magic

---

## License

This website is licensed under the **[GNU General Public License v3.0](https://github.com/iambhvsh/shelf/blob/main/LICENSE)**.

© 2025 Bhavesh Patil. All rights reserved.

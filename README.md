# Shelf — Website

The marketing and landing site for **Shelf**, a private, ad-free bookmark manager for Android.

Live at **[shelf.iambhvsh.in](https://shelf.iambhvsh.in)**

## Pages

| Route | Description |
|---|---|
| `/` | Landing page — hero, interactive screenshot showcase, features, and core architecture |
| `/download` | Download page — GitHub releases link and direct APK download |
| `/privacy` | Privacy policy — 100% offline and zero-tracking commitment |
| `/terms` | Terms of service |

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | [SvelteKit](https://kit.svelte.dev) (v2) |
| Language | Svelte 5 + TypeScript (strict) |
| Typography | Google Sans Flex Variable Font (WOFF2) |
| Rendering | SSG via `@sveltejs/adapter-static` |
| Routing | MPA (client-side router disabled) |
| Package Manager | pnpm |
| Quality & Linting | ESLint, svelte-check, Knip |

## Project Structure

```
web/
├── src/
│   ├── app.html                  # HTML shell (fonts preload, SEO & social meta)
│   ├── lib/
│   │   ├── components/
│   │   │   ├── Header.svelte     # Navigation header with brand, GitHub & download CTAs
│   │   │   ├── Footer.svelte     # Footer navigation, credits & version info
│   │   │   └── Icon.svelte       # Optimized inline SVG renderer
│   │   └── ripple.ts             # Material-style touch/pointer ripple interaction
│   └── routes/
│       ├── +layout.svelte        # Base layout shell
│       ├── +layout.ts            # SSG prerender configuration
│       ├── +page.svelte          # Main landing page with organic feature cards
│       ├── +error.svelte         # Fallback 404 & error page
│       ├── download/+page.svelte # Dedicated APK download page
│       ├── privacy/+page.svelte  # Offline privacy commitment
│       └── terms/+page.svelte    # Terms of service
└── static/
    ├── app-screens/              # Hi-res Google Pixel feature screenshots
    ├── assets/
    │   ├── css/main.css          # Design tokens, organic cards, fluid scroll
    │   ├── fonts/                # Google Sans Flex variable font (.woff2)
    │   ├── icons/                # Clean UI icon SVGs
    │   ├── icon.png              # High-resolution brand logo
    │   └── underline-*.svg       # Hand-drawn highlight accents
    ├── favicon.ico               # Multi-resolution favicon (16/32/48px)
    ├── og-image.png              # 1200x630 OpenGraph social card
    ├── robots.txt                # Search engine crawler rules
    ├── site.webmanifest          # PWA web app manifest
    └── sitemap.xml               # XML sitemap with all routes
```

## Getting Started

**Prerequisites:** Node.js ≥ 18, pnpm

```bash
pnpm install       # install dependencies
pnpm dev           # start dev server
pnpm check         # type-check
pnpm lint          # lint
pnpm knip          # dead code audit
pnpm build         # production build → build/
pnpm preview       # preview production build locally
```

## Links

- **Android app:** [github.com/iambhvsh/shelf/tree/app](https://github.com/iambhvsh/shelf/tree/app)
- **Issues:** [github.com/iambhvsh/shelf/issues](https://github.com/iambhvsh/shelf/issues)
- **Developer:** [iambhvsh.in](https://iambhvsh.in)

## License

Licensed under the **[GNU General Public License v3.0](https://github.com/iambhvsh/shelf/blob/main/LICENSE)**.

© 2026 Bhavesh Patil. All rights reserved.

# Shelf — Website

The marketing & landing site for **Shelf**, a beautiful, ad-free bookmark manager for Android.

Live at **[shelf.iambhvsh.in](https://shelf.iambhvsh.in)**

## Pages

| Route | Description |
|---|---|
| `/` | Landing page — hero, screenshots, features, tech stack, acknowledgements |
| `/privacy` | Privacy policy |
| `/terms` | Terms of service |

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | [SvelteKit](https://kit.svelte.dev) |
| Language | Svelte 5 + TypeScript (strict) |
| Rendering | SSG via `@sveltejs/adapter-static` |
| Routing | MPA (client-side router disabled) |
| Package Manager | pnpm |
| Linting | ESLint + Knip |

## Project Structure

```
src/
├── app.html                  # HTML shell (fonts, meta)
├── lib/
│   ├── components/
│   │   ├── Hero.svelte
│   │   ├── ScreenshotGallery.svelte
│   │   ├── FeatureGrid.svelte
│   │   ├── TechStack.svelte
│   │   ├── Credits.svelte
│   │   ├── Header.svelte
│   │   ├── Footer.svelte
│   │   ├── Card.svelte
│   │   └── Icon.svelte
│   ├── ripple.ts
│   └── theme.ts
└── routes/
    ├── +layout.svelte
    ├── +page.svelte
    ├── privacy/+page.svelte
    └── terms/+page.svelte
```

## Getting Started

**Prerequisites:** Node.js ≥ 18, pnpm

```bash
pnpm install       # install dependencies
pnpm dev           # start dev server
pnpm check         # type-check
pnpm lint          # lint
pnpm knip          # find unused exports / dead code
pnpm build         # production build → build/
pnpm preview       # preview production build locally
```

## Links

- **Android app:** [github.com/iambhvsh/shelf/tree/app](https://github.com/iambhvsh/shelf/tree/app)
- **Issues:** [github.com/iambhvsh/shelf/issues](https://github.com/iambhvsh/shelf/issues)
- **Developer:** [iambhvsh.in](https://iambhvsh.in)

## License

Licensed under the **[GNU General Public License v3.0](https://github.com/iambhvsh/shelf/blob/main/LICENSE)**.

© 2025 Bhavesh Patil. All rights reserved.

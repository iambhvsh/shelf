# Shelf - SvelteKit Rewrite

This is a rewrite of the Shelf website using SvelteKit. It is statically generated (SSG/SSR) for better SEO while providing interactive elements via standard Svelte CSR hydration without client-side routing (MPA mode).

## Features

- **Svelte 5**: Utilizing modern Runes (`$props`, `$state`, `$derived`).
- **Strict TypeScript**: 100% strict type coverage, with ESLint and Knip set up to enforce zero `any` or `@ts-ignore` usage.
- **SSR / SSG**: Statically built using `@sveltejs/adapter-static` into the `build/` directory.
- **MPA behavior**: SvelteKit's client-side router is disabled via `<body data-sveltekit-router="false">` to honor the "no SPA" requirement, giving normal browser page loads.
- **Inlined Content**: Every page/component has its static text content naturally embedded within its respective `.svelte` file rather than a separate dictionary.

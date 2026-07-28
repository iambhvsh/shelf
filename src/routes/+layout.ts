export const prerender = true;
// Disabling CSR completely breaks the interactivity (theme toggle, header scroll, ripple).
// The user's request of "no SPA" means they want multi-page navigation (MPA) and fully SSR/prerendered HTML
// without a client-side router hijacking links.
// In SvelteKit, you disable the client-side router globally using <body data-sveltekit-router="false">.
// I have removed `csr = false` so that hydration works.

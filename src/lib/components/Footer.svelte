<script lang="ts">
  import { onMount } from 'svelte';

  const footerLinks = [
    { label: 'Source Code', href: 'https://github.com/iambhvsh/shelf', external: true },
    { label: 'Report Issue', href: 'https://github.com/iambhvsh/shelf/issues', external: true },
    { label: 'Privacy Policy', href: '/privacy', external: false },
    { label: 'Terms of Service', href: '/terms', external: false },
    { label: 'GPLv3 License', href: 'https://github.com/iambhvsh/shelf/blob/main/LICENSE', external: true },
    { label: 'Developer Website', href: 'https://iambhvsh.in', external: true },
  ];

  const currentYear = new Date().getFullYear();
  let version = '1.0.12';

  onMount(async () => {
    try {
      const response = await fetch('https://api.github.com/repos/iambhvsh/shelf/releases/latest');
      if (response.ok) {
        const data = await response.json();
        if (data.tag_name) {
          version = data.tag_name.replace(/^v/, '');
        }
      }
    } catch (e) {
      console.error('Could not fetch latest release version.', e);
    }
  });
</script>

<footer class="app-footer">
  <div class="footer-links">
    {#each footerLinks as link, i (i)}
      <a
        class="footer-link"
        href={link.href}
        target={link.external ? "_blank" : undefined}
        rel={link.external ? "noopener" : undefined}
      >
        {link.label}
      </a>
    {/each}
    <div class="footer-meta">
      <span>Version {version} • Open Source</span>
      <span>© {currentYear} Bhavesh Patil. All rights reserved.</span>
    </div>
  </div>
  <div class="giant-footer-logo display">Shelf</div>
</footer>

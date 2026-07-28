<script lang="ts">
  const footerLinks = [
    { label: 'Source Code', href: 'https://github.com/iambhvsh/shelf', external: true },
    { label: 'Report Issue', href: 'https://github.com/iambhvsh/shelf/issues', external: true },
    { label: 'Privacy Policy', href: '/privacy', external: false },
    { label: 'Terms of Service', href: '/terms', external: false },
    { label: 'GPLv3 License', href: 'https://github.com/iambhvsh/shelf/blob/main/LICENSE', external: true },
    { label: 'Developer Website', href: 'https://iambhvsh.in', external: true },
  ];

  const currentYear = new Date().getFullYear();
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
      <span>Version 1.0.0 • Open Source</span>
      <span>© {currentYear} bhvsh. All rights reserved.</span>
    </div>
  </div>
  <div class="giant-footer-logo display">Shelf</div>
</footer>

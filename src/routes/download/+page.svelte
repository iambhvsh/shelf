<script lang="ts">
  import Icon from '$lib/components/Icon.svelte';
</script>

<svelte:head>
  <title>Download Shelf — Free, Offline Bookmark Manager for Android</title>
  <meta
    name="description"
    content="Download the latest release APK of Shelf (v1.2.0). A private, ad-free, 100% offline bookmark manager for modern Android."
  />

  <meta property="og:title" content="Download Shelf — Free, Offline Bookmark Manager for Android" />
  <meta
    property="og:description"
    content="Download the latest release APK of Shelf (v1.2.0). A private, ad-free, 100% offline bookmark manager for modern Android."
  />
  <meta property="og:url" content="https://shelf.iambhvsh.in/download" />
  <meta property="og:image" content="https://shelf.iambhvsh.in/og-image.png" />
  <meta property="og:image:width" content="1200" />
  <meta property="og:image:height" content="630" />

  <meta name="twitter:card" content="summary_large_image" />
  <meta name="twitter:title" content="Download Shelf — Free, Offline Bookmark Manager for Android" />
  <meta
    name="twitter:description"
    content="Download the latest release APK of Shelf (v1.2.0). A private, ad-free, 100% offline bookmark manager for modern Android."
  />
  <meta name="twitter:image" content="https://shelf.iambhvsh.in/og-image.png" />

  <link rel="canonical" href="https://shelf.iambhvsh.in/download" />

  <script type="application/ld+json">
    {
      "@context": "https://schema.org",
      "@type": "SoftwareApplication",
      "name": "Shelf",
      "operatingSystem": "Android 8.0+",
      "applicationCategory": "UtilitiesApplication",
      "softwareVersion": "1.2.0",
      "fileSize": "4.5MB",
      "offers": {
        "@type": "Offer",
        "price": "0",
        "priceCurrency": "USD"
      },
      "downloadUrl": "https://github.com/iambhvsh/shelf/releases/latest/download/shelf-release.apk",
      "url": "https://shelf.iambhvsh.in/download"
    }
  </script>
</svelte:head>

<div class="page-h-screen">
  <h1 class="display" style="font-size: clamp(32px, 6vw, 48px); text-wrap: balance;">Download Shelf</h1>
  <p style="color: var(--text-muted); font-size: clamp(16px, 3vw, 18px); margin-top: 16px; max-width: 500px; text-wrap: pretty;">
    Get the latest version of Shelf directly from our GitHub releases. Always free, independent, and ad-free.
  </p>

  <div style="margin-top: 32px;">
    <a
      class="btn-primary"
      href="https://github.com/iambhvsh/shelf/releases/latest/download/shelf-release.apk"
    >
      <Icon name="android" size={20} />
      <span>Download APK (v1.2.0)</span>
    </a>
  </div>

  <p style="color: var(--text-dim); font-size: 13px; margin-top: 24px;">
    Requires Android 8.0 or newer · 4.5 MB · GPL v3.0
  </p>
</div>

<script lang="ts">
  import Icon from '$lib/components/Icon.svelte';
</script>

<svelte:head>
  <title>Download Shelf - The links you love, entirely yours.</title>
  <meta name="description" content="Download Shelf, a beautiful, ad-free bookmark manager for Android. Get the latest APK.">

  <meta property="og:title" content="Download Shelf - The links you love, entirely yours.">
  <meta property="og:description" content="Download Shelf, a beautiful, ad-free bookmark manager for Android. Get the latest APK.">
  <meta property="og:url" content="https://shelf.iambhvsh.in/download">

  <meta name="twitter:title" content="Download Shelf - The links you love, entirely yours.">
  <meta name="twitter:description" content="Download Shelf, a beautiful, ad-free bookmark manager for Android. Get the latest APK.">

  <link rel="canonical" href="https://shelf.iambhvsh.in/download">
</svelte:head>

<div class="content-page" style="display: flex; flex-direction: column; align-items: center; text-align: center; justify-content: center; min-height: 50vh;">
  <h1 class="display" style="font-size:32px;color:var(--primary);">Download Shelf</h1>
  <p style="color:var(--on-surface-variant);font-size:16px; margin-top: 16px; max-width: 500px;">
    Get the latest version of Shelf directly from our GitHub releases.
  </p>

  <div style="margin-top: 32px;">
    <a class="btn-primary" href="https://github.com/iambhvsh/shelf/releases/latest/download/shelf-release.apk" style="display: inline-flex; align-items: center; justify-content: center; gap: 8px;">
      <Icon name="android" size={20} />
      Download APK
    </a>
  </div>

  <p style="color:var(--on-surface-variant);font-size:13px; margin-top: 24px;">
    Requires Android 8.0 or newer.
  </p>
</div>
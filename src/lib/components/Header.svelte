<script lang="ts">
  import Icon from './Icon.svelte';
</script>

<header class="site-header">
  <a href="#app" class="skip-link">Skip to content</a>
  <div class="site-header-inner">
    <a href="/" class="brand-link">
      <img src="/assets/icon.png" alt="Shelf logo" class="brand-icon" width="40" height="40" />
      <span class="brand-title display">Shelf</span>
    </a>

    <div class="header-nav">
      <a
        href="https://github.com/iambhvsh/shelf"
        class="icon-btn"
        aria-label="Shelf repository on GitHub"
        target="_blank"
        rel="noopener noreferrer"
      >
        <Icon name="github" size={21} />
      </a>

      <a
        href="https://github.com/iambhvsh/shelf/releases/latest/download/shelf-release.apk"
        class="btn-header-cta"
      >
        <Icon name="android" size={16} />
        <span>Download APK</span>
      </a>
    </div>
  </div>
</header>

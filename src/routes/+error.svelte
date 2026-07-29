<script lang="ts">
  import Icon from '$lib/components/Icon.svelte';
</script>

<svelte:head>
  <title>Page Not Found - Shelf</title>
</svelte:head>

<div class="page-h-screen">
  <h1 class="display" style="font-size:clamp(48px, 12vw, 80px);color:var(--primary);margin-bottom:8px;">404</h1>
  <p style="color:var(--on-surface-variant);font-size:clamp(16px, 4vw, 18px); margin-bottom: 24px;">
    We couldn't find the page you were looking for.
  </p>

  <a href="/" class="btn-primary" style="display: inline-flex; align-items: center; justify-content: center; gap: 8px;">
    <Icon name="arrow_back" size={20} />
    Return to Home
  </a>
</div>
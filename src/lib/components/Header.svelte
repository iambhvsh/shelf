<script lang="ts">
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import Icon from './Icon.svelte';
  import { toggleTheme, getTheme } from '$lib/theme';
  import type { Theme } from '$lib/theme';

  let currentTheme: Theme = $state('light');
  let scrolled = $state(false);
  let isHome = $derived($page.url.pathname === '/');

  onMount(() => {
    currentTheme = getTheme();

    const onScroll = () => {
      scrolled = window.scrollY > 10;
    };
    window.addEventListener('scroll', onScroll, { passive: true });
    onScroll();

    return () => {
      window.removeEventListener('scroll', onScroll);
    };
  });

  function handleThemeToggle() {
    currentTheme = toggleTheme();
  }
</script>

<header class="top-app-bar {scrolled ? 'scrolled' : ''}" id="appBar">
  <div class="top-app-bar-actions">
    <div class="top-app-bar-left">
      {#if !isHome}
        <button
          class="icon-btn"
          type="button"
          aria-label="Back to home"
          onclick={() => window.history.back()}
        >
          <Icon name="arrow_back" />
        </button>
      {/if}
    </div>
    <a href="/" class="brand display">Shelf</a>
    <button
      class="icon-btn"
      id="themeToggle"
      type="button"
      aria-label="Toggle theme"
      onclick={handleThemeToggle}
    >
      <Icon name={currentTheme === 'dark' ? 'light_mode' : 'dark_mode'} size={24} />
    </button>
  </div>
</header>

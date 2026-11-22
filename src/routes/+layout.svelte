<script lang="ts">
  import { onMount } from 'svelte';
  import { initRipple } from '$lib/ripple';
  import Header from '$lib/components/Header.svelte';
  import Footer from '$lib/components/Footer.svelte';

  let { children } = $props();

  onMount(() => {
    initRipple();
  });
</script>

<Header />
<main id="app" class="site-wrapper">
  {@render children()}
</main>
<Footer />

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

<div class="app-container">
  <Header />
  <main id="app">
    {@render children()}
  </main>
  <Footer />
</div>

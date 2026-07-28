<script lang="ts">
  import Hero from '$lib/components/Hero.svelte';
  import ScreenshotGallery from '$lib/components/ScreenshotGallery.svelte';
  import FeatureGrid from '$lib/components/FeatureGrid.svelte';
  import TechStack from '$lib/components/TechStack.svelte';
  import Credits from '$lib/components/Credits.svelte';
</script>

<svelte:head>
  <title>Shelf - The open-source bookmark manager for Android</title>
  <meta name="description" content="Shelf is a fast, open-source, ad-free bookmark manager for Android built entirely on Material 3. Drop a link, grab the details, and move on.">
  <meta name="keywords" content="bookmark manager, Android app, open-source bookmark app, Material 3 app, link organizer, offline bookmarks, Jetpack Compose app">
  <meta name="robots" content="index, follow">

  <meta property="og:type" content="website">
  <meta property="og:title" content="Shelf - The links you love, entirely yours.">
  <meta property="og:description" content="A fast, ad-free, open-source bookmark manager for Android built entirely on Material 3. No subscriptions, no cloud lock-in.">
  <meta property="og:url" content="https://shelf.iambhvsh.in">
  <meta property="og:image" content="/images/logo-transparent.webp">

  <meta name="twitter:card" content="summary_large_image">
  <meta name="twitter:title" content="Shelf - The links you love, entirely yours.">
  <meta name="twitter:description" content="A fast, ad-free, open-source bookmark manager for Android built entirely on Material 3. No subscriptions, no cloud lock-in.">
  <meta name="twitter:image" content="/images/logo-transparent.webp">

  <link rel="canonical" href="https://shelf.iambhvsh.in">
</svelte:head>

<Hero />

<section>
  <h2 class="section-title display">Screenshots</h2>
  <ScreenshotGallery />
</section>

<section>
  <h2 class="section-title display">Features</h2>
  <FeatureGrid />
</section>

<section>
  <h2 class="section-title display">Under the hood</h2>
  <TechStack />
</section>

<section>
  <h2 class="section-title display">Acknowledgements</h2>
  <Credits />
</section>

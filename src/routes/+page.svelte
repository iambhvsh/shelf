<script lang="ts">
  import Hero from '$lib/components/Hero.svelte';
  import ScreenshotGallery from '$lib/components/ScreenshotGallery.svelte';
  import FeatureGrid from '$lib/components/FeatureGrid.svelte';
  import TechStack from '$lib/components/TechStack.svelte';
  import Credits from '$lib/components/Credits.svelte';
</script>

<svelte:head>
  <title>Shelf - The links you love, entirely yours.</title>
  <meta name="description" content="The links you love, entirely yours. A beautiful, ad-free bookmark manager for Android crafted by iambhvsh. Save your links. Keep your privacy. Built for you on Material 3.">
  <meta name="robots" content="index, follow, max-image-preview:large, max-snippet:-1, max-video-preview:-1">

  <meta property="og:title" content="Shelf - The links you love, entirely yours.">
  <meta property="og:description" content="The links you love, entirely yours. A beautiful, ad-free bookmark manager for Android crafted by iambhvsh. Save your links. Keep your privacy. Built for you on Material 3.">
  <meta property="og:url" content="https://shelf.iambhvsh.in">

  <meta name="twitter:title" content="Shelf - The links you love, entirely yours.">
  <meta name="twitter:description" content="The links you love, entirely yours. A beautiful, ad-free bookmark manager for Android crafted by iambhvsh. Save your links. Keep your privacy. Built for you on Material 3.">

  <link rel="canonical" href="https://shelf.iambhvsh.in">

  <!-- Structured Data for SEO -->
  <script type="application/ld+json">
    {
      "@context": "https://schema.org",
      "@type": "SoftwareApplication",
      "name": "Shelf",
      "operatingSystem": "Android",
      "applicationCategory": "UtilitiesApplication",
      "offers": {
        "@type": "Offer",
        "price": "0",
        "priceCurrency": "USD"
      },
      "author": {
        "@type": "Person",
        "name": "iambhvsh",
        "url": "https://iambhvsh.in"
      },
      "description": "The links you love, entirely yours. A beautiful, ad-free bookmark manager for Android crafted by iambhvsh. Save your links. Keep your privacy. Built for you on Material 3.",
      "image": "https://shelf.iambhvsh.in/og-image.png",
      "url": "https://shelf.iambhvsh.in"
    }
  </script>
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

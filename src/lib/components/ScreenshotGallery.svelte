<script lang="ts">
  const screenshots = [
    { src: '/images/screenshots/screenshot-1.webp', alt: 'Shelf app home screen' },
    { src: '/images/screenshots/screenshot-2.webp', alt: 'Shelf app collections view' },
    { src: '/images/screenshots/screenshot-3.webp', alt: 'Shelf app settings and themes' },
  ];
</script>

<div class="screenshot-gallery">
  {#each screenshots as shot, i (i)}
    <img
      src={shot.src}
      alt={shot.alt}
      class="screenshot-img"
      loading="lazy"
      decoding="async"
    />
  {/each}
</div>

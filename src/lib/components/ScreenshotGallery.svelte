<script lang="ts">
  const screenshots = [
    { src: '/screenshots/screenshot-1.webp', alt: 'Shelf app home screen' },
    { src: '/screenshots/screenshot-2.webp', alt: 'Shelf app collections view' },
    { src: '/screenshots/screenshot-3.webp', alt: 'Shelf app settings and themes' },
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

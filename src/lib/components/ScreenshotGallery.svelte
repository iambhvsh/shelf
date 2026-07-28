<script lang="ts">
  const screenshots = [
    { src: 'https://raw.githubusercontent.com/iambhvsh/shelf/main/docs/assets/screenshot-1.png', alt: 'Shelf app home screen' },
    { src: 'https://raw.githubusercontent.com/iambhvsh/shelf/main/docs/assets/screenshot-2.png', alt: 'Shelf app collections view' },
    { src: 'https://raw.githubusercontent.com/iambhvsh/shelf/main/docs/assets/screenshot-3.png', alt: 'Shelf app settings and themes' },
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

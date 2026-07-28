<script lang="ts">
  import Card from './Card.svelte';

  const features = [
    { icon: 'link', title: 'Instant metadata', desc: 'Paste a URL and Shelf automatically pulls in the precise title, description, and preview image.' },
    { icon: 'share', title: 'Direct share', desc: 'Hit Share in Chrome, Twitter, or Reddit and send the link straight to your Shelf inbox natively.' },
    { icon: 'bookmarks', title: 'Collections', desc: 'No messy nested folders. Name a simple collection and drop your relevant links inside.' },
    { icon: 'bolt', title: 'Fast actions', desc: 'Configure what a tap actually does: open directly in browser, view preview, or copy URL.' },
    { icon: 'contrast', title: 'Dynamic themes', desc: 'Light, dark, or true AMOLED black. Pure Material 3 elements tailored specifically for Android.' },
  ];
</script>

<div class="card-grid">
  {#each features as f, i (i)}
    <Card iconName={f.icon} title={f.title} desc={f.desc} />
  {/each}
</div>

<script lang="ts">
  import Card from './Card.svelte';

  const credits = [
    {
      title: 'Savr, by qeiq',
      desc: 'Shelf is a direct fork of Savr. The core architecture of this application was built by qeiq.',
      href: 'https://github.com/qeiq/Savr',
    },
    {
      title: 'Android-Link-Preview',
      desc: "Massive thanks to Vishal Kumar Singhvi for the library that powers Shelf's metadata magic.",
      href: 'https://github.com/vishalkumarsinghvi/Android-Link-Preview',
    },
  ];
</script>

<div class="card-grid">
  {#each credits as c, i (i)}
    <Card
      title={c.title}
      desc={c.desc}
      action={{ label: 'View repository', href: c.href }}
    />
  {/each}
</div>

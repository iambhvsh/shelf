<script lang="ts">
  import Card from './Card.svelte';

  const credits = [
    {
      title: 'Savr, by qeiq',
      desc: 'First and foremost, the core of this application was built by qeiq in their amazing project Savr. Shelf is a fork of Savr, and without their fantastic open-source codebase, this project wouldn\'t exist.',
      href: 'https://github.com/qeiq/Savr',
    },
    {
      title: 'Android-Link-Preview',
      desc: 'A massive thank you to Vishal Kumar Singhvi for Android-Link-Preview, which powers the metadata magic.',
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

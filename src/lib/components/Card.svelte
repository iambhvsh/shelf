<script lang="ts">
  import Icon from './Icon.svelte';

  let {
    iconName,
    title,
    desc,
    action
  }: {
    iconName?: string;
    title: string;
    desc: string;
    action?: { label: string; href: string };
  } = $props();
</script>

<div class="m3-card m3-card-padded m3-card-interactive">
  {#if iconName}
    <Icon name={iconName} />
  {/if}
  <h3>{title}</h3>
  <!-- eslint-disable-next-line svelte/no-at-html-tags -->
  <p>{@html desc}</p>
  {#if action}
    <a class="m3-text-btn" href={action.href} target="_blank" rel="noopener">
      {action.label}
      <Icon name="arrow_forward" size={18} />
    </a>
  {/if}
</div>

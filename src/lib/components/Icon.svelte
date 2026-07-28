<script lang="ts">
  let { name, size = 24, style = '' }: { name: string; size?: number; style?: string } = $props();
</script>

<span
  class="icon"
  aria-hidden="true"
  style="width: {size}px; height: {size}px; --icon-url: url(/assets/icons/{name}.svg); {style}"
></span>

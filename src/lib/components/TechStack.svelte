<script lang="ts">
  const techStack = [
    { title: 'Language', subtitle: 'Kotlin' },
    { title: 'UI Toolkit', subtitle: 'Jetpack Compose + Material 3' },
    { title: 'Architecture', subtitle: 'MVI + StateFlow' },
    { title: 'Storage & Networking', subtitle: 'Room Database, OkHttp, Jsoup' },
  ];
</script>

<div class="m3-card m3-card-interactive">
  <div style="height:8px;"></div>
  {#each techStack as row, i (i)}
    <div class="list-item">
      <div class="list-item-content">
        <span class="list-item-title">{row.title}</span>
        <span class="list-item-subtitle">{row.subtitle}</span>
      </div>
    </div>
  {/each}
  <div style="height:8px;"></div>
</div>

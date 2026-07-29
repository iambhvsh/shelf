<script lang="ts">
  import Icon from './Icon.svelte';
</script>

<section class="hero-section">
  <h1 class="headline-large display">
    The <span class="text-primary">links you love</span>.<br>Beautifully <span class="text-primary">organized</span>.<br>Entirely <span class="text-primary">yours</span>.
  </h1>
  <div class="body-large" style="display: flex; flex-direction: column; gap: 1rem;">
    <p>
      I was tired of bookmark apps that felt like a chore. The ones packed with features I didn't ask for, or the ones hiding behind subscriptions just to save a URL.
    </p>
    <p>
      I wanted something clean. Something instant. So I took the brilliant foundation of an open-source app called Savr, and began crafting the exact tool I wanted on my home screen.
    </p>
    <p>
      Shelf is designed to do exactly one thing, flawlessly: <strong>drop a link, grab the details, and move on.</strong>
    </p>
    <p>
      No sign-ups. No cloud lock-in. No trackers. Just you and the links that matter.
    </p>
  </div>
  <a class="btn-primary" href="https://github.com/iambhvsh/shelf/releases/latest/download/shelf-release.apk">
    <Icon name="android" size={20} />
    Download APK
  </a>
</section>

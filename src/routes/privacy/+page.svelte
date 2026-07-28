<script lang="ts">
</script>

<svelte:head>
  <title>Privacy Policy — Shelf</title>
  <meta name="description" content="How Shelf handles your data: in short, it mostly doesn't collect any. Read the full privacy policy.">
  <meta name="keywords" content="Shelf privacy policy, Shelf bookmark manager privacy, Android app data collection">
  <meta property="og:title" content="Privacy Policy — Shelf">
  <meta property="og:description" content="Shelf is a local-first, ad-free bookmark manager. Here's exactly what data it touches.">
  <meta property="og:url" content="https://shelf.iambhvsh.in/privacy">
  <meta property="og:image" content="https://shelf.iambhvsh.in/assets/images/logo-transparent.webp">
</svelte:head>

<div class="content-page">
  <h1 class="display" style="font-size:32px;color:var(--primary);">Privacy Policy</h1>
  <p style="color:var(--on-surface-variant);font-size:13px;">Last updated: July 2026</p>

  <h2 style="font-size:20px;color:var(--primary);margin-top:8px;">Overview</h2>
  <p>Shelf is a local-first bookmark manager for Android. This policy explains exactly what data the app touches, why, and what it never does with your information.</p>

  <h2 style="font-size:20px;color:var(--primary);margin-top:8px;">Data We Collect</h2>
  <p>Shelf does not require an account or sign-in of any kind. Your bookmarks, collections, and settings are stored only in a local Room database on your own device.</p>
  <ul style="padding-left:20px;display:flex;flex-direction:column;gap:8px;">
    <li style="color:var(--on-surface-variant);font-size:15px;line-height:24px;">No name, email address, or phone number is ever requested.</li>
    <li style="color:var(--on-surface-variant);font-size:15px;line-height:24px;">No analytics, crash-reporting, or advertising SDK is bundled with the app.</li>
    <li style="color:var(--on-surface-variant);font-size:15px;line-height:24px;">No unique device identifiers or advertising IDs are read or generated.</li>
  </ul>

  <h2 style="font-size:20px;color:var(--primary);margin-top:8px;">Network Requests</h2>
  <p>When you save a link, Shelf fetches that page's metadata (title, description, preview image) directly from the URL you provide, the same way your browser would when opening a tab. This request goes straight from your device to the website you're bookmarking — never through a server operated by the developer.</p>

  <h2 style="font-size:20px;color:var(--primary);margin-top:8px;">Permissions the App Requests</h2>
  <ul style="padding-left:20px;display:flex;flex-direction:column;gap:8px;">
    <li style="color:var(--on-surface-variant);font-size:15px;line-height:24px;">Internet access — required solely to fetch metadata for links you save and to load preview images.</li>
    <li style="color:var(--on-surface-variant);font-size:15px;line-height:24px;">Notifications (optional) — used only for on-device alerts the app itself generates, such as import/export status.</li>
  </ul>
  <p>Shelf does not request access to contacts, location, camera, microphone, SMS, or call logs, and would have no use for them.</p>

  <h2 style="font-size:20px;color:var(--primary);margin-top:8px;">Data Sharing & Third Parties</h2>
  <p>No bookmark data, browsing history, or personal information is transmitted to, stored on, or sold to any server operated by the developer or any third party. Shelf has no backend of its own.</p>

  <h2 style="font-size:20px;color:var(--primary);margin-top:8px;">Data Retention & Deletion</h2>
  <p>All data lives in local app storage. Uninstalling Shelf, or clearing its storage in Android system settings, permanently deletes everything the app has saved. Nothing is recoverable from a remote server, because none exists.</p>

  <h2 style="font-size:20px;color:var(--primary);margin-top:8px;">Children's Privacy</h2>
  <p>Shelf does not knowingly collect any information from anyone, including children, since it collects no personal information from any user by design.</p>

  <h2 style="font-size:20px;color:var(--primary);margin-top:8px;">Open Source</h2>
  <p>Shelf is fully open-source. You're welcome to read the source code yourself to verify every claim on this page — see the link in the footer.</p>

  <h2 style="font-size:20px;color:var(--primary);margin-top:8px;">Changes to This Policy</h2>
  <p>If this policy ever changes — for example, if a future version of the app adds an optional feature that touches data differently — this page will be updated and the "last updated" date above will reflect that.</p>

  <h2 style="font-size:20px;color:var(--primary);margin-top:8px;">Contact</h2>
  <p>Questions about this policy can be raised via the project's GitHub repository, linked in the footer below.</p>
</div>

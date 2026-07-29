<script lang="ts">
  import Card from './Card.svelte';

  const features = [
    { icon: 'auto_awesome', title: 'Instant Metadata', desc: 'Paste a URL, and watch Shelf instantly pull in the title, description, and a high-quality preview image. It just works.' },
    { icon: 'share', title: 'Direct Share Integration', desc: 'Found something interesting in Chrome, Twitter, or Reddit? Just hit "Share" and send it straight to Shelf. It\'s saved in a split second without even opening the app.' },
    { icon: 'folder', title: 'Collections & Tags. Perfected.', desc: 'Group your bookmarks naturally. No messy nested folders—just name a collection, drop your links in, or add <strong>Tags</strong> to cross-reference and filter them effortlessly.' },
    { icon: 'push_pin', title: 'Pin Bookmarks', desc: 'Pin your most important links to the top for immediate access' },
    { icon: 'checklist', title: 'Bulk Actions & Sorting', desc: 'Manage your library efficiently. Select multiple bookmarks to delete or move them to collections at once, and sort them exactly how you like.' },
    { icon: 'palette', title: 'Designed for your screen', desc: 'Built with pure Material 3 and tailored for Android. Switch seamlessly between Light, Dark, or true OLED Black themes. Choose from 8 beautifully curated accent colors, or enable <strong>Dynamic Colors (Material You)</strong> to adapt to your wallpaper.' },
    { icon: 'lock', title: 'App Lock Privacy', desc: 'Keep your saved links for your eyes only. Secure the app using your device\'s built-in biometrics' },
    { icon: 'bolt', title: 'Lightning Fast Actions', desc: 'Configure exactly what happens when you tap a link. Open it directly in your browser, bring up a quick preview card without leaving the app, or instantly copy the URL to your clipboard.' },
    { icon: 'smartphone', title: 'View it your way', desc: 'Prefer a visual spread or a dense list? Toggle effortlessly between Grid and List view to browse your collection however you like.' },
    { icon: 'search', title: 'Full-Text Search', desc: 'Find exactly what you\'re looking for, the moment you need it. Instant full-text search across titles, URLs, descriptions, and your personal notes.' },
    { icon: 'edit_note', title: 'Personal Notes', desc: 'Add context to your bookmarks with private, inline notes directly on the cards. No more forgetting why you saved a link.' },
    { icon: 'schedule', title: 'Exact Reminders', desc: 'Schedule notifications to remind you to revisit specific links at exactly the right time.' },
    { icon: 'sync', title: 'Your data. Your rules.', desc: 'You own your bookmarks. Export them to JSON or HTML anytime. Import from your desktop browser just as easily. Works completely offline, prioritizing your privacy.' },
    { icon: 'save', title: 'Peace of mind, built-in', desc: 'Rest easy with automatic, daily backups saved straight to your device\'s internal storage and downloads folder. You never lose a thing.' }
  ];
</script>

<div class="card-grid">
  {#each features as f, i (i)}
    <Card iconName={f.icon} title={f.title} desc={f.desc} />
  {/each}
</div>

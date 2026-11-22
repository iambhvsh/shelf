<script lang="ts">
  import { onMount } from 'svelte';
  import { gsap } from 'gsap';
  import { ScrollTrigger } from 'gsap/ScrollTrigger';
  import Icon from '$lib/components/Icon.svelte';

  let pageContainer = $state<HTMLElement>();
  let screensTrackEl = $state<HTMLElement>();
  let progressBarEl = $state<HTMLElement>();

  const screenshots = [
    { src: '/app-screens/home.png', label: 'Home Feed', alt: 'Shelf home screen in Light mode with Purple accent' },
    { src: '/app-screens/preview.png', label: 'Rich Preview', alt: 'Shelf rich preview bottom sheet with link actions' },
    { src: '/app-screens/collections.png', label: 'Collections & Tags', alt: 'Shelf collections in OLED Black mode with Teal accent' },
    { src: '/app-screens/search.png', label: 'Instant Search', alt: 'Shelf instant search in Dark mode with Orange accent' },
    { src: '/app-screens/appearance.png', label: 'Accent Palettes', alt: 'Shelf appearance screen showing 9 accent color palettes' },
    { src: '/app-screens/storage.png', label: 'Offline Storage & Backup', alt: 'Shelf data export, import, and auto-backup in Dark Green' }
  ];

  const alsoIncluded = [
    {
      icon: 'lock',
      title: 'Biometric App Lock',
      desc: 'Gate your bookmark library behind your phone’s fingerprint scanner or biometric face unlock.'
    },
    {
      icon: 'save',
      title: 'Automated Daily Backups',
      desc: 'Automatic nightly snapshots saved directly to your phone storage and Downloads folder.'
    },
    {
      icon: 'edit_note',
      title: 'Personal Inline Notes',
      desc: 'Attach private thoughts, markdown quotes, or reminders directly to any saved link.'
    },
    {
      icon: 'bookmarks',
      title: 'Browser Bookmarks Import',
      desc: 'Bring your reading list from Chrome, Firefox, or Safari with standard HTML bookmark import.'
    },
    {
      icon: 'update',
      title: 'Direct GitHub Updates',
      desc: 'Built-in updater checks GitHub releases directly. Zero Google Play Services dependency.'
    },
    {
      icon: 'palette',
      title: 'Dynamic Wallpaper Theming',
      desc: 'Material You engine extracts accent palettes directly from your wallpaper, plus OLED black.'
    }
  ];

  const faqs = [
    {
      q: 'Is Shelf truly free and ad-free?',
      a: 'Yes, 100%. Shelf has no ads, no in-app purchases, no subscriptions, and no paywalled features. It is free software licensed under GPL v3.0.'
    },
    {
      q: 'Where are my bookmarks and link data stored?',
      a: 'Everything stays strictly on your Android device in a local Room SQLite database. Shelf does not operate remote servers, collect analytics, or phone home.'
    },
    {
      q: 'How does saving links from other apps work?',
      a: 'Whenever you find an article, video, or thread in Chrome, Twitter, Reddit, or any Android app, tap the system Share button and select Shelf. Shelf silently parses the title, description, cover image, and favicon in the background without pulling you away from what you were doing.'
    },
    {
      q: 'Can I import my existing bookmarks from my browser?',
      a: 'Yes. You can import any standard Netscape HTML bookmarks file exported from Chrome, Firefox, Brave, or Safari. You can also export your full Shelf library to standard JSON or HTML anytime.'
    },
    {
      q: 'Does Shelf work completely offline?',
      a: 'Yes. Once a link preview is captured, all metadata, favicons, tags, collections, and personal notes are cached locally. You can browse, search, and manage your entire reading library without any internet connection.'
    }
  ];

  onMount(() => {
    if (typeof window === 'undefined' || !pageContainer) return;

    // Respect user motion preferences (WCAG 2.2 / SonarLint S9440)
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (prefersReducedMotion) return;

    gsap.registerPlugin(ScrollTrigger);

    const ctx = gsap.context(() => {
      // 1. Hero Entrance & Scroll Parallax
      const heroTl = gsap.timeline({ defaults: { ease: 'power3.out' } });
      heroTl
        .from('.hero-headline', { y: 36, opacity: 0, duration: 0.9 })
        .from('.hero-lead', { y: 24, opacity: 0, duration: 0.8 }, '-=0.6')
        .from('.hero-actions', { y: 20, opacity: 0, duration: 0.7 }, '-=0.5')
        .from('.stage-hero', { scale: 0.95, opacity: 0, duration: 0.9 }, '-=0.6');

      // Hero phone plinth subtle receding depth on scroll
      gsap.to('.stage-hero .device-frame', {
        y: -40,
        scale: 0.97,
        ease: 'none',
        scrollTrigger: {
          trigger: '.hero-section',
          start: 'top top',
          end: 'bottom top',
          scrub: 1
        }
      });

      // 2. Showcase sections: smooth reveals
      const showcaseSections = gsap.utils.toArray<HTMLElement>('.showcase-section');
      showcaseSections.forEach((section) => {
        const header = section.querySelector('.showcase-header');
        const stage = section.querySelector('.stage-feature');

        if (header) {
          gsap.from(header, {
            y: 32,
            opacity: 0,
            duration: 0.8,
            ease: 'power2.out',
            scrollTrigger: {
              trigger: header,
              start: 'top 85%',
              toggleActions: 'play none none none'
            }
          });
        }

        if (stage) {
          gsap.from(stage, {
            scale: 0.96,
            opacity: 0.7,
            duration: 0.9,
            ease: 'power2.out',
            scrollTrigger: {
              trigger: stage,
              start: 'top 85%',
              toggleActions: 'play none none none'
            }
          });
        }
      });

      // 3. Showcase 1: Capture parallax float
      const stage1 = document.querySelectorAll('.showcase-section')[0];
      const stage1Frame = stage1?.querySelector('.device-frame');
      if (stage1Frame) {
        gsap.fromTo(
          stage1Frame,
          { y: 35 },
          {
            y: -35,
            ease: 'none',
            scrollTrigger: {
              trigger: stage1,
              start: 'top bottom',
              end: 'bottom top',
              scrub: 1.2
            }
          }
        );
      }

      // 4. Showcase 2: Dual Device Differential Parallax
      const dualWrap = document.querySelector('.device-dual-wrap');
      if (dualWrap) {
        const frames = dualWrap.querySelectorAll('.device-frame');
        if (frames.length === 2) {
          // Left frame (Collections - OLED Teal) moves at speed 1
          gsap.fromTo(
            frames[0],
            { y: 30 },
            {
              y: -30,
              ease: 'none',
              scrollTrigger: {
                trigger: dualWrap,
                start: 'top bottom',
                end: 'bottom top',
                scrub: 1
              }
            }
          );
          // Right frame (Search - Dark Orange) moves at speed 1.4 with differential offset
          gsap.fromTo(
            frames[1],
            { y: 60 },
            {
              y: -50,
              ease: 'none',
              scrollTrigger: {
                trigger: dualWrap,
                start: 'top bottom',
                end: 'bottom top',
                scrub: 1.4
              }
            }
          );
        }
      }

      // 5. Showcase 3: OLED Appearance Parallax
      const stage3 = document.querySelectorAll('.showcase-section')[2];
      const oledFrame = stage3?.querySelector('.device-frame');
      if (oledFrame) {
        gsap.fromTo(
          oledFrame,
          { y: 35 },
          {
            y: -35,
            ease: 'none',
            scrollTrigger: {
              trigger: stage3,
              start: 'top bottom',
              end: 'bottom top',
              scrub: 1.2
            }
          }
        );
      }

      // 6. Showcase 4: Storage & Offline Parallax
      const stage4 = document.querySelectorAll('.showcase-section')[3];
      const storageFrame = stage4?.querySelector('.device-frame');
      if (storageFrame) {
        gsap.fromTo(
          storageFrame,
          { y: 35 },
          {
            y: -35,
            ease: 'none',
            scrollTrigger: {
              trigger: stage4,
              start: 'top bottom',
              end: 'bottom top',
              scrub: 1.2
            }
          }
        );
      }

      // 7. "Also Included" Staggered Cascade
      const alsoStage = document.querySelector('.also-included-stage');
      if (alsoStage) {
        gsap.from('.feature-item', {
          y: 28,
          opacity: 0,
          stagger: 0.08,
          duration: 0.65,
          ease: 'power2.out',
          scrollTrigger: {
            trigger: '.features-grid',
            start: 'top 85%',
            toggleActions: 'play none none none'
          }
        });
      }

      // 8. Pinned Horizontal Screenshots Scrub (GSAP ScrollTrigger Native GPU translation)
      if (screensTrackEl) {
        const track = screensTrackEl;
        const getScrollDistance = () => {
          return track.scrollWidth - window.innerWidth;
        };

        const galleryTl = gsap.timeline({
          scrollTrigger: {
            trigger: '.gallery-pinned-section',
            pin: true,
            start: 'top top',
            end: () => `+=${Math.max(1200, getScrollDistance() + 400)}`,
            scrub: 0.8, // Fluid physics dampening catch-up
            invalidateOnRefresh: true
          }
        });

        // Scrub horizontally across all 6 screenshots
        galleryTl.to(track, {
          x: () => -Math.max(0, getScrollDistance()),
          ease: 'none',
          duration: 1
        });

        // Comfortable resting dwell period so the 6th screenshot stays centered before unpinning
        galleryTl.to({}, { duration: 0.15 });

        if (progressBarEl) {
          galleryTl.fromTo(
            progressBarEl,
            { scaleX: 0 },
            { scaleX: 1, ease: 'none', duration: 1 },
            0
          );
        }
      }

      // 9. Interactive 3D Pointer Tilt on Device Frames
      if (window.matchMedia('(pointer: fine)').matches) {
        const tiltFrames = pageContainer?.querySelectorAll<HTMLElement>('.device-frame') ?? [];
        tiltFrames.forEach((frame) => {
          let reqId: number | null = null;

          const onMouseMove = (e: MouseEvent) => {
            if (reqId) cancelAnimationFrame(reqId);
            reqId = requestAnimationFrame(() => {
              const rect = frame.getBoundingClientRect();
              const x = e.clientX - rect.left;
              const y = e.clientY - rect.top;
              const centerX = rect.width / 2;
              const centerY = rect.height / 2;
              const rotateX = ((y - centerY) / centerY) * -5;
              const rotateY = ((x - centerX) / centerX) * 5;

              gsap.to(frame, {
                rotateX,
                rotateY,
                duration: 0.35,
                ease: 'power1.out',
                overwrite: 'auto'
              });
            });
          };

          const onMouseLeave = () => {
            if (reqId) cancelAnimationFrame(reqId);
            gsap.to(frame, {
              rotateX: 0,
              rotateY: 0,
              duration: 0.7,
              ease: 'elastic.out(1, 0.6)',
              overwrite: 'auto'
            });
          };

          frame.addEventListener('mousemove', onMouseMove);
          frame.addEventListener('mouseleave', onMouseLeave);
        });
      }
    }, pageContainer);

    return () => {
      ctx.revert();
    };
  });
</script>

<svelte:head>
  <title>Shelf — Save links worth keeping</title>
  <meta
    name="description"
    content="Shelf is an independent, ad-free bookmark manager for Android. Pure Material 3, instant metadata previews, and 100% offline storage."
  />
  <meta name="robots" content="index, follow" />

  <meta property="og:title" content="Shelf — Save links worth keeping" />
  <meta
    property="og:description"
    content="Shelf is an independent, ad-free bookmark manager for Android. Pure Material 3, instant metadata previews, and 100% offline storage."
  />
  <meta property="og:url" content="https://shelf.iambhvsh.in" />
  <meta property="og:image" content="https://shelf.iambhvsh.in/og-image.png" />

  <meta name="twitter:title" content="Shelf — Save links worth keeping" />
  <meta
    name="twitter:description"
    content="Shelf is an independent, ad-free bookmark manager for Android. Pure Material 3, instant metadata previews, and 100% offline storage."
  />
  <meta name="twitter:image" content="https://shelf.iambhvsh.in/og-image.png" />
  <link rel="canonical" href="https://shelf.iambhvsh.in" />

  <script type="application/ld+json">
    {
      "@context": "https://schema.org",
      "@graph": [
        {
          "@type": "WebSite",
          "@id": "https://shelf.iambhvsh.in/#website",
          "url": "https://shelf.iambhvsh.in",
          "name": "Shelf",
          "description": "An independent, ad-free bookmark manager for Android.",
          "publisher": {
            "@type": "Person",
            "name": "iambhvsh",
            "url": "https://iambhvsh.in"
          }
        },
        {
          "@type": "SoftwareApplication",
          "@id": "https://shelf.iambhvsh.in/#software",
          "name": "Shelf",
          "operatingSystem": "Android 8.0+",
          "applicationCategory": "UtilitiesApplication",
          "softwareVersion": "1.2.0",
          "fileSize": "4.5MB",
          "offers": {
            "@type": "Offer",
            "price": "0",
            "priceCurrency": "USD"
          },
          "downloadUrl": "https://github.com/iambhvsh/shelf/releases/latest/download/shelf-release.apk",
          "license": "https://www.gnu.org/licenses/gpl-3.0.html",
          "author": {
            "@type": "Person",
            "name": "iambhvsh",
            "url": "https://iambhvsh.in"
          },
          "description": "An independent, ad-free bookmark manager for Android. Pure Material 3, instant metadata previews, and 100% offline storage.",
          "image": "https://shelf.iambhvsh.in/og-image.png",
          "screenshot": [
            "https://shelf.iambhvsh.in/app-screens/home.png",
            "https://shelf.iambhvsh.in/app-screens/preview.png",
            "https://shelf.iambhvsh.in/app-screens/collections.png",
            "https://shelf.iambhvsh.in/app-screens/search.png",
            "https://shelf.iambhvsh.in/app-screens/appearance.png",
            "https://shelf.iambhvsh.in/app-screens/storage.png"
          ]
        },
        {
          "@type": "FAQPage",
          "@id": "https://shelf.iambhvsh.in/#faq",
          "mainEntity": [
            {
              "@type": "Question",
              "name": "Is Shelf truly free and ad-free?",
              "acceptedAnswer": {
                "@type": "Answer",
                "text": "Yes, 100%. Shelf has no ads, no in-app purchases, no subscriptions, and no paywalled features. It is free software licensed under GPL v3.0."
              }
            },
            {
              "@type": "Question",
              "name": "Where are my bookmarks and link data stored?",
              "acceptedAnswer": {
                "@type": "Answer",
                "text": "Everything stays strictly on your Android device in a local Room SQLite database. Shelf does not operate remote servers, collect analytics, or phone home."
              }
            },
            {
              "@type": "Question",
              "name": "How does saving links from other apps work?",
              "acceptedAnswer": {
                "@type": "Answer",
                "text": "Whenever you find an article, video, or thread in Chrome, Twitter, Reddit, or any Android app, tap the system Share button and select Shelf. Shelf silently parses the title, description, cover image, and favicon in the background without pulling you away from what you were doing."
              }
            },
            {
              "@type": "Question",
              "name": "Can I import my existing bookmarks from my browser?",
              "acceptedAnswer": {
                "@type": "Answer",
                "text": "Yes. You can import any standard Netscape HTML bookmarks file exported from Chrome, Firefox, Brave, or Safari. You can also export your full Shelf library to standard JSON or HTML anytime."
              }
            },
            {
              "@type": "Question",
              "name": "Does Shelf work completely offline?",
              "acceptedAnswer": {
                "@type": "Answer",
                "text": "Yes. Once a link preview is captured, all metadata, favicons, tags, collections, and personal notes are cached locally. You can browse, search, and manage your entire reading library without any internet connection."
              }
            }
          ]
        }
      ]
    }
  </script>
</svelte:head>

<div bind:this={pageContainer} class="landing-content">
<!-- HERO -->
<section class="hero-section">
  <h1 class="hero-headline display">
    The links you save, <em>entirely yours</em>.
  </h1>

  <p class="hero-lead">
    Shelf is an independent, ad-free bookmark manager for Android. Tap Share in any app, and Shelf grabs the preview in the background. Pure Material 3, and 100% offline.
  </p>

  <div class="hero-actions">
    <a
      class="btn-primary"
      href="https://github.com/iambhvsh/shelf/releases/latest/download/shelf-release.apk"
    >
      <Icon name="android" size={18} />
      <span>Download APK</span>
    </a>

    <a
      class="btn-ghost"
      href="https://github.com/iambhvsh/shelf"
      target="_blank"
      rel="noopener noreferrer"
    >
      <Icon name="github" size={18} />
      <span>Source on GitHub</span>
    </a>
  </div>

  <div class="stage stage-hero">
    <div class="device-frame">
      <img
        src="/app-screens/home.png"
        alt="Shelf app home feed showing saved bookmarks"
        width="1080"
        height="2400"
        loading="eager"
        decoding="async"
      />
    </div>
  </div>
</section>

<!-- SHOWCASE 1: CAPTURE -->
<section class="showcase-section">
  <div class="showcase-header">
    <h2 class="display">Save links in a <em>second</em>.</h2>
    <p class="showcase-lead">
      Tap Share in Chrome, Twitter, Reddit, or any app. Shelf catches the title, artwork, and favicon quietly in the background without interrupting your flow.
    </p>
  </div>

  <div class="stage stage-feature">
    <div class="device-frame">
      <img
        src="/app-screens/preview.png"
        alt="Shelf rich preview bottom sheet"
        width="1080"
        height="2400"
        loading="eager"
        decoding="async"
      />
    </div>
  </div>
</section>

<!-- SHOWCASE 2: ORGANIZATION & SEARCH -->
<section class="showcase-section">
  <div class="showcase-header">
    <h2 class="display">Organize naturally. Find in <em>milliseconds</em>.</h2>
    <p class="showcase-lead">
      Group links into clean Collections, assign multi-tags, and search full text across titles, URLs, descriptions, and notes as you type.
    </p>
  </div>

  <div class="stage stage-feature">
    <div class="device-dual-wrap">
      <div class="device-frame">
        <img
          src="/app-screens/collections.png"
          alt="Shelf collections and tags screen"
          width="1080"
          height="2400"
          loading="eager"
          decoding="async"
        />
      </div>
      <div class="device-frame">
        <img
          src="/app-screens/search.png"
          alt="Shelf instant search screen"
          width="1080"
          height="2400"
          loading="eager"
          decoding="async"
        />
      </div>
    </div>
  </div>
</section>

<!-- SHOWCASE 3: MATERIAL YOU -->
<section class="showcase-section">
  <div class="showcase-header">
    <h2 class="display">Tailored for your phone and <em>wallpaper</em>.</h2>
    <p class="showcase-lead">
      Dynamic color harmonies extracted straight from your wallpaper with Material 3 Expressive, plus 9 hand-picked accent themes and true pitch-black OLED mode.
    </p>
  </div>

  <div class="stage stage-feature stage-oled">
    <div class="device-frame">
      <img
        src="/app-screens/appearance.png"
        alt="Shelf appearance and theme screen"
        width="1080"
        height="2400"
        loading="eager"
        decoding="async"
      />
    </div>
  </div>
</section>

<!-- SHOWCASE 4: PRIVACY & OFFLINE -->
<section class="showcase-section">
  <div class="showcase-header">
    <h2 class="display">No accounts. No trackers. <em>100% offline</em>.</h2>
    <p class="showcase-lead">
      Most bookmark apps store your reading habits on proprietary cloud servers. Shelf has no servers. Everything lives directly in a local Room database with biometric unlock.
    </p>
  </div>

  <div class="stage stage-feature">
    <div class="device-frame">
      <img
        src="/app-screens/storage.png"
        alt="Shelf offline storage, backup and biometric security screen"
        width="1080"
        height="2400"
        loading="eager"
        decoding="async"
      />
    </div>
  </div>
</section>

<!-- ALSO INCLUDED (UTILITY GRID) -->
<section class="also-included-section">
  <div class="also-included-stage">
    <div class="showcase-header">
      <h2 class="display">Also included</h2>
      <p class="showcase-lead">
        Built with thoughtful Android utility and zero bloat.
      </p>
    </div>

    <div class="features-grid">
      {#each alsoIncluded as item (item.title)}
        <div class="feature-item">
          <div class="feature-icon-wrap">
            <Icon name={item.icon} size={22} />
          </div>
          <h3 class="feature-title display">{item.title}</h3>
          <p class="feature-desc">{item.desc}</p>
        </div>
      {/each}
    </div>
  </div>
</section>

<!-- PINNED HORIZONTAL SCREENSHOTS SHOWCASE -->
<section class="gallery-pinned-section">
  <div class="gallery-sticky-wrap">
    <div class="showcase-header gallery-header">
      <h2 class="display">Screenshots</h2>
      <p class="showcase-lead">Every screen, built with care for modern Android.</p>
    </div>

    <div class="gallery-track-viewport">
      <div class="screens-track" bind:this={screensTrackEl}>
        {#each screenshots as shot (shot.src)}
          <div class="screen-thumb">
            <div class="device-frame">
              <img
                src={shot.src}
                alt={shot.alt}
                width="1080"
                height="2400"
                loading="eager"
                decoding="async"
              />
            </div>
            <div class="screen-caption">{shot.label}</div>
          </div>
        {/each}
      </div>
    </div>

    <!-- Smooth Pinned Scroll Progress Indicator -->
    <div class="gallery-progress-wrap" aria-hidden="true">
      <div class="gallery-progress-track">
        <div class="gallery-progress-bar" bind:this={progressBarEl}></div>
      </div>
    </div>
  </div>
</section>

<!-- PEOPLE OFTEN ASK (FAQ ACCORDION) -->
<section class="faq-section">
  <div class="showcase-header">
    <h2 class="display">People often ask</h2>
    <p class="showcase-lead">
      Honest answers about privacy, storage, and how Shelf works.
    </p>
  </div>

  <div class="faq-list">
    {#each faqs as faq (faq.q)}
      <details class="faq-row">
        <summary class="faq-summary">
          <span class="faq-question">{faq.q}</span>
          <span class="faq-toggle-icon" aria-hidden="true">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="5" x2="12" y2="19" class="faq-icon-v"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
          </span>
        </summary>
        <div class="faq-answer">
          <p>{faq.a}</p>
        </div>
      </details>
    {/each}
  </div>
</section>

<!-- COLOPHON & OPEN SOURCE HERITAGE -->
<section class="colophon-section">
  <div class="colophon-grid">
    <div class="colophon-block">
      <h3 class="display">Under the Hood</h3>
      <p>
        Built with Kotlin 2.x, Jetpack Compose, Material 3 Expressive, MVI architecture with StateFlow, Room SQLite database, OkHttp, Jsoup, and Coil image loader. Direct OTA updates check against GitHub releases.
      </p>
    </div>

    <div class="colophon-block">
      <h3 class="display">Open Source Heritage</h3>
      <p>
        Shelf is free software licensed under GPL v3.0. It originated as a fork of <a href="https://github.com/qeiq/Savr" target="_blank" rel="noopener noreferrer">Savr</a> by qeiq, with link metadata extraction powered by <a href="https://github.com/vishalkumarsinghvi/Android-Link-Preview" target="_blank" rel="noopener noreferrer">Android-Link-Preview</a>.
      </p>
    </div>
  </div>
</section>

<!-- DOWNLOAD CALLOUT STAGE -->
<section class="download-stage-section">
  <div class="stage stage-cta">
    <div class="stage-cta-content">
      <h2 class="display">Ready to save links cleanly?</h2>
      <p>Free, open source, and ad-free. Requires Android 8.0 or newer.</p>
      <a
        href="https://github.com/iambhvsh/shelf/releases/latest/download/shelf-release.apk"
        class="btn-primary"
      >
        <Icon name="android" size={18} />
        <span>Download APK (v1.2.0)</span>
      </a>
    </div>
  </div>
</section>
</div>

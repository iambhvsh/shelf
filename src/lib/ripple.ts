// A single delegated pointerdown listener drives ripples for every interactive
// element on the page, including ones the router injects later. This avoids
// re-querying and re-binding listeners on every navigation.

const RIPPLE_SELECTOR = '.icon-btn, .btn-primary, .m3-card-interactive, .list-item, .m3-text-btn, .footer-link';

function spawnRipple(target: HTMLElement, clientX: number, clientY: number): void {
  const rect = target.getBoundingClientRect();
  const diameter = Math.max(rect.width, rect.height);
  const radius = diameter / 2;

  const circle = document.createElement('span');
  circle.className = 'ripple-fx';
  circle.style.width = `${diameter}px`;
  circle.style.height = `${diameter}px`;
  circle.style.left = `${clientX - rect.left - radius}px`;
  circle.style.top = `${clientY - rect.top - radius}px`;

  const existing = target.querySelector(':scope > .ripple-fx');
  if (existing) existing.remove();

  target.appendChild(circle);
  setTimeout(() => circle.remove(), 500);
}

let initialized = false;

export function initRipple(root: Document = document): void {
  if (typeof document === 'undefined') return;
  if (initialized) return;
  initialized = true;

  root.addEventListener('pointerdown', (e: PointerEvent) => {
    const target = (e.target as Element).closest(RIPPLE_SELECTOR);
    if (!target) return;
    spawnRipple(target as HTMLElement, e.clientX, e.clientY);
  });
}

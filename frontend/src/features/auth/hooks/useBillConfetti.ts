import { useCallback, useEffect, useRef } from "react";

export interface UseBillConfettiOptions {
  maxBills?: number;
  spawnThrottleMs?: number;
  baseLifetimeMs?: number;
  driftRange?: number;
  billsPerScroll?: number;
  enabled?: boolean;
}

const DEFAULTS: Required<UseBillConfettiOptions> = {
  maxBills: 60,
  spawnThrottleMs: 45,
  baseLifetimeMs: 2200,
  driftRange: 160,
  billsPerScroll: 2,
  enabled: true,
};

const BILL_COLORS = ["var(--color-chart-2)", "var(--color-primary)"] as const;

const BILL_SVG = `<svg viewBox="0 0 32 32" fill="currentColor" preserveAspectRatio="xMidYMid meet" aria-hidden="true">
  <path d="M0 25v-18h32v18h-32zM2 8.938v14.062h28v-14.062h-28zM21 16c0-3.313-2.238-6-5-6h13v12h-13c2.762 0 5-2.687 5-6zM25 18c0.828 0 1.5-0.896 1.5-2s-0.672-2-1.5-2-1.5 0.896-1.5 2 0.672 2 1.5 2zM18.118 13.478c-0.015 0.055-0.036 0.094-0.062 0.119-0.027 0.025-0.063 0.037-0.109 0.037s-0.118-0.028-0.219-0.086c-0.1-0.059-0.223-0.121-0.368-0.189-0.146-0.068-0.314-0.13-0.506-0.187s-0.402-0.083-0.631-0.083c-0.18 0-0.336 0.021-0.469 0.065s-0.245 0.104-0.334 0.18c-0.090 0.077-0.156 0.17-0.2 0.277s-0.065 0.222-0.065 0.342c0 0.18 0.049 0.335 0.147 0.466s0.229 0.248 0.394 0.35c0.165 0.103 0.351 0.198 0.56 0.287 0.207 0.090 0.42 0.185 0.637 0.284 0.217 0.101 0.429 0.214 0.637 0.341s0.395 0.279 0.557 0.456 0.293 0.385 0.394 0.624c0.1 0.24 0.149 0.521 0.149 0.847 0 0.425-0.078 0.797-0.236 1.118s-0.373 0.588-0.645 0.802c-0.271 0.215-0.587 0.376-0.949 0.484-0.046 0.014-0.096 0.020-0.143 0.031v1.092h-0.983v-0.963c-0.013 0-0.024 0.002-0.036 0.002-0.279 0-0.539-0.022-0.778-0.067s-0.451-0.101-0.634-0.164c-0.184-0.064-0.336-0.131-0.459-0.201s-0.211-0.132-0.265-0.186c-0.054-0.054-0.093-0.132-0.116-0.234-0.023-0.103-0.035-0.249-0.035-0.441 0-0.129 0.004-0.237 0.013-0.325s0.022-0.158 0.041-0.213 0.043-0.093 0.075-0.116c0.031-0.022 0.067-0.034 0.109-0.034 0.058 0 0.14 0.034 0.247 0.103s0.243 0.145 0.409 0.228c0.167 0.084 0.365 0.159 0.597 0.229 0.231 0.068 0.499 0.103 0.803 0.103 0.2 0 0.379-0.024 0.537-0.072s0.293-0.115 0.403-0.203 0.194-0.196 0.253-0.325c0.059-0.13 0.088-0.273 0.088-0.433 0-0.183-0.051-0.34-0.15-0.472-0.1-0.131-0.23-0.247-0.391-0.35-0.16-0.102-0.342-0.197-0.546-0.287s-0.414-0.185-0.631-0.284c-0.216-0.1-0.427-0.213-0.631-0.341s-0.386-0.278-0.546-0.455c-0.16-0.177-0.291-0.387-0.39-0.628s-0.15-0.531-0.15-0.868c0-0.388 0.072-0.728 0.215-1.021s0.337-0.537 0.581-0.73 0.531-0.338 0.862-0.434c0.17-0.050 0.346-0.085 0.526-0.109v-1.034h0.983v1.034c0.039 0.005 0.078 0.003 0.117 0.009 0.191 0.029 0.371 0.068 0.537 0.118 0.167 0.049 0.314 0.104 0.444 0.167 0.129 0.062 0.214 0.113 0.256 0.155s0.069 0.076 0.085 0.105c0.014 0.029 0.026 0.068 0.037 0.116s0.018 0.108 0.021 0.182c0.004 0.072 0.006 0.163 0.006 0.271 0 0.121-0.003 0.224-0.009 0.308-0.009 0.079-0.019 0.149-0.034 0.203zM11 16c0 3.313 2.238 6 5 6h-13v-12h13c-2.762 0-5 2.687-5 6zM7 14c-0.829 0-1.5 0.896-1.5 2s0.671 2 1.5 2c0.828 0 1.5-0.896 1.5-2s-0.672-2-1.5-2z"/>
</svg>`;

interface PooledBill {
  el: HTMLDivElement;
  busy: boolean;
}

function prefersReducedMotion(): boolean {
  if (typeof window === "undefined" || !window.matchMedia) return false;
  return window.matchMedia("(prefers-reduced-motion: reduce)").matches;
}

export function useBillConfetti(
  containerRef: React.RefObject<HTMLDivElement | null>,
  options: UseBillConfettiOptions = {},
) {
  const opts = { ...DEFAULTS, ...options };
  const poolRef = useRef<PooledBill[]>([]);
  const lastSpawnRef = useRef(0);

  const spawn = useCallback(
    (direction: 1 | -1) => {
      const now = performance.now();
      if (now - lastSpawnRef.current < opts.spawnThrottleMs) return;
      lastSpawnRef.current = now;

      const pool = poolRef.current;
      let bill = pool.find((b) => !b.busy);

      if (!bill && pool.length < opts.maxBills) {
        const el = document.createElement("div");
        el.className = "bill-confetti__item";
        el.innerHTML = BILL_SVG;
        containerRef.current?.appendChild(el);
        bill = { el, busy: false };
        pool.push(bill);
      }

      if (!bill) {
        bill = pool[0];
      }

      bill.busy = true;
      const { el } = bill;

      const size = 16 + Math.random() * 26;
      const normalizedSize = (size - 16) / 26;
      const opacity = Math.max(
        0.2,
        Math.min(0.45, 0.35 + normalizedSize * 0.2 + (Math.random() - 0.5) * 0.04),
      );
      const driftX = (Math.random() - 0.5) * opts.driftRange;
      const rotateStart = Math.random() * 360;
      const rotateEnd = rotateStart + (Math.random() - 0.5) * 720;
      const lifetime = opts.baseLifetimeMs + Math.random() * 800 + normalizedSize * 400;
      const startX = Math.random() * 100;
      const color = BILL_COLORS[Math.random() < 0.7 ? 0 : 1];
      const easing =
        direction === 1
          ? "cubic-bezier(0.55, 0.055, 0.675, 0.19)"
          : "cubic-bezier(0.25, 0.46, 0.45, 0.94)";

      el.style.setProperty("--x", `${driftX}px`);
      el.style.setProperty("--y", direction === 1 ? "120vh" : "-120vh");
      el.style.setProperty("--r-start", `${rotateStart}deg`);
      el.style.setProperty("--r-end", `${rotateEnd}deg`);
      el.style.setProperty("--opacity", String(opacity));
      el.style.setProperty("--dur", `${lifetime}ms`);
      el.style.setProperty("--ease", easing);
      el.style.color = color;
      el.style.width = `${size}px`;
      el.style.height = `${size / 2}px`;
      el.style.left = `${startX}vw`;
      el.style.top = direction === 1 ? "-30px" : "calc(100vh + 30px)";

      el.style.animation = "none";
      el.offsetHeight;
      el.style.animation = `bill-confetti-fall var(--dur) var(--ease) forwards`;

      const onEnd = () => {
        bill!.busy = false;
        el.removeEventListener("animationend", onEnd);
      };
      el.addEventListener("animationend", onEnd, { once: true });
    },
    [containerRef, opts.spawnThrottleMs, opts.maxBills, opts.baseLifetimeMs, opts.driftRange],
  );

  useEffect(() => {
    if (!opts.enabled || prefersReducedMotion()) return;

    const burst = (direction: 1 | -1, count: number) => {
      for (let i = 0; i < count; i++) {
        window.setTimeout(() => spawn(direction), i * 45);
      }
    };

    const onWheel = (e: WheelEvent) => {
      if (Math.abs(e.deltaY) < 2) return;
      const dir = e.deltaY > 0 ? 1 : -1;
      burst(dir, opts.billsPerScroll);
      if (Math.abs(e.deltaY) > 40) burst(dir, 1);
    };

    let lastTouchY = 0;
    const onTouchStart = (e: TouchEvent) => {
      lastTouchY = e.touches[0].clientY;
    };
    const onTouchMove = (e: TouchEvent) => {
      const y = e.touches[0].clientY;
      const delta = lastTouchY - y;
      lastTouchY = y;
      if (Math.abs(delta) < 3) return;
      burst(delta > 0 ? 1 : -1, opts.billsPerScroll);
    };

    window.addEventListener("wheel", onWheel, { passive: true });
    window.addEventListener("touchstart", onTouchStart, { passive: true });
    window.addEventListener("touchmove", onTouchMove, { passive: true });

    const introTimers = [200, 350, 500, 650].map((delay, i) =>
      window.setTimeout(() => spawn(i % 2 === 0 ? 1 : -1), delay),
    );

    return () => {
      window.removeEventListener("wheel", onWheel);
      window.removeEventListener("touchstart", onTouchStart);
      window.removeEventListener("touchmove", onTouchMove);
      introTimers.forEach(clearTimeout);
      poolRef.current.forEach((b) => b.el.remove());
      poolRef.current = [];
    };
  }, [opts.enabled, opts.billsPerScroll, spawn]);
}

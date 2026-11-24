import { useEffect, useRef, useState } from 'react';
import type { PointerEvent } from 'react';
import { useTranslation } from 'react-i18next';
import {
  Activity,
  BarChart3,
  BriefcaseBusiness,
  Database,
  GripVertical,
  Layers,
  WalletCards,
} from 'lucide-react';
import type { LucideIcon } from 'lucide-react';

import { cn } from '@/lib/utils';

export type InvestmentTab =
  | 'portfolio'
  | 'activity'
  | 'performance'
  | 'assets'
  | 'holdings'
  | 'status';

const TABS: Array<{
  id: InvestmentTab;
  labelKey: string;
  icon: LucideIcon;
}> = [
  {
    id: 'portfolio',
    labelKey: 'tabs.portfolio',
    icon: BriefcaseBusiness,
  },
  {
    id: 'activity',
    labelKey: 'tabs.activity',
    icon: Activity,
  },
  {
    id: 'performance',
    labelKey: 'tabs.performance',
    icon: BarChart3,
  },
  {
    id: 'assets',
    labelKey: 'tabs.assets',
    icon: Layers,
  },
  {
    id: 'holdings',
    labelKey: 'tabs.holdings',
    icon: WalletCards,
  },
  {
    id: 'status',
    labelKey: 'tabs.status',
    icon: Database,
  },
];

interface InvestmentTabsProps {
  active: InvestmentTab;
  onChange: (tab: InvestmentTab) => void;
}

type Position = { x: number; y: number };
const DOCK_MARGIN = 16;

function constrain(position: Position, bounds: DOMRect): Position {
  return {
    x: Math.max(DOCK_MARGIN, Math.min(position.x, document.documentElement.clientWidth - bounds.width - DOCK_MARGIN)),
    y: Math.max(64, Math.min(position.y, window.innerHeight - bounds.height - DOCK_MARGIN)),
  };
}

export function InvestmentTabs({
  active,
  onChange,
}: InvestmentTabsProps) {
  const { t } = useTranslation(['investments', 'nav']);
  const dock = useRef<HTMLDivElement>(null);
  const drag = useRef<{ pointer: number; offset: Position } | null>(null);
  const [position, setPosition] = useState<Position>();

  useEffect(() => {
    function onResize() {
      const bounds = dock.current?.getBoundingClientRect();
      if (bounds) setPosition((current) => current && constrain(current, bounds));
    }
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);

  function startDrag(event: PointerEvent<HTMLButtonElement>) {
    if (!event.isPrimary || event.button !== 0 || !dock.current) return;
    const bounds = dock.current.getBoundingClientRect();
    drag.current = { pointer: event.pointerId, offset: { x: event.clientX - bounds.x, y: event.clientY - bounds.y } };
    event.currentTarget.setPointerCapture(event.pointerId);
  }

  return (
    <div className="min-h-12 min-w-0">
    <div
      ref={dock}
      className={cn(
        'flex w-fit max-w-full items-center rounded-lg border border-border bg-card p-1',
        position && 'md:fixed md:z-30 md:max-w-[calc(100vw-2rem)] md:shadow-md',
      )}
      style={position ? { left: position.x, top: position.y } : undefined}
    >
      <button type="button"
        aria-label={t('nav:moveNavigation')}
        title={t('nav:moveNavigation')}
        className="hidden size-10 shrink-0 touch-none cursor-grab place-items-center rounded-md border-e border-border text-muted-foreground hover:text-foreground focus-visible:outline-2 focus-visible:outline-ring active:cursor-grabbing md:grid"
        onPointerDown={startDrag}
        onPointerMove={(event) => {
          const current = drag.current;
          if (current?.pointer !== event.pointerId || !dock.current) return;
          setPosition(constrain({ x: event.clientX - current.offset.x, y: event.clientY - current.offset.y }, dock.current.getBoundingClientRect()));
        }}
        onLostPointerCapture={() => { drag.current = null; }}
        onKeyDown={(event) => {
          if (event.key === 'Escape') {
            setPosition(undefined);
            return;
          }
          const deltas: Record<string, [number, number]> = {
            ArrowLeft: [-24, 0], ArrowRight: [24, 0], ArrowUp: [0, -24], ArrowDown: [0, 24],
          };
          const delta = deltas[event.key];
          if (!delta || !dock.current) return;
          event.preventDefault();
          const bounds = dock.current.getBoundingClientRect();
          setPosition(constrain({ x: bounds.x + delta[0], y: bounds.y + delta[1] }, bounds));
        }}>
        <GripVertical className="size-4" aria-hidden="true" />
      </button>
    <div
      className="flex min-w-0 gap-1 overflow-x-auto no-scrollbar"
      role="tablist"
      aria-label={t('title')}
    >
      {TABS.map((tab) => {
        const Icon = tab.icon;
        const isActive = active === tab.id;

        return (
          <button
            key={tab.id}
            type="button"
            role="tab"
            aria-selected={isActive}
            onClick={() => onChange(tab.id)}
            className={cn(
              'inline-flex shrink-0 items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition-colors',
              isActive
                ? 'bg-primary text-primary-foreground shadow-sm'
                : 'text-muted-foreground hover:bg-accent/60 hover:text-foreground',
            )}
          >
            <Icon className="size-4" aria-hidden="true" />
            {t(tab.labelKey)}
          </button>
        );
      })}
    </div>
    </div>
    </div>
  );
}

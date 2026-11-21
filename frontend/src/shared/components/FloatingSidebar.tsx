import { useEffect, useId, useRef, useState } from 'react';
import type { KeyboardEvent, PointerEvent } from 'react';
import { GripVertical, UserRound } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import { cn } from '@/lib/utils';
import type { NavItem } from '@/shared/config/navigation';
import { prefetchRoute } from '@/shared/config/routeLoaders';
import { Button } from '@/shared/components/core/button';

type Position = { x: number; y: number };

interface FloatingSidebarProps {
  items: NavItem[];
  current?: NavItem;
  className?: string;
}

// Each route mounts its own Sidebar; keep the position when navigating in this tab.
let lastPosition: Position | undefined;

function constrain(position: Position, bounds: DOMRect): Position {
  const margin = 16;
  const maxX = Math.max(margin, document.documentElement.clientWidth - bounds.width - margin);
  const maxY = Math.max(margin, window.innerHeight - bounds.height - margin);
  return {
    x: Math.min(maxX, Math.max(margin, position.x)),
    y: Math.min(maxY, Math.max(Math.min(80, maxY), position.y)),
  };
}

export function FloatingSidebar({ items, current, className }: FloatingSidebarProps) {
  const { t } = useTranslation('nav');
  const destinationsId = useId();
  const dock = useRef<HTMLElement>(null);
  const trigger = useRef<HTMLButtonElement>(null);
  const drag = useRef<{ pointer: number; offset: Position } | null>(null);
  const [position, setPosition] = useState(lastPosition);
  const [hovered, setHovered] = useState(false);
  const [opened, setOpened] = useState(false);
  const [dragging, setDragging] = useState(false);
  const [expandLeft, setExpandLeft] = useState(false);
  const expanded = !dragging && (hovered || opened);
  const orderedItems = current ? items : [...items, undefined];
  const Icon = current?.icon ?? UserRound;
  const currentLabel = t(current?.labelKey ?? 'profile');

  useEffect(() => {
    function adjustToViewport() {
      const bounds = dock.current?.getBoundingClientRect();
      if (!bounds?.width) return;
      const next = lastPosition ? constrain(lastPosition, bounds) : undefined;
      if (next) {
        lastPosition = next;
        setPosition(next);
      }
      setExpandLeft((next?.x ?? bounds.left) + bounds.width / 2 > window.innerWidth / 2);
    }
    adjustToViewport();
    window.addEventListener('resize', adjustToViewport);
    return () => window.removeEventListener('resize', adjustToViewport);
  }, []);

  function move(next: Position, bounds: DOMRect) {
    const bounded = constrain(next, bounds);
    lastPosition = bounded;
    setPosition(bounded);
    if (!drag.current) {
      setExpandLeft(bounded.x + bounds.width / 2 > window.innerWidth / 2);
    }
  }

  function startDrag(event: PointerEvent<HTMLButtonElement>) {
    if (!event.isPrimary || event.button !== 0 || !dock.current) return;
    const bounds = dock.current.getBoundingClientRect();
    drag.current = {
      pointer: event.pointerId,
      offset: { x: event.clientX - bounds.left, y: event.clientY - bounds.top },
    };
    event.currentTarget.setPointerCapture(event.pointerId);
    setHovered(false);
    setOpened(false);
    setDragging(true);
  }

  function dragDock(event: PointerEvent<HTMLButtonElement>) {
    const active = drag.current;
    if (active?.pointer !== event.pointerId || !dock.current) return;
    move(
      { x: event.clientX - active.offset.x, y: event.clientY - active.offset.y },
      dock.current.getBoundingClientRect(),
    );
  }

  function finishDrag(event: PointerEvent<HTMLButtonElement>) {
    if (drag.current?.pointer !== event.pointerId) return;
    drag.current = null;
    setDragging(false);
    const bounds = dock.current?.getBoundingClientRect();
    if (bounds) {
      setExpandLeft((lastPosition?.x ?? bounds.left) + bounds.width / 2 > window.innerWidth / 2);
    }
  }

  function moveWithKeys(event: KeyboardEvent<HTMLButtonElement>) {
    const offsets: Record<string, Position> = {
      ArrowLeft: { x: -24, y: 0 },
      ArrowRight: { x: 24, y: 0 },
      ArrowUp: { x: 0, y: -24 },
      ArrowDown: { x: 0, y: 24 },
    };
    const offset = offsets[event.key];
    if (!offset || !dock.current) return;
    event.preventDefault();
    const bounds = dock.current.getBoundingClientRect();
    move({ x: bounds.left + offset.x, y: bounds.top + offset.y }, bounds);
  }

  return (
    <nav
      ref={dock}
      aria-label={t('mainNav')}
      className={cn(
        'fixed z-40 hidden h-12 w-24 select-none md:block',
        !position && 'bottom-6 left-6',
        className,
      )}
      style={position ? { left: position.x, top: position.y } : undefined}
      onPointerEnter={(event) => {
        if (event.pointerType !== 'touch' && !drag.current) setHovered(true);
      }}
      onPointerLeave={() => setHovered(false)}
      onBlur={(event) => {
        if (!event.currentTarget.contains(event.relatedTarget)) setOpened(false);
      }}
      onKeyDown={(event) => {
        if (event.key !== 'Escape' || !expanded) return;
        event.stopPropagation();
        setHovered(false);
        setOpened(false);
        trigger.current?.focus();
      }}
    >
      <div
        className={cn(
          'absolute inset-y-0 flex items-center gap-0.5 rounded-lg border border-border bg-card px-0.5 shadow-md',
          expanded ? 'w-max' : 'w-full',
          expandLeft ? 'right-0 flex-row-reverse' : 'left-0',
        )}
      >
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className={cn('size-11 touch-none text-muted-foreground', dragging ? 'cursor-grabbing' : 'cursor-grab')}
          aria-label={t('moveNavigation')}
          title={t('moveNavigation')}
          onPointerDown={startDrag}
          onPointerMove={dragDock}
          onPointerUp={finishDrag}
          onPointerCancel={finishDrag}
          onLostPointerCapture={finishDrag}
          onKeyDown={moveWithKeys}
        >
          <GripVertical className="size-5" aria-hidden="true" />
        </Button>

        <ul id={destinationsId} className="flex items-center gap-0.5">
          {orderedItems.map((item) => {
            if (item === current) {
              return (
                <li key={item?.id ?? 'profile'}>
                  <Button
                    ref={trigger}
                    type="button"
                    variant="ghost"
                    size="icon"
                    className="size-11 bg-accent/50 text-primary hover:bg-accent/70 hover:text-primary"
                    aria-current="page"
                    aria-label={currentLabel}
                    aria-controls={destinationsId}
                    aria-expanded={expanded}
                    title={currentLabel}
                    onClick={() => {
                      if (opened) setHovered(false);
                      setOpened(!opened);
                    }}
                  >
                    <Icon className="size-5" aria-hidden="true" />
                  </Button>
                </li>
              );
            }

            if (!item) return null;

            return (
              <li key={item.id} hidden={!expanded}>
                <Link
                  to={item.path}
                  aria-label={t(item.labelKey)}
                  title={t(item.labelKey)}
                  onMouseEnter={() => prefetchRoute(item.path)}
                  onFocus={() => {
                    setOpened(true);
                    prefetchRoute(item.path);
                  }}
                  onClick={() => {
                    setOpened(false);
                    setHovered(false);
                  }}
                  className="grid size-11 place-items-center rounded-md text-muted-foreground transition-colors hover:bg-accent/30 hover:text-foreground focus-visible:outline-2 focus-visible:-outline-offset-2 focus-visible:outline-ring"
                >
                  <item.icon className="size-5" aria-hidden="true" />
                </Link>
              </li>
            );
          })}
        </ul>
      </div>
    </nav>
  );
}

import { useRef, type KeyboardEvent } from 'react';
import { useTranslation } from 'react-i18next';

import { cn } from '@/lib/utils';

import { CATEGORY_COLORS } from '../constants';

interface ColorPickerProps {
  value: string;
  onChange: (color: string) => void;
  disabled?: boolean;
  labelledBy?: string;
}

export function ColorPicker({
  value,
  onChange,
  disabled = false,
  labelledBy,
}: ColorPickerProps) {
  const { t } = useTranslation('categories');

  const buttonsRef = useRef<(HTMLButtonElement | null)[]>([]);

  const normalizedValue = value.toLowerCase();
  const isPaletteColor = CATEGORY_COLORS.some(
    (color) => color.toLowerCase() === normalizedValue,
  );
  const isValidHexColor = /^#[0-9A-Fa-f]{6}$/.test(value);

  // Keep valid custom colors representable so an edit doesn't silently
  // display no selected option when the stored color isn't in the palette.
  const colors: readonly string[] =
    !isPaletteColor && isValidHexColor
      ? [value, ...CATEGORY_COLORS]
      : CATEGORY_COLORS;

  const total = colors.length;
  const selectedIndex = colors.findIndex(
    (color) => color.toLowerCase() === normalizedValue,
  );
  const focusableIndex = selectedIndex === -1 ? 0 : selectedIndex;

  const handleKeyDown = (
    event: KeyboardEvent<HTMLButtonElement>,
    index: number,
  ) => {
    let nextIndex: number | null = null;

    switch (event.key) {
      case 'ArrowRight':
      case 'ArrowDown':
        nextIndex = (index + 1) % total;
        break;

      case 'ArrowLeft':
      case 'ArrowUp':
        nextIndex = (index - 1 + total) % total;
        break;

      case 'Home':
        nextIndex = 0;
        break;

      case 'End':
        nextIndex = total - 1;
        break;

      default:
        return;
    }

    event.preventDefault();
    buttonsRef.current[nextIndex]?.focus();
    onChange(colors[nextIndex]);
  };

  return (
    <div
      className="grid grid-cols-6 gap-2 p-1 sm:grid-cols-8"
      role="radiogroup"
      aria-labelledby={labelledBy}
      aria-label={
        labelledBy ? undefined : t('categories:color')
      }
    >
      {colors.map((color, index) => {
        const isSelected =
          color.toLowerCase() === normalizedValue;

        return (
          <button
            key={color}
            ref={(element) => {
              buttonsRef.current[index] = element;
            }}
            type="button"
            role="radio"
            aria-checked={isSelected}
            aria-label={color}
            tabIndex={index === focusableIndex ? 0 : -1}
            disabled={disabled}
            onClick={() => onChange(color)}
            onKeyDown={(event) => handleKeyDown(event, index)}
            className={cn(
              'size-7 cursor-pointer rounded-md border transition-all',
              'border-white/10 hover:border-white/30',
              isSelected
                ? 'scale-110 border-primary ring-2 ring-primary/30 hover:border-primary'
                : 'hover:scale-105',
              disabled &&
                'cursor-not-allowed opacity-40 hover:scale-100',
            )}
            style={{ backgroundColor: color }}
          />
        );
      })}
    </div>
  );
}

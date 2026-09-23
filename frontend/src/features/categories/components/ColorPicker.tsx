import { cn } from '@/lib/utils';

import { CATEGORY_COLORS } from '../constants';

interface ColorPickerProps {
  value: string;
  onChange: (color: string) => void;
  disabled?: boolean;
}

export function ColorPicker({
  value,
  onChange,
  disabled = false,
}: ColorPickerProps) {
  return (
    <div
      className="grid grid-cols-6 gap-2 p-1 sm:grid-cols-8"
      role="radiogroup"
      aria-label="Seleccionar color"
    >
      {CATEGORY_COLORS.map((color) => {
        const isSelected = value === color;

        return (
          <button
            key={color}
            type="button"
            role="radio"
            aria-checked={isSelected}
            aria-label={color}
            disabled={disabled}
            onClick={() => onChange(color)}
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
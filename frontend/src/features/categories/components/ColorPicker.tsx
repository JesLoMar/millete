import { cn } from "@/lib/utils"
import { CATEGORY_COLORS } from "../constants"

interface ColorPickerProps {
  value: string
  onChange: (color: string) => void
  disabled?: boolean
}

export function ColorPicker({ value, onChange, disabled = false }: ColorPickerProps) {
  return (
    <div className="grid grid-cols-8 gap-2">
      {CATEGORY_COLORS.map((c) => {
        const isSelected = value === c

        return (
          <button
            key={c}
            type="button"
            disabled={disabled}
            onClick={() => onChange(c)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault()
                onChange(c)
              }
            }}
            aria-label={`Color ${c}`}
            aria-pressed={isSelected}
            className={cn(
              "size-7 rounded-md border transition-all cursor-pointer",
              "border-white/10 hover:border-white/30",
              isSelected
                ? "border-primary scale-110 ring-2 ring-primary/30 hover:border-primary"
                : "hover:scale-105",
              disabled && "opacity-40 cursor-not-allowed hover:scale-100"
            )}
            style={{ backgroundColor: c }}
          />
        )
      })}
    </div>
  )
}
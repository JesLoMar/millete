import { useRef, useLayoutEffect, useEffect, useState, useCallback } from "react"
import { useTranslation } from "react-i18next"
import { m } from "framer-motion"
import { cn } from "@/lib/utils"

export type PeriodFilter = "week" | "month" | "year"

interface PeriodSelectorProps {
  period: PeriodFilter
  onPeriodChange: (period: PeriodFilter) => void
  className?: string
}

const OPTIONS = [
  { value: "week" as PeriodFilter, labelKey: "header.period.week" as const },
  { value: "month" as PeriodFilter, labelKey: "header.period.month" as const },
  { value: "year" as PeriodFilter, labelKey: "header.period.year" as const },
]

export function PeriodSelector({ period, onPeriodChange, className }: PeriodSelectorProps) {
  const { t } = useTranslation(['dashboard'])
  const containerRef = useRef<HTMLFieldSetElement>(null)
  const buttonRefs = useRef<(HTMLButtonElement | null)[]>([])
  const [sliderStyle, setSliderStyle] = useState({ left: 0, width: 0 })

  const activeIndex = OPTIONS.findIndex((o) => o.value === period)

  const measureSlider = useCallback(() => {
    const container = containerRef.current
    const activeBtn = buttonRefs.current[activeIndex]
    if (!container || !activeBtn) return

    const containerRect = container.getBoundingClientRect()
    const btnRect = activeBtn.getBoundingClientRect()

    setSliderStyle({
      left: btnRect.left - containerRect.left,
      width: btnRect.width,
    })
  }, [activeIndex])

  useLayoutEffect(() => {
    measureSlider()
  }, [measureSlider])

  const measureRef = useRef(measureSlider)
  useLayoutEffect(() => {
    measureRef.current = measureSlider
  })

  useEffect(() => {
    const listener = () => measureRef.current()
    window.addEventListener("resize", listener)
    return () => window.removeEventListener("resize", listener)
  }, [])

  return (
    <fieldset
      ref={containerRef}
      className={cn(
        "relative flex sm:inline-flex p-1 bg-secondary/40 backdrop-blur-md rounded-xl border border-border/40 w-full sm:w-auto min-w-0",
        className
      )}
      aria-label={t('header.period.ariaLabel')}
    >
      {}
      <m.div
        className="absolute top-1 bottom-1 rounded-lg bg-primary shadow-sm z-0"
        animate={{
          left: sliderStyle.left,
          width: sliderStyle.width,
        }}
        transition={{
          type: "spring",
          stiffness: 400,
          damping: 30,
        }}
        style={{
          left: sliderStyle.left,
          width: sliderStyle.width,
        }}
      />

      {OPTIONS.map((option, index) => {
        const isActive = period === option.value
        return (
          <button
            key={option.value}
            ref={(el) => { buttonRefs.current[index] = el }}
            onClick={() => onPeriodChange(option.value)}
            type="button"
            className={cn(
              "relative z-10 flex-1 sm:flex-none px-2 sm:px-5 py-2",
              "text-xs sm:text-sm font-bold uppercase tracking-wider",
              "rounded-lg transition-colors duration-200 cursor-pointer min-w-17.5 sm:min-w-21.25 h-10 flex items-center justify-center select-none",
              "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-1",
              isActive
                ? "text-primary-foreground font-extrabold"
                : "text-muted-foreground hover:text-foreground hover:bg-accent/40"
            )}
            aria-current={isActive ? "true" : undefined}
          >
            <span className="truncate w-full block text-center">
              {t(option.labelKey)}
            </span>
          </button>
        )
      })}
    </fieldset>
  )
}

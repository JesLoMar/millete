import { cn } from "@/lib/utils"

interface SpinnerProps {
  className?: string
  size?: number
}

export function Spinner({ className, size = 64 }: SpinnerProps) {
  return (
    <img
      src="/web-app-icon.webp"
      alt=""
      className={cn("animate-spin", className)}
      style={{ width: size, height: size }}
      aria-hidden="true"
    />
  )
}

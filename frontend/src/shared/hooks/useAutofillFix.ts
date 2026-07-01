import { useEffect } from "react"

export function useAutofillFix() {
  useEffect(() => {
    const fix = () => {
      document.querySelectorAll("input, textarea, select").forEach((el) => {
        const input = el as HTMLInputElement
        const style = window.getComputedStyle(input)
        const bg = style.backgroundColor

        if (bg.includes("232") && bg.includes("240")) {
          input.style.setProperty("background-color", "#f0e0c0", "important")
          input.style.setProperty("-webkit-text-fill-color", "#3d2b1f", "important")
          input.style.setProperty(
            "-webkit-box-shadow",
            "0 0 0px 1000px #f0e0c0 inset",
            "important"
          )
        }
      })
    }

    fix()
    const t1 = setTimeout(fix, 100)
    const t2 = setTimeout(fix, 500)
    const t3 = setTimeout(fix, 1000)

    return () => {
      clearTimeout(t1)
      clearTimeout(t2)
      clearTimeout(t3)
    }
  }, [])
}

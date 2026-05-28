
export function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  return d.toLocaleDateString("es-ES", { day: "2-digit", month: "short", year: "numeric" })
}
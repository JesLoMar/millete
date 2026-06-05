export function BackgroundDecoration() {
  return (
    <div className="fixed inset-0 pointer-events-none -z-10" aria-hidden="true">
      <div className="absolute top-0 left-0 size-[50%] bg-primary/5 rounded-full blur-[120px] -translate-x-1/4 -translate-y-1/4" />
      <div className="absolute bottom-0 right-0 size-[40%] bg-accent/5 rounded-full blur-[100px] translate-x-1/4 translate-y-1/4" />
    </div>
  )
}
export function BackgroundDecoration() {
  return (
    <div className="fixed inset-0 pointer-events-none overflow-hidden -z-10">
      <div className="absolute top-[-10%] left-[-10%] size-[40%] bg-primary/5 rounded-full blur-[120px]" />
      <div className="absolute bottom-[-10%] right-[-10%] size-[30%] bg-accent/5 rounded-full blur-[100px]" />
    </div>
  )
}
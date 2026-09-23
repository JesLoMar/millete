export function BackgroundDecoration() {
  return (
    <div
      className="pointer-events-none fixed inset-0 -z-10"
      aria-hidden="true"
    >
      <div className="absolute left-0 top-0 size-[50%] -translate-x-1/4 -translate-y-1/4 rounded-full bg-primary/5 blur-[120px]" />

      <div className="absolute bottom-0 right-0 size-[40%] translate-x-1/4 translate-y-1/4 rounded-full bg-accent/5 blur-[100px]" />
    </div>
  );
}
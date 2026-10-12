import './BackgroundDecoration.css';

export function BackgroundDecoration() {
  return (
    <div
      className="auth-decoration pointer-events-none fixed inset-0"
      aria-hidden="true"
    >
      <span className="auth-coin auth-coin--left">€</span>
      <span className="auth-coin auth-coin--right">₿</span>
    </div>
  );
}

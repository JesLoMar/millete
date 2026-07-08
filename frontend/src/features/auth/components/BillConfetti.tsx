import { useRef } from "react";
import { useBillConfetti, type UseBillConfettiOptions } from "../hooks/useBillConfetti";

export function BillConfetti(props: UseBillConfettiOptions = {}) {
  const containerRef = useRef<HTMLDivElement>(null);
  useBillConfetti(containerRef, props);

  return <div ref={containerRef} className="bill-confetti__container" aria-hidden="true" />;
}

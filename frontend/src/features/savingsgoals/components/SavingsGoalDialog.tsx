import { useState, useRef } from "react";
import { Plus, Loader2, PiggyBank } from "lucide-react";
import { Button } from "@/shared/components/core/button";
import { Input } from "@/shared/components/core/input";
import { Label } from "@/shared/components/core/label";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogTrigger,
} from "@/shared/components/core/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/shared/components/core/select";
import { useCreateSavingsGoal } from "../hooks/useSavingsGoals";
import { notify } from "@/shared/utils/notifications/notify";
import type { ApiError } from "@/shared/types/api";

const PRIORITIES = [
  { value: "LOW", label: "Baja" },
  { value: "MEDIUM", label: "Media" },
  { value: "HIGH", label: "Alta" },
] as const;

export function SavingsGoalDialog() {
  const { mutateAsync: createGoal, isPending: isCreating } = useCreateSavingsGoal();
  const [open, setOpen] = useState(false);
  const [name, setName] = useState("");
  const [targetAmount, setTargetAmount] = useState("");
  const [priority, setPriority] = useState<"LOW" | "MEDIUM" | "HIGH">("MEDIUM");
  const [deadline, setDeadline] = useState("");
  const [link, setLink] = useState("");
  const inputRef = useRef<HTMLInputElement>(null);

  const resetForm = () => {
    setName("");
    setTargetAmount("");
    setPriority("MEDIUM");
    setDeadline("");
    setLink("");
  };

  const handleOpenChange = (isOpen: boolean) => {
    setOpen(isOpen);
    if (!isOpen) resetForm();
  };

  const handleSave = async () => {
    if (!name.trim() || !targetAmount) return;

    try {
      await createGoal({
        name: name.trim(),
        targetAmount: Number(targetAmount),
        priority,
        deadline: deadline || undefined,
        link: link.trim() || undefined,
      });
      setOpen(false);
      resetForm();
    } catch (err) {
      const apiError = err as ApiError;
      const message =
        apiError?.response?.data?.message || "Error al crear la meta de ahorro";
      notify.error(message);
    }
  };

  const isValid = name.trim() && targetAmount && Number(targetAmount) > 0;

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        <Button className="gap-2 bg-primary hover:bg-primary/90 font-semibold h-9 px-4">
          <Plus size={16} />
          Nueva meta
        </Button>
      </DialogTrigger>

      <DialogContent
        className="bg-card border-border sm:max-w-112.5"
        onOpenAutoFocus={(e) => {
          e.preventDefault();
          inputRef.current?.focus();
        }}
      >
        <div className="max-h-[85dvh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-xl font-semibold flex items-center gap-2">
              <PiggyBank className="text-primary size-5" />
              Nueva meta de ahorro
            </DialogTitle>
          </DialogHeader>

          <div className="space-y-4 py-2 sm:py-4">
            <div className="space-y-2">
              <Label className="text-sm font-semibold">Nombre</Label>
              <Input
                ref={inputRef}
                placeholder="Ej: Fondo de emergencia"
                value={name}
                onChange={(e) => setName(e.target.value)}
                disabled={isCreating}
                className="bg-background border-border"
              />
            </div>

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label className="text-sm font-semibold">Cantidad objetivo (€)</Label>
                <Input
                  type="number"
                  placeholder="0.00"
                  value={targetAmount}
                  onChange={(e) => setTargetAmount(e.target.value)}
                  disabled={isCreating}
                  className="bg-background border-border"
                  min="0.01"
                  step="0.01"
                />
              </div>
              <div className="space-y-2">
                <Label className="text-sm font-semibold">Prioridad</Label>
                <Select value={priority} onValueChange={(v) => setPriority(v as typeof priority)}>
                  <SelectTrigger className="bg-background border-border">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent className="bg-card border-border">
                    {PRIORITIES.map((p) => (
                      <SelectItem key={p.value} value={p.value}>
                        {p.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="space-y-2">
              <Label className="text-sm font-semibold">Fecha límite (opcional)</Label>
              <Input
                type="date"
                value={deadline}
                onChange={(e) => setDeadline(e.target.value)}
                disabled={isCreating}
                className="bg-background border-border"
              />
            </div>

            <div className="space-y-2">
              <Label className="text-sm font-semibold">Enlace (opcional)</Label>
              <Input
                placeholder="https://..."
                value={link}
                onChange={(e) => setLink(e.target.value)}
                disabled={isCreating}
                className="bg-background border-border"
              />
            </div>
          </div>

          <DialogFooter className="gap-2 pt-2 pb-1 sticky bottom-0 bg-card">
            <Button
              variant="outline"
              onClick={() => setOpen(false)}
              disabled={isCreating}
              className="border-border"
            >
              Cancelar
            </Button>
            <Button
              onClick={handleSave}
              disabled={isCreating || !isValid}
              className="bg-primary hover:bg-primary/90 px-6"
            >
              {isCreating ? <Loader2 size={16} className="animate-spin" /> : "Crear meta"}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  );
}
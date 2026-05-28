import { useTranslation } from "react-i18next"
import { AlertTriangle } from "lucide-react"
import { Button } from "@/shared/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/shared/components/ui/dialog"

interface ConfirmDeletionDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  itemName: string
  onConfirm: () => void
  isDeleting?: boolean
}

export function ConfirmDeletionDialog({
  open,
  onOpenChange,
  itemName,
  onConfirm,
  isDeleting = false,
}: ConfirmDeletionDialogProps) {
  const { t } = useTranslation()

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="bg-card border-border sm:max-w-106.25">
        <DialogHeader>
          <div className="mx-auto mb-4 bg-destructive/10 p-3 rounded-full w-fit">
            <AlertTriangle className="size-8 text-destructive" />
          </div>
          <DialogTitle className="text-xl font-semibold text-center">
            {t("categories.deleteTitle")}
          </DialogTitle>
          <DialogDescription className="text-center pt-2">
            {t("categories.deleteConfirmation", { name: itemName })}
          </DialogDescription>
        </DialogHeader>

        <DialogFooter className="gap-2 sm:justify-center">
          <Button
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={isDeleting}
            className="border-border"
          >
            {t("common.cancel")}
          </Button>
          <Button
            onClick={onConfirm}
            disabled={isDeleting}
            variant="destructive"
            className="gap-2"
          >
            {isDeleting ? t("common.deleting") : t("categories.delete")}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
import { useState } from "react"
import { useTranslation } from "react-i18next"
import { Button } from "@/shared/components/ui/button"
import { Input } from "@/shared/components/ui/input"
import { Label } from "@/shared/components/ui/label"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/shared/components/ui/dialog"

interface InviteMemberDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onInvite: (email: string) => void
}

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function InviteMemberDialog({ open, onOpenChange, onInvite }: InviteMemberDialogProps) {
  const { t } = useTranslation()
  const [email, setEmail] = useState("")
  const [error, setError] = useState<string | null>(null)

  const isValidEmail = EMAIL_REGEX.test(email.trim())

  const handleInvite = () => {
    if (!isValidEmail) {
      setError(t("family.invalidEmail"))
      return
    }
    onInvite(email.trim())
    setEmail("")
    setError(null)
    onOpenChange(false)
  }

  const handleOpenChange = (isOpen: boolean) => {
    if (!isOpen) {
      setEmail("")
      setError(null)
    }
    onOpenChange(isOpen)
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="bg-card border-border sm:max-w-106.25">
        <DialogHeader>
          <DialogTitle>{t("family.inviteTitle")}</DialogTitle>
          <DialogDescription>
            {t("family.inviteDesc")}
          </DialogDescription>
        </DialogHeader>
        <div className="py-4 space-y-4">
          <div className="space-y-2">
            <Label htmlFor="email">{t("family.email")}</Label>
            <Input
              id="email"
              type="email"
              placeholder={t("family.emailPlaceholder")}
              value={email}
              onChange={(e) => {
                setEmail(e.target.value)
                setError(null)
              }}
              className="bg-background border-border"
            />
            {error && (
              <p className="text-red-400 text-xs">{error}</p>
            )}
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => handleOpenChange(false)} className="border-border">
            {t("common.cancel")}
          </Button>
          <Button onClick={handleInvite} disabled={!email.trim()}>
            {t("family.sendInvitation")}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
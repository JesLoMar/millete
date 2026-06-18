import { useState } from "react"
import { useTranslation } from "react-i18next"
import { Button } from "@/shared/components/core/button"
import { Input } from "@/shared/components/core/input"
import { Label } from "@/shared/components/core/label"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/shared/components/core/dialog"

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
      setError(t('groupGoals:invalidEmail'))
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
          <DialogTitle>{t('groupGoals:inviteTitle')}</DialogTitle>
          <DialogDescription>
            {t('groupGoals:inviteDesc')}
          </DialogDescription>
        </DialogHeader>
        <div className="py-4 space-y-4">
          <div className="space-y-2">
            <Label htmlFor="email">{t('groupGoals:email')}</Label>
            <Input
              id="email"
              type="email"
              placeholder={t('groupGoals:emailPlaceholder')}
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
            {t('common:actions.cancel')}
          </Button>
          <Button onClick={handleInvite} disabled={!email.trim()}>
            {t('groupGoals:sendInvitation')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

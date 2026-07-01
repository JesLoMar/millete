import { useState } from "react"
import { useTranslation } from "react-i18next"
import { Button } from "@/shared/components/core/button"
import { Spinner } from "@/shared/components/Spinner"
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
  onInvite: (identifier: string) => Promise<void>
  isInviting?: boolean
}

export function InviteMemberDialog({ open, onOpenChange, onInvite, isInviting = false }: InviteMemberDialogProps) {
  const { t } = useTranslation()
  const [identifier, setIdentifier] = useState("")
  const [error, setError] = useState<string | null>(null)

  const handleInvite = async () => {
    const trimmed = identifier.trim()
    if (!trimmed) {
      setError(t('groupGoals:invalidIdentifier'))
      return
    }
    await onInvite(trimmed)
    setIdentifier("")
    setError(null)
    onOpenChange(false)
  }

  const handleOpenChange = (isOpen: boolean) => {
    if (!isOpen) {
      setIdentifier("")
      setError(null)
    }
    onOpenChange(isOpen)
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="bg-card border-border sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{t('groupGoals:inviteTitle')}</DialogTitle>
          <DialogDescription>
            {t('groupGoals:inviteDesc')}
          </DialogDescription>
        </DialogHeader>
        <div className="py-4 space-y-4">
          <div className="space-y-2">
            <Label htmlFor="identifier">{t('groupGoals:identifier')}</Label>
            <Input
              id="identifier"
              type="text"
              placeholder={t('groupGoals:identifierPlaceholder')}
              value={identifier}
              onChange={(e) => {
                setIdentifier(e.target.value)
                setError(null)
              }}
              className="bg-background border-border"
            />
            {error && (
              <p className="text-destructive text-xs">{error}</p>
            )}
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => handleOpenChange(false)} className="border-border">
            {t('common:actions.cancel')}
          </Button>
          <Button onClick={handleInvite} disabled={!identifier.trim() || isInviting}>
            {isInviting ? <Spinner size={20} /> : t('groupGoals:sendInvitation')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

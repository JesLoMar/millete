import { useTranslation } from "react-i18next"
import { Card, CardContent, CardHeader, CardTitle } from "@/shared/components/core/card"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/shared/components/core/select"
import { AlertCircle, CheckCircle2, Loader2 } from "lucide-react"

interface DistributionCardProps {
  distributionMode: string
  isAdmin: boolean
  isCustomMode: boolean
  isPercentageInvalid: boolean
  onModeChange: (mode: string) => void
  isChangingMode?: boolean
}

export function DistributionCard({
  distributionMode,
  isAdmin,
  isCustomMode,
  isPercentageInvalid,
  onModeChange,
  isChangingMode = false,
}: DistributionCardProps) {
  const { t } = useTranslation()

  return (
    <Card className="border-subtle bg-primary/5 relative overflow-hidden">
      <CardHeader className="pb-2">
        <CardTitle className="text-base flex items-center justify-between gap-2">
          <span className="truncate">{t('groupGoals:distributionMode')}</span>
          {isChangingMode && (
            <Loader2 size={14} className="animate-spin text-muted-foreground shrink-0" aria-hidden="true" />
          )}
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        {isAdmin ? (
          <Select value={distributionMode} onValueChange={onModeChange} disabled={isChangingMode}>
            <SelectTrigger className="bg-background border-border w-full">
              <SelectValue />
            </SelectTrigger>
            <SelectContent className="bg-card border-border">
              <SelectItem value="EQUITATIVE">{t('groupGoals:modes.equitative')}</SelectItem>
              <SelectItem value="PROPORTIONAL">{t('groupGoals:modes.proportional')}</SelectItem>
              <SelectItem value="CUSTOM">{t('groupGoals:modes.custom')}</SelectItem>
            </SelectContent>
          </Select>
        ) : (
          <p className="text-sm font-medium truncate">
            {t(`groupGoals:modes.${distributionMode.toLowerCase()}`)}
          </p>
        )}
        <p className="text-xs text-muted-foreground leading-relaxed">
          {t(`groupGoals:modes.${distributionMode.toLowerCase()}Desc`)}
        </p>

        {isCustomMode && (
          <div className={`flex items-start gap-2 p-2 sm:p-3 rounded-lg border text-xs transition-colors ${
            isPercentageInvalid 
              ? "bg-amber-500/10 border-amber-500/20 text-amber-400" 
              : "bg-emerald-500/10 border-emerald-500/20 text-emerald-400"
          }`}>
            {isPercentageInvalid ? (
              <AlertCircle className="size-3.5 sm:size-4 shrink-0 mt-0.5" aria-hidden="true" />
            ) : (
              <CheckCircle2 className="size-3.5 sm:size-4 shrink-0 mt-0.5" aria-hidden="true" />
            )}
            <span>{isPercentageInvalid ? t('groupGoals:customPercentageHint') : t('groupGoals:customPercentageOk')}</span>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
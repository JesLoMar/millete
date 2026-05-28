import { useTranslation } from "react-i18next"
import { Card, CardContent, CardHeader, CardTitle } from "@/shared/components/ui/card"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/shared/components/ui/select"
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
    <Card className="col-span-12 lg:col-span-3 border-subtle bg-primary/5 relative overflow-hidden">
      <CardHeader className="pb-2">
        <CardTitle className="text-base flex items-center justify-between">
          {t("family.distributionMode")}
          {isChangingMode && <Loader2 size={14} className="animate-spin text-muted-foreground" />}
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        {isAdmin ? (
          <Select value={distributionMode} onValueChange={onModeChange} disabled={isChangingMode}>
            <SelectTrigger className="bg-background border-border">
              <SelectValue />
            </SelectTrigger>
            <SelectContent className="bg-card border-border">
              <SelectItem value="EQUITATIVE">{t("family.modes.equitative")}</SelectItem>
              <SelectItem value="PROPORTIONAL">{t("family.modes.proportional")}</SelectItem>
              <SelectItem value="CUSTOM">{t("family.modes.custom")}</SelectItem>
            </SelectContent>
          </Select>
        ) : (
          <p className="text-sm font-medium">
            {t(`family.modes.${distributionMode.toLowerCase()}`)}
          </p>
        )}
        <p className="text-xs text-muted-foreground leading-relaxed">
          {t(`family.modes.${distributionMode.toLowerCase()}Desc`)}
        </p>

        {isCustomMode && (
          <div className={`flex items-start gap-2 p-2 rounded-lg border text-xs transition-colors ${
            isPercentageInvalid 
              ? "bg-amber-500/10 border-amber-500/20 text-amber-400" 
              : "bg-emerald-500/10 border-emerald-500/20 text-emerald-400"
          }`}>
            {isPercentageInvalid ? (
              <AlertCircle className="size-4 shrink-0 mt-0.5" />
            ) : (
              <CheckCircle2 className="size-4 shrink-0 mt-0.5" />
            )}
            <span>
              {isPercentageInvalid ? t("family.customPercentageHint") : t("family.customPercentageOk")}
            </span>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
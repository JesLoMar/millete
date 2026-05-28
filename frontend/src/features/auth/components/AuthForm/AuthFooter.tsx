import { useTranslation } from "react-i18next"
import { Badge } from "@/shared/components/ui/badge"

export function AuthFooter() {
  const { t } = useTranslation()

  return (
    <div className="flex items-center justify-between border-t border-border/30 pt-12 mt-auto w-full">
      <div className="flex gap-6 items-center">
        <Badge variant="outline" className="font-mono text-[14px] px-4 py-1.5 border-border/50 text-muted-foreground/60 bg-secondary/10 whitespace-nowrap">
          {t("auth.footer.webVersion")}
        </Badge>
        <Badge variant="outline" className="font-mono text-[14px] px-4 py-1.5 border-border/50 text-muted-foreground/60 bg-secondary/10 whitespace-nowrap">
          {t("auth.footer.apiVersion")}
        </Badge>
      </div>
      <span className="text-[15px] text-muted-foreground/40 font-medium whitespace-nowrap">
        {t("auth.footer.copyright")}
      </span>
    </div>
  )
}
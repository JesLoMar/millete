import { useTranslation } from "react-i18next"
import { Loader2 } from "lucide-react"
import { Label } from "@/shared/components/ui/label"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/shared/components/ui/select"
import { useCategories } from "@/shared/hooks/useCategories"

interface CategorySelectProps {
  value: string
  onValueChange: (value: string) => void
  className?: string
}

export function CategorySelect({ value, onValueChange, className }: CategorySelectProps) {
  const { t } = useTranslation()
  const { data: categories, isLoading } = useCategories()

  return (
    <div className={`space-y-2 ${className || ""}`}>
      <Label className="text-sm font-semibold">
        {t("transactions.category")}
      </Label>
      <Select value={value} onValueChange={onValueChange} disabled={isLoading}>
        <SelectTrigger className="bg-background border-border">
          {isLoading ? (
            <div className="flex items-center gap-2">
              <Loader2 size={14} className="animate-spin" />
              <span className="text-muted-foreground">{t("common.loading")}</span>
            </div>
          ) : (
            <SelectValue placeholder={t("transactions.selectCategory")} />
          )}
        </SelectTrigger>
        <SelectContent className="bg-card border-border">
          {categories?.length === 0 && (
            <div className="px-2 py-4 text-sm text-muted-foreground text-center">
              {t("categories.empty")}
            </div>
          )}
          {categories?.map((cat) => (
            <SelectItem key={cat.id} value={cat.id}>
              {cat.name}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  )
}
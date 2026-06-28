import { useState } from "react"
import { useTranslation } from "react-i18next"
import { ArrowLeft } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/shared/components/core/dialog"
import { notify } from "@/shared/utils/notifications/notify"
import { ExportCard } from "./ExportCard.tsx"
import { useExport } from "../hooks/useExport"
import {
  EXPORT_FORMATS,
  EXPORT_ENTITY_TYPES,
  EXPORT_PERIOD_OPTIONS,
  type ExportFormat,
} from "../constants"

interface ExportModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function ExportModal({ open, onOpenChange }: ExportModalProps) {
  const { t } = useTranslation(["dashboard", "nav", "common"])
  const { performExport, isExporting } = useExport()
  const [step, setStep] = useState<"format" | "config">("format")
  const [selectedFormat, setSelectedFormat] = useState<ExportFormat>("json")

  const resetState = () => {
    setStep("format")
    setSelectedFormat("json")
  }

  const handleOpenChange = (isOpen: boolean) => {
    if (!isOpen) resetState()
    onOpenChange(isOpen)
  }

  const handleFormatSelect = (format: ExportFormat) => {
    setSelectedFormat(format)
    if (!EXPORT_FORMATS.find(f => f.id === format)?.needsConfig) {
      handleExport(format)
    } else {
      setStep("config")
    }
  }

  const handleConfigSelect = (value: string) => {
    handleExport(selectedFormat, value)
  }

  const handleExport = async (format: ExportFormat, configValue?: string) => {
    const success = await performExport(format, configValue)
    if (success) {
      notify.success(t("export.success"))
      onOpenChange(false)
      resetState()
    } else {
      notify.error(t("export.error"))
    }
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="bg-card border-border sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle className="text-xl font-semibold">
            {step === "format" ? t("export.title") : t("export.configTitle")}
          </DialogTitle>
          <DialogDescription className="sr-only">
            Opciones para exportar tus datos financieros en múltiples formatos.
          </DialogDescription>
        </DialogHeader>

        {step === "format" && (
          <div className="grid grid-cols-2 gap-3 sm:gap-4 py-4">
            {EXPORT_FORMATS.map((fmt) => (
              <ExportCard
                key={fmt.id}
                icon={fmt.icon}
                label={t(fmt.labelKey)}
                description={t(fmt.descKey)}
                color={fmt.color}
                onClick={() => handleFormatSelect(fmt.id)}
                disabled={isExporting}
              />
            ))}
          </div>
        )}

        {step === "config" && (
          <div className="space-y-4 py-4">
            <button
              type="button"
              onClick={() => setStep("format")}
              className="flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground transition-colors"
            >
              <ArrowLeft className="size-4" />
              {t("common:actions.back")}
            </button>

            {selectedFormat === "csv" && (
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 sm:gap-4">
                {EXPORT_ENTITY_TYPES.map((entity) => (
                  <ExportCard
                    key={entity.value}
                    icon={EXPORT_FORMATS[2].icon}
                    label={t(`common:${entity.labelKey}`)}
                    color="bg-emerald-500/10 text-emerald-500 group-hover:bg-emerald-500 group-hover:text-white"
                    onClick={() => handleConfigSelect(entity.value)}
                    disabled={isExporting}
                  />
                ))}
              </div>
            )}

            {selectedFormat === "pdf" && (
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 sm:gap-4">
                {EXPORT_PERIOD_OPTIONS.map((period) => (
                  <ExportCard
                    key={period.value}
                    icon={EXPORT_FORMATS[3].icon}
                    label={t(period.labelKey)}
                    color="bg-rose-500/10 text-rose-500 group-hover:bg-rose-500 group-hover:text-white"
                    onClick={() => handleConfigSelect(period.value)}
                    disabled={isExporting}
                  />
                ))}
              </div>
            )}
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}
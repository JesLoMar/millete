import { useState } from "react"
import { apiClient } from "@/shared/api/axiosClient"
import type { ExportFormat } from "../constants"

const FILE_BRAND = "millete"

const sanitizeFilenamePart = (value: string): string =>
  value.replace(/[^a-zA-Z0-9._-]+/g, "_").replace(/^_+|_+$/g, "").slice(0, 50)

export function useExport() {
  const [isExporting, setIsExporting] = useState(false)

  const performExport = async (format: ExportFormat, configValue?: string) => {
    setIsExporting(true)
    try {
      let response: BlobPart
      let filename = ""
      switch (format) {
        case "json":
          response = (await apiClient.get("/data/export", { responseType: "blob" })).data
          filename = `${FILE_BRAND}_export.json`
          break
        case "zip":
          response = (await apiClient.get("/data/export/zip", { responseType: "blob" })).data
          filename = `${FILE_BRAND}_export.zip`
          break
        case "csv": {
          if (!configValue) return
          const safe = sanitizeFilenamePart(configValue)
          response = (await apiClient.get(`/data/export/csv/${encodeURIComponent(configValue)}`, { responseType: "blob" })).data
          filename = `${FILE_BRAND}_export_${safe}.csv`
          break
        }
        case "pdf": {
          if (!configValue) return
          const safe = sanitizeFilenamePart(configValue)
          response = (await apiClient.get(`/data/export/pdf?${new URLSearchParams({ period: configValue })}`, { responseType: "blob" })).data
          filename = `${FILE_BRAND}_financial_data_${safe}.pdf`
          break
        }
      }
      const url = window.URL.createObjectURL(new Blob([response]))
      const link = document.createElement("a")
      link.href = url
      link.download = filename
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      return true
    } catch {
      return false
    } finally {
      setIsExporting(false)
    }
  }

  return { performExport, isExporting }
}
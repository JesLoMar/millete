import { useState } from "react"
import { apiClient } from "@/shared/api/axiosClient"
import type { ExportFormat } from "../constants"

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
          filename = "familybudget_export.json"
          break
        case "zip":
          response = (await apiClient.get("/data/export/zip", { responseType: "blob" })).data
          filename = "familybudget_export.zip"
          break
        case "csv":
          if (!configValue) return
          response = (await apiClient.get(`/data/export/csv/${configValue}`, { responseType: "blob" })).data
          filename = `familybudget_${configValue}.csv`
          break
        case "pdf":
          if (!configValue) return
          response = (await apiClient.get(`/data/export/pdf?period=${configValue}`, { responseType: "blob" })).data
          filename = `millete_financial_data_${configValue}.pdf`
          break
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
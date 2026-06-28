import { useState, useCallback, useRef } from "react"
import { useTranslation } from "react-i18next"
import { Upload, X, FileJson, CheckCircle } from "lucide-react"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/shared/components/core/dialog"
import { Button } from "@/shared/components/core/button"

interface ImportModalProps {
  isOpen: boolean
  onClose: () => void
  onImport: (file: File) => void
}

export function ImportModal({ isOpen, onClose, onImport }: ImportModalProps) {
  const { t } = useTranslation(['dashboard', 'common'])
  const [dragOver, setDragOver] = useState(false)
  const [fileName, setFileName] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault()
    setDragOver(true)
  }, [])

  const handleDragLeave = useCallback(() => {
    setDragOver(false)
  }, [])

  const validateAndSetFile = useCallback(async (file: File) => {
    if (file.type !== "application/json" && !file.name.endsWith(".json")) {
      setError(t('dashboard:importModal.jsonOnly'))
      return
    }
    try {
      const text = await file.text()
      JSON.parse(text)
      setFileName(file.name)
      setSelectedFile(file)
      setError(null)
    } catch {
      setError(t('dashboard:importModal.invalidContent'))
    }
  }, [t])

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault()
    setDragOver(false)
    setError(null)
    const files = e.dataTransfer.files
    if (files && files.length > 0) {
      validateAndSetFile(files[0])
    }
  }, [validateAndSetFile])

  const handleFileChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setError(null)
    const files = e.target.files
    if (files && files.length > 0) {
      validateAndSetFile(files[0])
    }
  }, [validateAndSetFile])

  const triggerFileSelect = () => {
    fileInputRef.current?.click()
  }

  const removeFile = (e: React.MouseEvent<HTMLElement>) => {
    e.stopPropagation()
    setFileName(null)
    setSelectedFile(null)
    if (fileInputRef.current) fileInputRef.current.value = ""
  }

  const handleSubmit = () => {
    if (selectedFile) {
      onImport(selectedFile)
      setFileName(null)
      setSelectedFile(null)
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => { if (!open) onClose() }}>
      <DialogContent className="sm:max-w-120 bg-card border-border p-6 text-white rounded-2xl">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold tracking-tight">
            {t('dashboard:importModal.title')}
          </DialogTitle>
          <DialogDescription className="text-sm text-muted-foreground">
            {t('dashboard:importModal.description')}
          </DialogDescription>
        </DialogHeader>

        <div
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          className={`my-5 border-2 border-dashed rounded-xl flex flex-col items-center transition-all w-full ${
            dragOver 
              ? "border-primary bg-primary/5 scale-[0.99]" 
              : "border-border/60 hover:border-primary/50 hover:bg-secondary/10"
          }`}
        >
          <input type="file" ref={fileInputRef} onChange={handleFileChange} accept=".json,application/json" className="hidden" aria-label={t('dashboard:importModal.fileInput')} />
          <button
            type="button"
            onClick={triggerFileSelect}
            className="flex flex-col items-center justify-center gap-4 cursor-pointer w-full p-8"
          >
            {!fileName ? (
              <>
                <div className="p-3 bg-secondary/40 rounded-full text-muted-foreground">
                  <Upload className="size-6" />
                </div>
                <div className="text-center space-y-1">
                  <p className="text-sm font-medium">{t('dashboard:importModal.dropHere')}</p>
                  <p className="text-xs text-muted-foreground">{t('dashboard:importModal.onlyJson')}</p>
                </div>
              </>
            ) : (
              <div className="flex items-center gap-3 w-full bg-secondary/20 p-3 rounded-xl border border-border/40">
                <div className="p-2 bg-primary/10 rounded-lg text-primary">
                  <FileJson className="size-5" />
                </div>
                <p className="text-sm font-medium truncate flex-1 text-left">{fileName}</p>
              </div>
            )}
          </button>
          
          {fileName && (
            <button
              type="button"
              onClick={removeFile}
              className="flex items-center gap-1.5 text-sm text-muted-foreground hover:text-white transition-colors mt-2 mb-4"
              aria-label={t('common:actions.delete')}
            >
              <X className="size-4" />
              {t('common:actions.remove')}
            </button>
          )}
        </div>

        {error && (
          <p className="text-red-400 text-xs font-medium bg-red-500/10 p-2.5 rounded-lg border border-red-500/20 mb-4 animate-in fade-in-50">
            {error}
          </p>
        )}

        <div className="flex justify-end gap-3 pt-2">
          <Button variant="outline" onClick={onClose} className="border-border hover:bg-secondary text-white">
            {t('common:actions.cancel')}
          </Button>
          <Button onClick={handleSubmit} disabled={!selectedFile} className="bg-primary hover:bg-primary/90 text-white font-semibold rounded-xl px-5 transition-all">
            <CheckCircle className="mr-2 size-4" />
            {t('common:actions.accept')}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  )
}

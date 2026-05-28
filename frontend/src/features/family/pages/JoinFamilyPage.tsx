import { useState } from "react"
import { useSearchParams, useNavigate } from "react-router-dom"
import { useTranslation } from "react-i18next"
import { Button } from "@/shared/components/ui/button"
import { Card, CardContent } from "@/shared/components/ui/card"
import { apiClient } from "@/shared/api/axiosClient"
import { useAuth } from "@/features/auth/context/AuthContext"
import { notify } from "@/shared/utils/notifications/notify"
import { CheckCircle, XCircle, Loader2, Users } from "lucide-react"
import type { ApiError } from "@/shared/types/api"

export const JoinFamilyPage = () => {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { isLoading: authLoading } = useAuth()
  const token = searchParams.get("token")

  const [status, setStatus] = useState<"ready" | "accepting" | "success" | "error">("ready")
  const [message, setMessage] = useState("")

  if (authLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background p-4">
        <Loader2 className="size-12 text-primary animate-spin" />
      </div>
    )
  }

  if (!token) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background p-4">
        <Card className="max-w-md w-full border-subtle">
          <CardContent className="p-8 text-center space-y-6">
            <div className="bg-destructive/10 p-4 rounded-full w-fit mx-auto">
              <XCircle className="size-12 text-destructive" />
            </div>
            <h2 className="text-xl font-semibold">{t("family.error")}</h2>
            <p className="text-muted-foreground">{t("family.invalidToken")}</p>
            <Button variant="outline" onClick={() => navigate("/dashboard")}>
              {t("common.goToLogin")}
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  const handleAccept = async () => {
    setStatus("accepting")
    try {
      await apiClient.post(`/families/invitations/${token}/accept`)
      setStatus("success")
      const successMsg = t("family.invitationAccepted") || "¡Te has unido a la familia con éxito!"
      setMessage(successMsg)
      notify.success(successMsg)
    } catch (err) {
      const apiError = err as ApiError
      setStatus("error")
      const errorMsg = apiError.response?.data?.message || t("family.invitationError") || "Error al procesar la invitación"
      setMessage(errorMsg)
      notify.error(errorMsg)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <Card className="max-w-md w-full border-subtle">
        <CardContent className="p-8 text-center space-y-6">
          {status === "ready" && (
            <>
              <div className="bg-primary/10 p-4 rounded-full w-fit mx-auto">
                <Users className="size-12 text-primary" />
              </div>
              <h2 className="text-xl font-semibold">{t("family.invitationReceived")}</h2>
              <p className="text-muted-foreground">{t("family.invitationMessage")}</p>
              <div className="flex gap-3 justify-center">
                <Button onClick={handleAccept} className="gap-2">
                  <CheckCircle className="size-4" />
                  {t("common.accept")}
                </Button>
                <Button variant="outline" onClick={() => navigate("/dashboard")}>
                  <XCircle className="size-4" />
                  {t("common.reject")}
                </Button>
              </div>
            </>
          )}

          {status === "accepting" && (
            <div className="space-y-4 py-6">
              <Loader2 className="size-12 text-primary animate-spin mx-auto" />
              <p className="text-muted-foreground text-sm">{t("family.processingInvitation")}</p>
            </div>
          )}

          {status === "success" && (
            <>
              <div className="bg-emerald-500/10 p-4 rounded-full w-fit mx-auto">
                <CheckCircle className="size-12 text-emerald-500" />
              </div>
              <h2 className="text-xl font-semibold">{t("family.welcome")}</h2>
              <p className="text-muted-foreground">{message}</p>
              <Button onClick={() => navigate("/family")} className="gap-2">
                <Users className="size-4" />
                {t("family.goToFamily")}
              </Button>
            </>
          )}

          {status === "error" && (
            <>
              <div className="bg-destructive/10 p-4 rounded-full w-fit mx-auto">
                <XCircle className="size-12 text-destructive" />
              </div>
              <h2 className="text-xl font-semibold">{t("family.error")}</h2>
              <p className="text-muted-foreground">{message}</p>
              <Button variant="outline" onClick={() => navigate("/dashboard")}>
                {t("common.goToLogin")}
              </Button>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
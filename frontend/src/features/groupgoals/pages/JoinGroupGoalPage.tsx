import { useState } from "react"
import { useSearchParams, useNavigate } from "react-router-dom"
import { useTranslation } from "react-i18next"
import { Button } from "@/shared/components/core/button"
import { Card, CardContent } from "@/shared/components/core/card"
import { apiClient } from "@/shared/api/axiosClient"
import { useAuth } from "@/features/auth/context/AuthContext"
import { notify } from "@/shared/utils/notifications/notify"
import { CheckCircle, XCircle, Loader2, Users, UserX } from "lucide-react"
import { ROUTES } from "@/app/router/routes"
import type { ApiError } from "@/shared/types/api"

type JoinStatus = "ready" | "processing" | "accepted" | "rejected" | "error"

export const JoinGroupGoalPage = () => {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { isLoading: authLoading } = useAuth()
  const invitationId = searchParams.get("invitationId")
  const [status, setStatus] = useState<JoinStatus>("ready")
  const [message, setMessage] = useState("")

  if (authLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background p-4">
        <Loader2 className="size-12 text-primary animate-spin" />
      </div>
    )
  }

  if (!invitationId) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background p-4">
        <Card className="max-w-md w-full border">
          <CardContent className="p-8 text-center space-y-6">
            <div className="bg-destructive/10 p-4 rounded-full w-fit mx-auto">
              <XCircle className="size-12 text-destructive" />
            </div>
            <h2 className="text-xl font-semibold">{t('groupGoals:error')}</h2>
            <p className="text-muted-foreground">{t('groupGoals:invalidToken')}</p>
            <Button variant="outline" onClick={() => navigate(ROUTES.dashboard)}>
              {t('nav:goToDashboard')}
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  const handleAccept = async () => {
    setStatus("processing")
    try {
      await apiClient.post(`/goals/invitations/${invitationId}/accept`)
      setStatus("accepted")
      const successMsg = t('groupGoals:invitationAccepted')
      setMessage(successMsg)
      notify.success(successMsg)
    } catch (err) {
      const apiError = err as ApiError
      setStatus("error")
      const errorMsg = apiError.response?.data?.message || t('groupGoals:invitationError')
      setMessage(errorMsg)
      notify.error(errorMsg)
    }
  }

  const handleReject = async () => {
    setStatus("processing")
    try {
      await apiClient.post(`/goals/invitations/${invitationId}/reject`)
      setStatus("rejected")
      const rejectedMsg = t('groupGoals:invitationRejectedMessage')
      setMessage(rejectedMsg)
      notify.info(rejectedMsg)
    } catch (err) {
      const apiError = err as ApiError
      setStatus("error")
      const errorMsg = apiError.response?.data?.message || t('groupGoals:rejectError')
      setMessage(errorMsg)
      notify.error(errorMsg)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <Card className="max-w-md w-full border">
        <CardContent className="p-8 text-center space-y-6">
          {status === "ready" && (
            <>
              <div className="bg-primary/10 p-4 rounded-full w-fit mx-auto">
                <Users className="size-12 text-primary" />
              </div>
              <h2 className="text-xl font-semibold">{t('groupGoals:invitationReceived')}</h2>
              <p className="text-muted-foreground">{t('groupGoals:invitationMessage')}</p>
              <div className="flex gap-3 justify-center">
                <Button onClick={handleAccept} className="gap-2">
                  <CheckCircle className="size-4" />
                  {t('common:actions.accept')}
                </Button>
                <Button variant="outline" onClick={handleReject}>
                  <XCircle className="size-4" />
                  {t('common:actions.reject')}
                </Button>
              </div>
            </>
          )}
          {status === "processing" && (
            <div className="space-y-4 py-6">
              <Loader2 className="size-12 text-primary animate-spin mx-auto" />
              <p className="text-muted-foreground text-sm">{t('groupGoals:processingInvitation')}</p>
            </div>
          )}
          {status === "accepted" && (
            <>
              <div className="bg-primary/10 p-4 rounded-full w-fit mx-auto">
                <CheckCircle className="size-12 text-primary" />
              </div>
              <h2 className="text-xl font-semibold">{t('groupGoals:welcome')}</h2>
              <p className="text-muted-foreground">{message}</p>
              <Button onClick={() => navigate(ROUTES.groupGoals)} className="gap-2">
                <Users className="size-4" />
                {t('groupGoals:goToFamily')}
              </Button>
            </>
          )}
          {status === "rejected" && (
            <>
              <div className="bg-muted p-4 rounded-full w-fit mx-auto">
                <UserX className="size-12 text-muted-foreground" />
              </div>
              <h2 className="text-xl font-semibold">{t('groupGoals:invitationRejected')}</h2>
              <p className="text-muted-foreground">{message}</p>
              <Button variant="outline" onClick={() => navigate(ROUTES.dashboard)}>
                {t('nav:goToDashboard')}
              </Button>
            </>
          )}
          {status === "error" && (
            <>
              <div className="bg-destructive/10 p-4 rounded-full w-fit mx-auto">
                <XCircle className="size-12 text-destructive" />
              </div>
              <h2 className="text-xl font-semibold">{t('groupGoals:error')}</h2>
              <p className="text-muted-foreground">{message}</p>
              <Button variant="outline" onClick={() => navigate(ROUTES.dashboard)}>
                {t('nav:goToDashboard')}
              </Button>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
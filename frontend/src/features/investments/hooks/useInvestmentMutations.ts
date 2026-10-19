import { useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import { useTranslation } from 'react-i18next';
import { investmentsService } from '../services/investments.service';
import { notify } from '@/shared/utils/notifications/notify';
import {
  ALL_INVESTMENT_QUERY_PREFIXES,
  INVESTMENT_QUERY_KEYS,
} from '../constants';
import type {
  ConfigureTrackingRequest,
  CreateUserAssetRequest,
  EditActivityRequest,
  RegisterActivityRequest,
  RegisterUserAssetPriceRequest,
  UpdateUserAssetRequest,
} from '../types';

function getErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as
      | { message?: string; error?: string }
      | undefined;
    return data?.message ?? data?.error ?? fallback;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

export function useInvestmentMutations() {
  const queryClient = useQueryClient();
  const { t } = useTranslation('investments');

  const invalidateAll = async () => {
    await Promise.all(
      ALL_INVESTMENT_QUERY_PREFIXES.map((prefix) =>
        queryClient.invalidateQueries({ queryKey: [prefix] }),
      ),
    );
  };

  // ── Activities ────────────────────────────────────────────────────────────

  const registerActivity = useMutation({
    mutationFn: async (payload: RegisterActivityRequest) => {
      switch (payload.type) {
        case 'BUY':
          return investmentsService.registerBuy(payload);
        case 'SELL':
          return investmentsService.registerSell(payload);
        case 'DEPOSIT':
          return investmentsService.registerDeposit(payload);
        case 'WITHDRAW':
          return investmentsService.registerWithdrawal(payload);
        default:
          // Sin endpoint en el servicio todavía: falla de forma explícita.
          throw new Error(`Tipo de actividad no soportado todavía: ${payload.type}`);
      }
    },
    onSuccess: async () => {
      notify.success(t('alerts.activityCreated'));
      await invalidateAll();
    },
    onError: (error) => {
      notify.error(getErrorMessage(error, t('alerts.activityCreateError')));
    },
  });

  const editActivity = useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: EditActivityRequest }) =>
      investmentsService.editActivity(id, payload),
    onSuccess: async () => {
      notify.success(t('alerts.activityUpdated'));
      await invalidateAll();
    },
    onError: (error) => {
      notify.error(getErrorMessage(error, t('alerts.activityUpdateError')));
    },
  });

  // ── User assets ───────────────────────────────────────────────────────────

  const createUserAsset = useMutation({
    mutationFn: (payload: CreateUserAssetRequest) =>
      investmentsService.createUserAsset(payload),
    onSuccess: async () => {
      notify.success(t('alerts.assetCreated'));
      await queryClient.invalidateQueries({ queryKey: INVESTMENT_QUERY_KEYS.userAssets });
      await invalidateAll();
    },
    onError: (error) => {
      notify.error(getErrorMessage(error, t('alerts.assetCreateError')));
    },
  });

  const updateUserAsset = useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateUserAssetRequest }) =>
      investmentsService.updateUserAsset(id, payload),
    onSuccess: async () => {
      notify.success(t('alerts.assetUpdated'));
      await queryClient.invalidateQueries({ queryKey: INVESTMENT_QUERY_KEYS.userAssets });
      await invalidateAll();
    },
    onError: (error) => {
      notify.error(getErrorMessage(error, t('alerts.assetUpdateError')));
    },
  });

  const registerPrice = useMutation({
    mutationFn: ({ assetId, payload }: { assetId: string; payload: RegisterUserAssetPriceRequest }) =>
      investmentsService.registerUserAssetPrice(assetId, payload),
    onSuccess: async () => {
      notify.success(t('alerts.priceRegistered'));
      await invalidateAll();
    },
    onError: (error) => {
      notify.error(getErrorMessage(error, t('alerts.priceRegisterError')));
    },
  });

  // ── Market data ───────────────────────────────────────────────────────────
  // Invalida portfolio, rendimiento y salud: sin esto, los precios refrescados
  // no se reflejaban hasta el siguiente refetch.

  const refreshMarketData = useMutation({
    mutationFn: ({ currencies }: { currencies: string[] }) => {
      const to = new Date();
      const from = new Date(to);
      from.setDate(from.getDate() - 7);
      return investmentsService.refreshMarketData(
        from.toISOString(),
        to.toISOString(),
        currencies,
      );
    },
    onSuccess: async (result) => {
      if (result.failures.length > 0) {
        notify.error(
          t('alerts.marketDataPartial', {
            count: result.failures.length,
            defaultValue:
              'No se pudieron actualizar {{count}} precios. Revisa los activos afectados.',
          }),
        );
      } else {
        notify.success(
          t('alerts.marketDataRefreshed', {
            count: result.updatedPrices + result.updatedFxRates,
          }),
        );
      }
      await invalidateAll();
    },
    onError: (error) => {
      notify.error(getErrorMessage(error, t('alerts.marketDataRefreshError')));
    },
  });

  // ── Tracking ──────────────────────────────────────────────────────────────

  const configureTracking = useMutation({
    mutationFn: (payload: ConfigureTrackingRequest) =>
      investmentsService.configureTracking(payload),
    onSuccess: async () => {
      notify.success(t('alerts.trackingConfigured'));
      await queryClient.invalidateQueries({ queryKey: INVESTMENT_QUERY_KEYS.tracking });
      await invalidateAll();
    },
    onError: (error) => {
      notify.error(getErrorMessage(error, t('alerts.trackingConfigError')));
    },
  });

  return {
    registerActivity,
    editActivity,
    createUserAsset,
    updateUserAsset,
    registerPrice,
    refreshMarketData,
    configureTracking,
    isRegisteringActivity: registerActivity.isPending,
    isEditingActivity: editActivity.isPending,
    isCreatingAsset: createUserAsset.isPending,
    isUpdatingAsset: updateUserAsset.isPending,
    isRegisteringPrice: registerPrice.isPending,
    isRefreshingMarketData: refreshMarketData.isPending,
    isConfiguringTracking: configureTracking.isPending,
  };
}
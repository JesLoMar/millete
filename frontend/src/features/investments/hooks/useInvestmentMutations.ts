import { useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import { useTranslation } from 'react-i18next';

import { notify } from '@/shared/utils/notifications/notify';
import {
  ALL_INVESTMENT_QUERY_PREFIXES,
} from '../constants';
import { investmentsService } from '../services/investments.service';
import type {
  ConfigureTrackingRequest,
  CreateHoldingRequest,
  CreateUserAssetRequest,
  EditActivityRequest,
  InvestmentLedgerSnapshot,
  RefreshMarketDataRequest,
  RegisterActivityRequest,
  RegisterUserAssetPriceRequest,
  ReplaceHoldingHistoryRequest,
  UpdateUserAssetRequest,
} from '../types';

function getErrorMessage(
  error: unknown,
  fallback: string,
): string {
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

  const invalidateAll = () =>
    Promise.all(
      ALL_INVESTMENT_QUERY_PREFIXES.map((prefix) =>
        queryClient.invalidateQueries({
          queryKey: [prefix],
        }),
      ),
    );

  const registerActivity = useMutation({
    mutationFn: (payload: RegisterActivityRequest) =>
      investmentsService.registerActivity(payload),
    onSuccess: async () => {
      await invalidateAll();
      notify.success(t('alerts.activityCreated'));
    },
    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(error, t('alerts.activityCreateError')),
      );
    },
  });

  const editActivity = useMutation({
    mutationFn: ({
      id,
      data,
    }: {
      id: string;
      data: EditActivityRequest;
    }) => investmentsService.editActivity(id, data),
    onSuccess: async () => {
      await invalidateAll();
      notify.success(t('alerts.activityUpdated'));
    },
    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(error, t('alerts.activityUpdateError')),
      );
    },
  });

  const createHolding = useMutation({
    mutationFn: (payload: CreateHoldingRequest) =>
      investmentsService.createHolding(payload),
    onSuccess: async () => {
      await invalidateAll();
      notify.success(t('alerts.holdingCreated'));
    },
    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(error, t('alerts.holdingCreateError')),
      );
    },
  });

  const replaceHoldingHistory = useMutation({
    mutationFn: ({
      holdingId,
      data,
    }: {
      holdingId: string;
      data: ReplaceHoldingHistoryRequest;
    }) =>
      investmentsService.replaceHoldingHistory(
        holdingId,
        data,
      ),
    onSuccess: async () => {
      await invalidateAll();
      notify.success(t('alerts.holdingHistoryReplaced'));
    },
    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(
          error,
          t('alerts.holdingHistoryReplaceError'),
        ),
      );
    },
  });

  const createUserAsset = useMutation({
    mutationFn: (payload: CreateUserAssetRequest) =>
      investmentsService.createUserAsset(payload),
    onSuccess: async () => {
      await invalidateAll();
      notify.success(t('alerts.assetCreated'));
    },
    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(error, t('alerts.assetCreateError')),
      );
    },
  });

  const updateUserAsset = useMutation({
    mutationFn: ({
      id,
      data,
    }: {
      id: string;
      data: UpdateUserAssetRequest;
    }) => investmentsService.updateUserAsset(id, data),
    onSuccess: async () => {
      await invalidateAll();
      notify.success(t('alerts.assetUpdated'));
    },
    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(error, t('alerts.assetUpdateError')),
      );
    },
  });

  const registerUserAssetPrice = useMutation({
    mutationFn: ({
      assetId,
      data,
    }: {
      assetId: string;
      data: RegisterUserAssetPriceRequest;
    }) =>
      investmentsService.registerUserAssetPrice(
        assetId,
        data,
      ),
    onSuccess: async () => {
      await invalidateAll();
      notify.success(t('alerts.priceRegistered'));
    },
    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(
          error,
          t('alerts.priceRegisterError'),
        ),
      );
    },
  });

  const configureTracking = useMutation({
    mutationFn: (payload: ConfigureTrackingRequest) =>
      investmentsService.configureTracking(payload),
    onSuccess: async () => {
      await invalidateAll();
      notify.success(t('alerts.trackingConfigured'));
    },
    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(
          error,
          t('alerts.trackingConfigError'),
        ),
      );
    },
  });

  const refreshMarketData = useMutation({
    mutationFn: (payload: RefreshMarketDataRequest) =>
      investmentsService.refreshMarketData(payload),
    onSuccess: async () => {
      await invalidateAll();
      notify.success(t('alerts.marketDataRefreshed'));
    },
    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(
          error,
          t('alerts.marketDataRefreshError'),
        ),
      );
    },
  });

  const restoreLedger = useMutation({
    mutationFn: (snapshot: InvestmentLedgerSnapshot) =>
      investmentsService.restoreLedger(snapshot),
    onSuccess: async () => {
      await invalidateAll();
      notify.success(t('alerts.ledgerRestored'));
    },
    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(error, t('alerts.ledgerRestoreError')),
      );
    },
  });

  return {
    registerActivity,
    editActivity,
    createHolding,
    replaceHoldingHistory,
    createUserAsset,
    updateUserAsset,
    registerUserAssetPrice,
    configureTracking,
    refreshMarketData,
    restoreLedger,
  };
}

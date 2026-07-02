/**
 * Cliente HTTP centralizado para comunicarse con el backend Spring Boot.
 * Todos los microfrontends usan este módulo compartido para evitar duplicar lógica de API.
 */
import {
  AccountResponse,
  ApiError,
  BalanceResponse,
  CreateAccountRequest,
  ErrorResponse,
  TransactionRequest,
  TransactionResponse,
} from '../types';
import { logger } from '../util/logger';

const API_BASE = import.meta.env.VITE_API_URL ?? '';

/**
 * Procesa la respuesta HTTP: convierte JSON en caso de éxito,
 * o lanza ApiError con detalle en caso de fallo.
 */
async function handleResponse<T>(response: Response, operation: string): Promise<T> {
  if (!response.ok) {
    let error: ErrorResponse;

    try {
      error = await response.json();
    } catch {
      // El servidor no retornó JSON (ej: backend caído, error de red)
      logger.error(`[API:${operation}] Respuesta no parseable - HTTP ${response.status}`);
      error = {
        status: response.status,
        message: 'Error de conexión con el servidor',
        timestamp: new Date().toISOString(),
      };
    }

    logger.warn(`[API:${operation}] Fallo - HTTP ${error.status}: ${error.message}`, error.errors);
    throw new ApiError(error.message, error.status, error.errors);
  }

  logger.debug(`[API:${operation}] Éxito - HTTP ${response.status}`);
  return response.json();
}

export const accountApi = {
  createAccount(data: CreateAccountRequest): Promise<AccountResponse> {
    logger.info('[API:CREAR_CUENTA] Enviando petición', { titular: data.titular });
    return fetch(`${API_BASE}/accounts`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
      .then((res) => handleResponse<AccountResponse>(res, 'CREAR_CUENTA'))
      .catch((err) => {
        if (!(err instanceof ApiError)) {
          logger.error('[API:CREAR_CUENTA] Error de red o conexión', err);
        }
        throw err;
      });
  },

  deposit(accountId: number, data: TransactionRequest): Promise<TransactionResponse> {
    logger.info('[API:DEPOSITO] Enviando petición', { accountId, monto: data.monto });
    return fetch(`${API_BASE}/accounts/${accountId}/deposit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
      .then((res) => handleResponse<TransactionResponse>(res, 'DEPOSITO'))
      .catch((err) => {
        if (!(err instanceof ApiError)) {
          logger.error('[API:DEPOSITO] Error de red o conexión', { accountId, err });
        }
        throw err;
      });
  },

  withdraw(accountId: number, data: TransactionRequest): Promise<TransactionResponse> {
    logger.info('[API:RETIRO] Enviando petición', { accountId, monto: data.monto });
    return fetch(`${API_BASE}/accounts/${accountId}/withdraw`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
      .then((res) => handleResponse<TransactionResponse>(res, 'RETIRO'))
      .catch((err) => {
        if (!(err instanceof ApiError)) {
          logger.error('[API:RETIRO] Error de red o conexión', { accountId, err });
        }
        throw err;
      });
  },

  getBalance(accountId: number): Promise<BalanceResponse> {
    logger.info('[API:CONSULTA_SALDO] Enviando petición', { accountId });
    return fetch(`${API_BASE}/accounts/${accountId}/balance`)
      .then((res) => handleResponse<BalanceResponse>(res, 'CONSULTA_SALDO'))
      .catch((err) => {
        if (!(err instanceof ApiError)) {
          logger.error('[API:CONSULTA_SALDO] Error de red o conexión', { accountId, err });
        }
        throw err;
      });
  },
};

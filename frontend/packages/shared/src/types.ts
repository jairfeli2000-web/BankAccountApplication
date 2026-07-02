export type TipoTransaccion = 'DEPOSITO' | 'RETIRO';

export interface AccountResponse {
  id: number;
  titular: string;
  saldo: number;
  fechaCreacion: string;
}

export interface BalanceResponse {
  id: number;
  titular: string;
  saldo: number;
}

export interface CreateAccountRequest {
  titular: string;
}

export interface TransactionRequest {
  monto: number;
}

export interface TransactionResponse {
  transaccionId: number;
  cuentaId: number;
  titular: string;
  tipo: TipoTransaccion;
  monto: number;
  saldo: number;
  fecha: string;
}

export interface ErrorResponse {
  status: number;
  message: string;
  errors?: Record<string, string>;
  timestamp: string;
}

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public errors?: Record<string, string>
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

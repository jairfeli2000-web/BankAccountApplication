import { useState, FormEvent, useEffect } from 'react';
import { accountApi, ApiError, TransactionResponse, formatCurrency } from '@bank/shared';
import '@bank/shared/styles/components.css';
import './DepositApp.css';

interface Props {
  suggestedAccountId?: number | null;
  onDepositCompleted?: (accountId: number) => void;
}

export function DepositApp({ suggestedAccountId, onDepositCompleted }: Props) {
  const [accountId, setAccountId] = useState('');
  const [monto, setMonto] = useState('');
  const [accountError, setAccountError] = useState('');
  const [amountError, setAmountError] = useState('');
  const [apiError, setApiError] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<TransactionResponse | null>(null);

  useEffect(() => {
    if (suggestedAccountId) {
      setAccountId(String(suggestedAccountId));
    }
  }, [suggestedAccountId]);

  const validate = (): boolean => {
    let valid = true;
    const id = Number(accountId);
    if (!accountId.trim() || isNaN(id) || id <= 0 || !Number.isInteger(id)) {
      setAccountError('Ingrese un ID de cuenta válido');
      valid = false;
    } else {
      setAccountError('');
    }

    const amount = Number(monto);
    if (!monto.trim() || isNaN(amount) || amount <= 0) {
      setAmountError('El monto debe ser mayor a cero');
      valid = false;
    } else {
      setAmountError('');
    }

    return valid;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setApiError('');
    setResult(null);

    if (!validate()) return;

    setLoading(true);
    try {
      const response = await accountApi.deposit(Number(accountId), { monto: Number(monto) });
      setResult(response);
      setMonto('');
      onDepositCompleted?.(response.cuentaId);
    } catch (err) {
      if (err instanceof ApiError) {
        setApiError(err.errors?.monto ?? err.message);
      } else {
        setApiError('No se pudo conectar con el servidor. Verifique que el backend esté activo.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card deposit-app">
      <div className="card-header">
        <div className="card-icon deposit-icon">↑</div>
        <div>
          <h2 className="card-title">Consignación</h2>
          <p className="card-subtitle">Microfrontend — Depósitos bancarios</p>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="deposit-accountId" className="form-label">ID de la cuenta</label>
          <input
            id="deposit-accountId"
            type="number"
            min="1"
            className={`form-input ${accountError ? 'error' : ''}`}
            placeholder="Ej: 1"
            value={accountId}
            onChange={(e) => { setAccountId(e.target.value); setAccountError(''); }}
            disabled={loading}
          />
          {accountError && <p className="form-error">{accountError}</p>}
        </div>

        <div className="form-group">
          <label htmlFor="deposit-monto" className="form-label">Monto a consignar</label>
          <input
            id="deposit-monto"
            type="number"
            min="0.01"
            step="0.01"
            className={`form-input ${amountError ? 'error' : ''}`}
            placeholder="Ej: 500.00"
            value={monto}
            onChange={(e) => { setMonto(e.target.value); setAmountError(''); }}
            disabled={loading}
          />
          {amountError && <p className="form-error">{amountError}</p>}
        </div>

        <button type="submit" className="btn btn-primary btn-deposit" disabled={loading}>
          {loading ? <span className="spinner" /> : 'Realizar consignación'}
        </button>
      </form>

      {apiError && <div className="alert alert-error">{apiError}</div>}

      {result && (
        <div className="alert alert-success">
          Consignación realizada exitosamente
          <div className="result-box">
            <div className="result-row">
              <span className="result-label">Transacción</span>
              <span className="result-value">#{result.transaccionId}</span>
            </div>
            <div className="result-row">
              <span className="result-label">Titular</span>
              <span className="result-value">{result.titular}</span>
            </div>
            <div className="result-row">
              <span className="result-label">Monto consignado</span>
              <span className="result-value">{formatCurrency(result.monto)}</span>
            </div>
            <div className="result-row">
              <span className="result-label">Nuevo saldo</span>
              <span className="result-value highlight">{formatCurrency(result.saldo)}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

import { useState, FormEvent, useEffect } from 'react';
import { accountApi, ApiError, TransactionResponse, formatCurrency } from '@bank/shared';
import '@bank/shared/styles/components.css';
import './WithdrawApp.css';

interface Props {
  suggestedAccountId?: number | null;
  onWithdrawCompleted?: (accountId: number) => void;
}

export function WithdrawApp({ suggestedAccountId, onWithdrawCompleted }: Props) {
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
      const response = await accountApi.withdraw(Number(accountId), { monto: Number(monto) });
      setResult(response);
      setMonto('');
      onWithdrawCompleted?.(response.cuentaId);
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
    <div className="card withdraw-app">
      <div className="card-header">
        <div className="card-icon withdraw-icon">↓</div>
        <div>
          <h2 className="card-title">Retiro</h2>
          <p className="card-subtitle">Microfrontend — Retiros bancarios</p>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="withdraw-accountId" className="form-label">ID de la cuenta</label>
          <input
            id="withdraw-accountId"
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
          <label htmlFor="withdraw-monto" className="form-label">Monto a retirar</label>
          <input
            id="withdraw-monto"
            type="number"
            min="0.01"
            step="0.01"
            className={`form-input ${amountError ? 'error' : ''}`}
            placeholder="Ej: 200.00"
            value={monto}
            onChange={(e) => { setMonto(e.target.value); setAmountError(''); }}
            disabled={loading}
          />
          {amountError && <p className="form-error">{amountError}</p>}
        </div>

        <button type="submit" className="btn btn-primary btn-withdraw" disabled={loading}>
          {loading ? <span className="spinner" /> : 'Realizar retiro'}
        </button>
      </form>

      {apiError && <div className="alert alert-error">{apiError}</div>}

      {result && (
        <div className="alert alert-success">
          Retiro realizado exitosamente
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
              <span className="result-label">Monto retirado</span>
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

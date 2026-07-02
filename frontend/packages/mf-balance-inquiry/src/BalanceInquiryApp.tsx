import { useState, FormEvent, useEffect } from 'react';
import { accountApi, BalanceResponse, ApiError, formatCurrency } from '@bank/shared';
import '@bank/shared/styles/components.css';
import './BalanceInquiryApp.css';

interface Props {
  suggestedAccountId?: number | null;
  refreshTrigger?: number;
}

export function BalanceInquiryApp({ suggestedAccountId, refreshTrigger }: Props) {
  const [accountId, setAccountId] = useState('');
  const [fieldError, setFieldError] = useState('');
  const [apiError, setApiError] = useState('');
  const [loading, setLoading] = useState(false);
  const [balance, setBalance] = useState<BalanceResponse | null>(null);

  useEffect(() => {
    if (suggestedAccountId) {
      setAccountId(String(suggestedAccountId));
    }
  }, [suggestedAccountId]);

  useEffect(() => {
    if (refreshTrigger && accountId.trim()) {
      const id = Number(accountId);
      if (!isNaN(id) && id > 0) {
        accountApi.getBalance(id).then(setBalance).catch(() => {});
      }
    }
  }, [refreshTrigger, accountId]);

  const validate = (): boolean => {
    const id = Number(accountId);
    if (!accountId.trim() || isNaN(id) || id <= 0 || !Number.isInteger(id)) {
      setFieldError('Ingrese un ID de cuenta válido (número entero positivo)');
      return false;
    }
    setFieldError('');
    return true;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setApiError('');
    setBalance(null);

    if (!validate()) return;

    setLoading(true);
    try {
      const result = await accountApi.getBalance(Number(accountId));
      setBalance(result);
    } catch (err) {
      if (err instanceof ApiError) {
        setApiError(err.message);
      } else {
        setApiError('No se pudo conectar con el servidor. Verifique que el backend esté activo.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card balance-inquiry">
      <div className="card-header">
        <div className="card-icon" style={{ background: 'var(--info-light)', color: 'var(--info)' }}>
          $
        </div>
        <div>
          <h2 className="card-title">Consultar Saldo</h2>
          <p className="card-subtitle">Microfrontend — Consulta de saldos bancarios</p>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="accountId" className="form-label">ID de la cuenta</label>
          <input
            id="accountId"
            type="number"
            min="1"
            className={`form-input ${fieldError ? 'error' : ''}`}
            placeholder="Ej: 1"
            value={accountId}
            onChange={(e) => { setAccountId(e.target.value); setFieldError(''); }}
            disabled={loading}
          />
          {fieldError && <p className="form-error">{fieldError}</p>}
        </div>

        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? <span className="spinner" /> : 'Consultar saldo'}
        </button>
      </form>

      {apiError && <div className="alert alert-error">{apiError}</div>}

      {balance && (
        <div className="balance-result">
          <div className="balance-amount">{formatCurrency(balance.saldo)}</div>
          <div className="result-box">
            <div className="result-row">
              <span className="result-label">ID Cuenta</span>
              <span className="result-value">#{balance.id}</span>
            </div>
            <div className="result-row">
              <span className="result-label">Titular</span>
              <span className="result-value">{balance.titular}</span>
            </div>
            <div className="result-row">
              <span className="result-label">Saldo disponible</span>
              <span className="result-value highlight">{formatCurrency(balance.saldo)}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

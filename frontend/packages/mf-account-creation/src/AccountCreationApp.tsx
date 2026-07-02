import { useState, FormEvent } from 'react';
import { accountApi, AccountResponse, ApiError, formatCurrency } from '@bank/shared';
import '@bank/shared/styles/components.css';
import './AccountCreationApp.css';

interface Props {
  onAccountCreated?: (account: AccountResponse) => void;
}

export function AccountCreationApp({ onAccountCreated }: Props) {
  const [titular, setTitular] = useState('');
  const [fieldError, setFieldError] = useState('');
  const [apiError, setApiError] = useState('');
  const [loading, setLoading] = useState(false);
  const [createdAccount, setCreatedAccount] = useState<AccountResponse | null>(null);

  const validate = (): boolean => {
    const trimmed = titular.trim();
    if (!trimmed) {
      setFieldError('El titular es obligatorio');
      return false;
    }
    if (trimmed.length > 100) {
      setFieldError('El titular no puede superar 100 caracteres');
      return false;
    }
    setFieldError('');
    return true;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setApiError('');
    setCreatedAccount(null);

    if (!validate()) return;

    setLoading(true);
    try {
      const account = await accountApi.createAccount({ titular: titular.trim() });
      setCreatedAccount(account);
      setTitular('');
      onAccountCreated?.(account);
    } catch (err) {
      if (err instanceof ApiError) {
        setApiError(err.errors?.titular ?? err.message);
      } else {
        setApiError('No se pudo conectar con el servidor. Verifique que el backend esté activo.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card account-creation">
      <div className="card-header">
        <div className="card-icon" style={{ background: 'var(--accent-light)', color: 'var(--accent)' }}>
          +
        </div>
        <div>
          <h2 className="card-title">Crear Cuenta</h2>
          <p className="card-subtitle">Microfrontend — Registro de cuentas bancarias</p>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="titular" className="form-label">Titular de la cuenta</label>
          <input
            id="titular"
            type="text"
            className={`form-input ${fieldError ? 'error' : ''}`}
            placeholder="Ej: Juan Pérez"
            value={titular}
            onChange={(e) => { setTitular(e.target.value); setFieldError(''); }}
            maxLength={100}
            disabled={loading}
          />
          {fieldError && <p className="form-error">{fieldError}</p>}
        </div>

        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? <span className="spinner" /> : 'Crear cuenta'}
        </button>
      </form>

      {apiError && <div className="alert alert-error">{apiError}</div>}

      {createdAccount && (
        <div className="alert alert-success">
          Cuenta creada exitosamente
          <div className="result-box">
            <div className="result-row">
              <span className="result-label">ID</span>
              <span className="result-value">#{createdAccount.id}</span>
            </div>
            <div className="result-row">
              <span className="result-label">Titular</span>
              <span className="result-value">{createdAccount.titular}</span>
            </div>
            <div className="result-row">
              <span className="result-label">Saldo inicial</span>
              <span className="result-value highlight">{formatCurrency(createdAccount.saldo)}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

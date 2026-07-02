/**
 * Shell App — Contenedor principal de microfrontends.
 * Orquesta la comunicación entre MFEs propagando el ID de cuenta activa
 * y disparando refresco de saldo tras transacciones.
 */
import { StrictMode, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { AccountCreationApp } from '@bank/mf-account-creation';
import { BalanceInquiryApp } from '@bank/mf-balance-inquiry';
import { DepositApp } from '@bank/mf-deposit';
import { WithdrawApp } from '@bank/mf-withdraw';
import { AccountResponse, logger } from '@bank/shared';
import './index.css';
import './App.css';

function ShellApp() {
  // ID de la última cuenta creada o sobre la que se operó
  const [activeAccountId, setActiveAccountId] = useState<number | null>(null);
  // Contador que fuerza refresco del saldo en mf-balance-inquiry
  const [balanceRefreshTrigger, setBalanceRefreshTrigger] = useState(0);

  const handleAccountCreated = (account: AccountResponse) => {
    logger.info('[SHELL] Cuenta creada, propagando ID a microfrontends', { accountId: account.id });
    setActiveAccountId(account.id);
  };

  const handleTransactionCompleted = (accountId: number) => {
    logger.info('[SHELL] Transacción completada, refrescando saldo', { accountId });
    setActiveAccountId(accountId);
    setBalanceRefreshTrigger((prev) => prev + 1);
  };

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-content">
          <div className="logo">
            <span className="logo-icon">◈</span>
            <div>
              <h1 className="logo-title">Bank Account Portal</h1>
              <p className="logo-subtitle">Arquitectura de Microfrontends</p>
            </div>
          </div>
          <div className="header-badge">
            <span className="badge-dot" />
            Shell App
          </div>
        </div>
      </header>

      <main className="app-main">
        <section className="mfe-grid">
          <div className="mfe-wrapper">
            <span className="mfe-label">mf-account-creation</span>
            <AccountCreationApp onAccountCreated={handleAccountCreated} />
          </div>
          <div className="mfe-wrapper">
            <span className="mfe-label">mf-deposit</span>
            <DepositApp
              suggestedAccountId={activeAccountId}
              onDepositCompleted={handleTransactionCompleted}
            />
          </div>
          <div className="mfe-wrapper">
            <span className="mfe-label">mf-withdraw</span>
            <WithdrawApp
              suggestedAccountId={activeAccountId}
              onWithdrawCompleted={handleTransactionCompleted}
            />
          </div>
          <div className="mfe-wrapper">
            <span className="mfe-label">mf-balance-inquiry</span>
            <BalanceInquiryApp
              suggestedAccountId={activeAccountId}
              refreshTrigger={balanceRefreshTrigger}
            />
          </div>
        </section>

        <footer className="app-footer">
          <p>
            Los microfrontends se comunican con el backend Spring Boot en{' '}
            <code>http://localhost:8080</code>
          </p>
        </footer>
      </main>
    </div>
  );
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ShellApp />
  </StrictMode>
);

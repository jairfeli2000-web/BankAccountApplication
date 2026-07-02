/**
 * Utilidad de logging para el frontend.
 * Prefija mensajes con timestamp para facilitar el diagnóstico en consola del navegador.
 */
const isDev = import.meta.env.DEV;

export const logger = {
  info(message: string, data?: unknown) {
    if (isDev) console.info(`[${timestamp()}] ${message}`, data ?? '');
  },

  warn(message: string, data?: unknown) {
    console.warn(`[${timestamp()}] ${message}`, data ?? '');
  },

  error(message: string, data?: unknown) {
    console.error(`[${timestamp()}] ${message}`, data ?? '');
  },

  debug(message: string, data?: unknown) {
    if (isDev) console.debug(`[${timestamp()}] ${message}`, data ?? '');
  },
};

function timestamp(): string {
  return new Date().toISOString();
}

import { useCallback, useEffect, useState } from 'react';
import { getErrorMessage } from '../utils/errors';
import { useToast } from '../context/ToastContext';

export function useAsync(asyncFn, deps = [], options = {}) {
  const { immediate = true, successMessage } = options;
  const { showToast } = useToast();
  const [data, setData] = useState(options.initialData ?? null);
  const [loading, setLoading] = useState(immediate);
  const [error, setError] = useState(null);

  const execute = useCallback(
    async (...args) => {
      setLoading(true);
      setError(null);
      try {
        const response = await asyncFn(...args);
        const nextData = response?.data ?? response;
        setData(nextData);
        if (successMessage) showToast(successMessage, 'success');
        return nextData;
      } catch (err) {
        const message = getErrorMessage(err);
        setError(message);
        showToast(message, 'error');
        throw err;
      } finally {
        setLoading(false);
      }
    },
    deps
  );

  useEffect(() => {
    if (immediate) execute();
  }, [execute, immediate]);

  return { data, loading, error, execute, setData };
}

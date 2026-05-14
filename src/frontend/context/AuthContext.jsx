import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { authApi } from '../api/authApi';
import { userApi } from '../api/userApi';
import {
  TOKEN_KEY,
  USER_KEY,
  clearStoredAuth,
  decodeJwt,
  getStoredToken,
  normalizeRole,
  setStoredToken,
} from '../utils/auth';
import { useToast } from './ToastContext';

const AuthContext = createContext(null);

function userFromToken(token) {
  const payload = decodeJwt(token);
  if (!payload) return null;
  return {
    email: payload.sub || payload.email,
    firstName: payload.firstName || payload.given_name || '',
    lastName: payload.lastName || payload.family_name || '',
    role: normalizeRole(payload.role || payload.roles || payload.authorities),
  };
}

export function AuthProvider({ children }) {
  const { showToast } = useToast();
  const [token, setToken] = useState(() => getStoredToken());
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem(USER_KEY);
    return stored ? JSON.parse(stored) : userFromToken(getStoredToken());
  });
  const [loadingProfile, setLoadingProfile] = useState(false);

  useEffect(() => {
    const handleLogout = () => {
      setToken(null);
      setUser(null);
    };
    window.addEventListener('auth:logout', handleLogout);
    return () => window.removeEventListener('auth:logout', handleLogout);
  }, []);

  useEffect(() => {
    document.body.classList.toggle('is-authenticated', Boolean(token));
  }, [token]);

  const saveUser = (nextUser) => {
    setUser(nextUser);
    localStorage.setItem(USER_KEY, JSON.stringify(nextUser));
    return nextUser;
  };

  const fetchProfile = async (profileToken) => {
    const { data } = await userApi.profile();
    return {
      ...userFromToken(profileToken),
      ...data,
      role: normalizeRole(data.role),
    };
  };

  const refreshProfile = async () => {
    if (!token) return null;
    setLoadingProfile(true);
    try {
      return saveUser(await fetchProfile(token));
    } catch {
      const fallback = userFromToken(token);
      setUser((current) => current || fallback);
      return fallback;
    } finally {
      setLoadingProfile(false);
    }
  };

  useEffect(() => {
    if (token) {
      refreshProfile();
    }
  }, [token]);

  const login = async (payload) => {
    const { data } = await authApi.login(payload);
    const nextToken = data.token;
    setStoredToken(nextToken);
    setToken(nextToken);
    try {
      const nextUser = await fetchProfile(nextToken);
      saveUser(nextUser);
      showToast(data.message || 'Login successful', 'success');
      return nextUser;
    } catch {
      const fallbackUser = userFromToken(nextToken) || { email: payload.email, role: 'USER' };
      saveUser(fallbackUser);
      showToast(data.message || 'Login successful', 'success');
      return fallbackUser;
    }
  };

  const register = async (payload) => {
    const { data } = await authApi.register(payload);
    showToast(data.message || 'Registration started', 'success');
    return data;
  };

  const logout = (message = 'You have been signed out', type = 'info') => {
    clearStoredAuth();
    localStorage.removeItem(TOKEN_KEY);
    setToken(null);
    setUser(null);
    if (message) showToast(message, type);
  };

  const value = useMemo(
    () => ({
      isAuthenticated: Boolean(token),
      loadingProfile,
      login,
      logout,
      refreshProfile,
      register,
      token,
      user,
      role: normalizeRole(user?.role),
    }),
    [token, user, loadingProfile]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider');
  return context;
}

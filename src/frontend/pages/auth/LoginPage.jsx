import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import Button from '../../components/Button';
import InputField from '../../components/InputField';
import { useAuth } from '../../context/AuthContext';
import { getErrorMessage } from '../../utils/errors';
import { useToast } from '../../context/ToastContext';

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const { showToast } = useToast();
  const [form, setForm] = useState({ email: '', password: '' });
  const [loading, setLoading] = useState(false);

  const update = (event) => setForm({ ...form, [event.target.name]: event.target.value });

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    try {
      const user = await login(form);
      const target = location.state?.from?.pathname || (user.role === 'ADMIN' ? '/admin' : '/');
      navigate(target, { replace: true });
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="auth-card glass">
      <span className="eyebrow">Welcome back</span>
      <h2>Sign in to your account</h2>
      <form className="stack-form" onSubmit={submit}>
        <InputField label="Email" name="email" type="email" value={form.email} onChange={update} required />
        <InputField label="Password" name="password" type="password" value={form.password} onChange={update} required />
        <Button disabled={loading}>{loading ? 'Signing in...' : 'Login'}</Button>
      </form>
      <div className="auth-links">
        <Link to="/forgot-password">Forgot password?</Link>
        <Link to="/register">Create account</Link>
      </div>
    </section>
  );
}

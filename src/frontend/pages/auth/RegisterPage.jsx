import { useState } from 'react';
import { Link } from 'react-router-dom';
import Button from '../../components/Button';
import InputField from '../../components/InputField';
import Modal from '../../components/Modal';
import { authApi } from '../../api/authApi';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { getErrorMessage } from '../../utils/errors';

const EMAIL_PATTERN = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,6}$/;
const PASSWORD_PATTERN = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\S+$).{8,}$/;

const EMAIL_ERROR = 'Please provide a valid email address (e.g. user@example.com)';
const PASSWORD_ERROR = 'Password must be at least 8 characters and include digit, uppercase, lowercase, and special character (@#$%^&+=).';

export default function RegisterPage() {
  const { register } = useAuth();
  const { showToast } = useToast();
  const [token, setToken] = useState('');
  const [otp, setOtp] = useState('');
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  });
  const [errors, setErrors] = useState({});

  const validate = (nextForm = form) => {
    const nextErrors = {};
    if (nextForm.email && !EMAIL_PATTERN.test(nextForm.email)) {
      nextErrors.email = EMAIL_ERROR;
    }
    if (nextForm.password && !PASSWORD_PATTERN.test(nextForm.password)) {
      nextErrors.password = PASSWORD_ERROR;
    }
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const update = (event) => {
    const nextForm = { ...form, [event.target.name]: event.target.value };
    setForm(nextForm);
    validate(nextForm);
  };

  const submit = async (event) => {
    event.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      const data = await register(form);
      setToken(data.token);
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setLoading(false);
    }
  };

  const verify = async () => {
    setLoading(true);
    try {
      const { data } = await authApi.verifySignup({ token, otp });
      showToast(data.message || 'Registration verified', 'success');
      setToken('');
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="auth-card glass">
      <span className="eyebrow">Join GrandStay</span>
      <h2>Create your account</h2>
      <form className="stack-form" onSubmit={submit}>
        <div className="two-col">
          <InputField label="First name" name="firstName" value={form.firstName} onChange={update} required />
          <InputField label="Last name" name="lastName" value={form.lastName} onChange={update} required />
        </div>
        <InputField label="Email" name="email" type="email" value={form.email} onChange={update} error={errors.email} required />
        <InputField label="Password" name="password" type="password" value={form.password} onChange={update} error={errors.password} required />
        <Button disabled={loading}>{loading ? 'Creating...' : 'Register'}</Button>
      </form>
      <div className="auth-links">
        <Link to="/login">Already have an account?</Link>
      </div>
      <Modal open={Boolean(token)} title="Verify your email" onClose={() => setToken('')}>
        <div className="otp-copy">
          <p>
            Enter the OTP sent to <strong>{form.email}</strong>.
          </p>
          <p className="otp-hint">
            If you do not see the OTP in your Primary inbox, please check your Spam or Junk folder.
          </p>
        </div>
        <InputField label="OTP" value={otp} onChange={(event) => setOtp(event.target.value)} />
        <Button onClick={verify} disabled={loading}>{loading ? 'Verifying...' : 'Verify account'}</Button>
      </Modal>
    </section>
  );
}

import { useState } from 'react';
import { Link } from 'react-router-dom';
import Button from '../../components/Button';
import InputField from '../../components/InputField';
import { authApi } from '../../api/authApi';
import { useToast } from '../../context/ToastContext';
import { getErrorMessage } from '../../utils/errors';

export default function ForgotPasswordPage() {
  const { showToast } = useToast();
  const [step, setStep] = useState('email');
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ email: '', token: '', otp: '', newPassword: '' });

  const update = (event) => setForm({ ...form, [event.target.name]: event.target.value });

  const sendOtp = async (event) => {
    event.preventDefault();
    setLoading(true);
    try {
      const { data } = await authApi.forgotPassword(form.email);
      setForm((current) => ({ ...current, token: data.token }));
      showToast(data.message || 'OTP sent', 'success');
      setStep('otp');
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setLoading(false);
    }
  };

  const verifyOtp = async (event) => {
    event.preventDefault();
    setLoading(true);
    try {
      await authApi.verifyForgotOtp({ token: form.token, otp: form.otp });
      showToast('OTP verified', 'success');
      setStep('reset');
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setLoading(false);
    }
  };

  const resetPassword = async (event) => {
    event.preventDefault();
    setLoading(true);
    try {
      await authApi.resetPassword({ token: form.token, newPassword: form.newPassword, confirmPassword: form.newPassword });
      showToast('Password reset successfully', 'success');
      setStep('done');
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="auth-card glass">
      <span className="eyebrow">Account recovery</span>
      <h2>Reset your password</h2>
      {step === 'email' && (
        <form className="stack-form" onSubmit={sendOtp}>
          <InputField label="Registered email" name="email" type="email" value={form.email} onChange={update} required />
          <Button disabled={loading}>{loading ? 'Sending...' : 'Send OTP'}</Button>
        </form>
      )}
      {step === 'otp' && (
        <form className="stack-form" onSubmit={verifyOtp}>
          <p>If the OTP is not in your Primary inbox, please check your Spam or Junk folder.</p>
          <InputField label="OTP" name="otp" value={form.otp} onChange={update} required />
          <Button disabled={loading}>{loading ? 'Checking...' : 'Verify OTP'}</Button>
        </form>
      )}
      {step === 'reset' && (
        <form className="stack-form" onSubmit={resetPassword}>
          <InputField label="New password" name="newPassword" type="password" value={form.newPassword} onChange={update} required />
          <Button disabled={loading}>{loading ? 'Saving...' : 'Reset password'}</Button>
        </form>
      )}
      {step === 'done' && (
        <div className="stack-form">
          <p className="success-copy">Your password has been reset successfully. You can now sign in with your new password.</p>
          <Link to="/login">
            <Button>Go to login</Button>
          </Link>
        </div>
      )}
    </section>
  );
}

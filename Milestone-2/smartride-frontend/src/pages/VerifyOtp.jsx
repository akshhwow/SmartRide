// ================== VERIFYOTP.JSX ==================
// Save as: src/pages/VerifyOtp.jsx

import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { authAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-toastify';
import './Auth.css';

const VerifyOtp = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [otp, setOtp] = useState('');
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  
  const email = location.state?.email || '';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await authAPI.verifyOtp({ email, otpCode: otp });
      
      if (response.data.success) {
        login(response.data.data.token, response.data.data.user);
        toast.success(response.data.message);
        navigate('/dashboard');
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Invalid OTP');
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    setResending(true);
    try {
      const response = await authAPI.resendOtp(email);
      toast.success(response.data.message);
    } catch (error) {
      toast.error('Failed to resend OTP');
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2 className="auth-title">Verify Email</h2>
        <p className="auth-subtitle">
          Enter the 6-digit code sent to<br />
          <strong>{email}</strong>
        </p>

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label>OTP Code</label>
            <input
              type="text"
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              required
              maxLength="6"
              placeholder="123456"
              style={{ fontSize: '1.5rem', textAlign: 'center', letterSpacing: '0.5rem' }}
            />
          </div>

          <button type="submit" className="auth-button" disabled={loading}>
            {loading ? 'Verifying...' : 'Verify OTP'}
          </button>

          <button
            type="button"
            onClick={handleResend}
            disabled={resending}
            className="auth-button"
            style={{ background: 'transparent', color: '#667eea', border: '2px solid #667eea', marginTop: '0.5rem' }}
          >
            {resending ? 'Sending...' : 'Resend OTP'}
          </button>

          <p className="auth-footer">
            <Link to="/login">Back to Login</Link>
          </p>
        </form>
      </div>
    </div>
  );
};

export default VerifyOtp;
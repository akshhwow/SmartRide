import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { authAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-toastify';

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
    <div className="min-h-[85vh] flex flex-col justify-center items-center py-12 sm:px-6 lg:px-8 bg-gray-50">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div className="mx-auto w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mb-6 shadow-sm shadow-blue-200">
          <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path></svg>
        </div>
        <h2 className="text-3xl font-extrabold text-blue-600 tracking-tight">
          Verify Email
        </h2>
        <p className="mt-4 text-gray-500 font-medium leading-relaxed">
          Enter the 6-digit verification code sent to <br />
          <strong className="text-gray-900 border-b-2 border-blue-200 px-1">{email}</strong>
        </p>
      </div>

      <div className="mt-8 mx-4 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-10 px-8 shadow-sm sm:rounded-3xl border border-gray-100 relative overflow-hidden">
          <form className="space-y-8 relative z-10" onSubmit={handleSubmit}>
            <div>
              <label className="block text-sm font-semibold text-gray-700 text-center mb-4 uppercase tracking-wider">
                6-Digit Security Code
              </label>
              <input
                type="text"
                value={otp}
                onChange={(e) => setOtp(e.target.value.replace(/[^0-9]/g, ''))}
                required
                maxLength="6"
                placeholder="000000"
                className="block w-full text-center text-4xl tracking-[0.5em] font-mono py-4 border-2 border-gray-200 rounded-xl shadow-sm focus:outline-none focus:ring-0 focus:border-blue-500 transition-colors bg-gray-50 placeholder-gray-300"
              />
            </div>

            <div className="space-y-4">
              <button
                type="submit"
                disabled={loading || otp.length !== 6}
                className="w-full flex justify-center py-4 px-4 border border-transparent rounded-xl shadow-sm text-lg font-bold text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all disabled:bg-gray-300 disabled:cursor-not-allowed shadow-blue-200"
              >
                {loading ? 'Verifying...' : 'Verify Email'}
              </button>

              <button
                type="button"
                onClick={handleResend}
                disabled={resending}
                className="w-full flex justify-center py-3.5 px-4 border-2 border-blue-600 rounded-xl shadow-sm text-sm font-bold text-blue-600 bg-transparent hover:bg-blue-50 focus:outline-none transition-all disabled:opacity-50"
              >
                {resending ? 'Sending...' : 'Resend Code'}
              </button>
            </div>
          </form>

          <div className="mt-8 text-center relative z-10">
            <Link to="/login" className="text-sm font-bold text-gray-500 hover:text-gray-700 transition-colors flex items-center justify-center gap-1">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
              Back to Login
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default VerifyOtp;
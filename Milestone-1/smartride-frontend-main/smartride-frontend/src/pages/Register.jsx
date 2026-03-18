import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authAPI } from '../services/api';
import { toast } from 'react-toastify';
import './Auth.css';

/**
 * ============================================================
 * Register Component - New User Registration
 * ============================================================
 *
 * Handles registration for both PASSENGER and DRIVER roles.
 *
 * 📌 FIXES APPLIED:
 *    1. vehicleType select had no default value — HTML 'required'
 *       on a select with value="" causes silent form failure.
 *       Fixed by pre-selecting "Sedan" as default for drivers.
 *
 *    2. vehicleCapacity was being sent as string even after parseInt
 *       when the field is empty — added fallback: parseInt(...) || 0
 *
 *    3. Added frontend validation before API call so user sees
 *       clear error messages instead of silent failures.
 *
 *    4. Added console.log for debugging so you can see what's
 *       being sent to the backend in browser DevTools Console.
 * ============================================================
 */
const Register = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phone: '',
    password: '',
    role: 'PASSENGER',
    carModel: '',
    licensePlate: '',
    vehicleCapacity: '',
    vehicleType: 'Sedan'   // ✅ FIX: Pre-select default so it's never empty
  });
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      // ✅ FIX: Frontend validation BEFORE calling API
      // This catches issues early and shows clear messages to user

      if (!formData.fullName.trim()) {
        toast.error('Please enter your full name');
        setLoading(false);
        return;
      }

      if (!formData.email.trim()) {
        toast.error('Please enter your email');
        setLoading(false);
        return;
      }

      if (!formData.phone.trim() || formData.phone.length !== 10) {
        toast.error('Please enter a valid 10-digit phone number');
        setLoading(false);
        return;
      }

      if (!formData.password || formData.password.length < 8) {
        toast.error('Password must be at least 8 characters');
        setLoading(false);
        return;
      }

      // Driver-specific validation
      if (formData.role === 'DRIVER') {
        if (!formData.carModel.trim()) {
          toast.error('Please enter your car model');
          setLoading(false);
          return;
        }
        if (!formData.licensePlate.trim()) {
          toast.error('Please enter your license plate');
          setLoading(false);
          return;
        }
        if (!formData.vehicleCapacity || parseInt(formData.vehicleCapacity) < 1) {
          toast.error('Please enter a valid vehicle capacity');
          setLoading(false);
          return;
        }
        if (!formData.vehicleType) {
          toast.error('Please select a vehicle type');
          setLoading(false);
          return;
        }
      }

      // Build the payload to send to backend
      const payload = {
        fullName: formData.fullName.trim(),
        email: formData.email.trim().toLowerCase(),
        phone: formData.phone.trim(),
        password: formData.password,
        role: formData.role
      };

      // Add driver fields only if registering as DRIVER
      if (formData.role === 'DRIVER') {
        payload.carModel = formData.carModel.trim();
        payload.licensePlate = formData.licensePlate.trim().toUpperCase();
        payload.vehicleCapacity = parseInt(formData.vehicleCapacity) || 4; // ✅ FIX: fallback to 4
        payload.vehicleType = formData.vehicleType;
      }

      // ✅ Debug: Log what we're sending (visible in browser Console F12)
      console.log('📤 Registering with payload:', payload);

      // Call the backend API
      const response = await authAPI.register(payload);

      console.log('📥 Registration response:', response.data);

      if (response.data.success) {
        toast.success(response.data.message || 'Registration successful! Check your email for OTP.');
        // Navigate to OTP verification page, passing the email
        navigate('/verify-otp', { state: { email: formData.email } });
      } else {
        // Backend returned success: false with an error message
        toast.error(response.data.message || 'Registration failed. Please try again.');
      }

    } catch (error) {
      console.error('❌ Registration error:', error);
      // Show the backend error message if available, else generic message
      toast.error(
        error.response?.data?.message ||
        'Registration failed. Please check your details and try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2 className="auth-title">Create Account</h2>
        <p className="auth-subtitle">Join SmartRide today!</p>

        <form onSubmit={handleSubmit} className="auth-form">

          {/* Full Name */}
          <div className="form-group">
            <label>Full Name</label>
            <input
              type="text"
              name="fullName"
              value={formData.fullName}
              onChange={handleChange}
              placeholder="John Doe"
            />
          </div>

          {/* Email */}
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="john@example.com"
            />
          </div>

          {/* Phone */}
          <div className="form-group">
            <label>Phone</label>
            <input
              type="tel"
              name="phone"
              value={formData.phone}
              onChange={handleChange}
              placeholder="9876543210"
              maxLength="10"
            />
          </div>

          {/* Password */}
          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Min 8 characters"
            />
          </div>

          {/* Role Selection */}
          <div className="form-group">
            <label>I want to</label>
            <select name="role" value={formData.role} onChange={handleChange}>
              <option value="PASSENGER">Find Rides (Passenger)</option>
              <option value="DRIVER">Offer Rides (Driver)</option>
            </select>
          </div>

          {/* Driver-specific fields — only shown when role is DRIVER */}
          {formData.role === 'DRIVER' && (
            <>
              <div className="driver-section-title">🚗 Driver Details</div>

              {/* Car Model */}
              <div className="form-group">
                <label>Car Model</label>
                <input
                  type="text"
                  name="carModel"
                  value={formData.carModel}
                  onChange={handleChange}
                  placeholder="Honda Civic"
                />
              </div>

              {/* License Plate */}
              <div className="form-group">
                <label>License Plate</label>
                <input
                  type="text"
                  name="licensePlate"
                  value={formData.licensePlate}
                  onChange={handleChange}
                  placeholder="MH-12-AB-1234"
                />
              </div>

              {/* Vehicle Capacity */}
              <div className="form-group">
                <label>Vehicle Capacity (seats)</label>
                <input
                  type="number"
                  name="vehicleCapacity"
                  value={formData.vehicleCapacity}
                  onChange={handleChange}
                  min="1"
                  max="8"
                  placeholder="4"
                />
              </div>

              {/* Vehicle Type */}
              <div className="form-group">
                <label>Vehicle Type</label>
                <select
                  name="vehicleType"
                  value={formData.vehicleType}
                  onChange={handleChange}
                >
                  {/* ✅ FIX: No empty option — always has a valid value selected */}
                  <option value="Sedan">Sedan</option>
                  <option value="SUV">SUV</option>
                  <option value="Hatchback">Hatchback</option>
                  <option value="Van">Van</option>
                </select>
              </div>
            </>
          )}

          {/* Submit Button */}
          <button
            type="submit"
            className="auth-button"
            disabled={loading}
          >
            {loading ? '⏳ Creating Account...' : 'Register'}
          </button>

          <p className="auth-footer">
            Already have an account? <Link to="/login">Login</Link>
          </p>
        </form>
      </div>
    </div>
  );
};

export default Register;
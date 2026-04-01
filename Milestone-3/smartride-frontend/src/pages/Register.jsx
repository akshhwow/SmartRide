import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authAPI } from '../services/api';
import { toast } from 'react-toastify';
import { motion, AnimatePresence } from 'framer-motion';
import LoadingAnimation from '../components/animations/LoadingAnimation';

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
    vehicleType: 'Sedan'
  });
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      if (!formData.fullName.trim()) return toast.error('Please enter your full name') && setLoading(false);
      if (!formData.email.trim()) return toast.error('Please enter your email') && setLoading(false);
      if (!formData.phone.trim() || formData.phone.length !== 10) return toast.error('Please enter a valid 10-digit phone number') && setLoading(false);
      if (!formData.password || formData.password.length < 8) return toast.error('Password must be at least 8 characters') && setLoading(false);

      if (formData.role === 'DRIVER') {
        if (!formData.carModel.trim()) return toast.error('Please enter your car model') && setLoading(false);
        if (!formData.licensePlate.trim()) return toast.error('Please enter your license plate') && setLoading(false);
        if (!formData.vehicleCapacity || parseInt(formData.vehicleCapacity) < 1) return toast.error('Please enter a valid vehicle capacity') && setLoading(false);
        if (!formData.vehicleType) return toast.error('Please select a vehicle type') && setLoading(false);
      }

      const payload = {
        fullName: formData.fullName.trim(),
        email: formData.email.trim().toLowerCase(),
        phone: formData.phone.trim(),
        password: formData.password,
        role: formData.role
      };

      if (formData.role === 'DRIVER') {
        payload.carModel = formData.carModel.trim();
        payload.licensePlate = formData.licensePlate.trim().toUpperCase();
        payload.vehicleCapacity = parseInt(formData.vehicleCapacity) || 4;
        payload.vehicleType = formData.vehicleType;
      }

      const response = await authAPI.register(payload);
      if (response.data.success) {
        toast.success(response.data.message || 'Registration successful! Check your email for OTP.');
        navigate('/verify-otp', { state: { email: formData.email } });
      } else {
        toast.error(response.data.message || 'Registration failed. Please try again.');
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Registration failed. Please check your details and try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[85vh] flex flex-col justify-center py-10 sm:px-6 lg:px-8 bg-gray-50">
      <div className="sm:mx-auto sm:w-full sm:max-w-xl text-center">
        <h2 className="text-center text-4xl font-extrabold text-blue-600 tracking-tight">
          Create Account
        </h2>
        <p className="mt-2 text-center text-gray-500 font-medium text-lg">
          Join SmartRide and start sharing journeys!
        </p>
      </div>

      <motion.div 
        initial={{ y: 20, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.5 }}
        className="mt-8 sm:mx-auto sm:w-full sm:max-w-xl relative"
      >
        <div className="bg-white py-10 px-6 sm:px-12 shadow-sm sm:rounded-3xl border border-gray-100 relative overflow-hidden">
          <AnimatePresence>
            {loading && (
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="absolute inset-0 z-10 flex flex-col items-center justify-center bg-white/80 backdrop-blur-sm"
              >
                <LoadingAnimation className="w-32 h-32" text="CREATING ACCOUNT..." />
              </motion.div>
            )}
          </AnimatePresence>
          <motion.form 
            className="space-y-6 relative z-0" 
            onSubmit={handleSubmit}
            initial="hidden"
            animate="visible"
            variants={{
              hidden: { opacity: 0 },
              visible: { opacity: 1, transition: { staggerChildren: 0.05 } }
            }}
          >
            
            <div className="grid grid-cols-1 gap-y-6 sm:grid-cols-2 sm:gap-x-8">
              <motion.div 
                className="sm:col-span-2"
                variants={{ hidden: { opacity: 0, y: 10 }, visible: { opacity: 1, y: 0 } }}
              >
                <label className="block text-sm font-semibold text-gray-700">Full Name</label>
                <div className="mt-2">
                  <input type="text" name="fullName" value={formData.fullName} onChange={handleChange} placeholder="John Doe" className="block w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors bg-transparent relative z-20" />
                </div>
              </motion.div>

              <motion.div 
                className="sm:col-span-2"
                variants={{ hidden: { opacity: 0, y: 10 }, visible: { opacity: 1, y: 0 } }}
              >
                <label className="block text-sm font-semibold text-gray-700">Email Address</label>
                <div className="mt-2">
                  <input type="email" name="email" value={formData.email} onChange={handleChange} placeholder="name@example.com" className="block w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors bg-transparent relative z-20" />
                </div>
              </motion.div>

              <motion.div variants={{ hidden: { opacity: 0, y: 10 }, visible: { opacity: 1, y: 0 } }}>
                <label className="block text-sm font-semibold text-gray-700">Phone Number</label>
                <div className="mt-2">
                  <input type="tel" name="phone" value={formData.phone} onChange={handleChange} placeholder="9876543210" maxLength="10" className="block w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors bg-transparent relative z-20" />
                </div>
              </motion.div>

              <motion.div variants={{ hidden: { opacity: 0, y: 10 }, visible: { opacity: 1, y: 0 } }}>
                <label className="block text-sm font-semibold text-gray-700">Password</label>
                <div className="mt-2">
                  <input type="password" name="password" value={formData.password} onChange={handleChange} placeholder="Min 8 characters" className="block w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors bg-transparent relative z-20" />
                </div>
              </motion.div>

              <motion.div 
                className="sm:col-span-2"
                variants={{ hidden: { opacity: 0, y: 10 }, visible: { opacity: 1, y: 0 } }}
              >
                <label className="block text-sm font-bold text-gray-900 bg-gray-50 py-3 px-4 rounded-xl border border-blue-100 flex flex-col sm:flex-row items-center justify-between gap-4">
                  <span>How will you normally use SmartRide?</span>
                  <select name="role" value={formData.role} onChange={handleChange} className="block w-full sm:w-auto px-4 py-2 border-2 border-blue-200 bg-white rounded-lg shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 font-semibold text-blue-800">
                    <option value="PASSENGER">Find Rides (Passenger)</option>
                    <option value="DRIVER">Offer Rides (Driver)</option>
                  </select>
                </label>
              </motion.div>

              <AnimatePresence>
                {formData.role === 'DRIVER' && (
                  <motion.div 
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: 'auto' }}
                    exit={{ opacity: 0, height: 0 }}
                    transition={{ duration: 0.3 }}
                    className="sm:col-span-2 mt-4 border-t border-gray-200 pt-6 overflow-hidden"
                  >
                  <h3 className="text-xl font-bold text-gray-900 mb-6 flex items-center gap-2">🚗 Vehicle Details</h3>
                  <div className="grid grid-cols-1 gap-y-6 sm:grid-cols-2 sm:gap-x-8">
                    
                    <div>
                      <label className="block text-sm font-semibold text-gray-700">Car Model</label>
                      <div className="mt-2">
                        <input type="text" name="carModel" value={formData.carModel} onChange={handleChange} placeholder="e.g. Honda Civic" className="block w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500" />
                      </div>
                    </div>

                    <div>
                      <label className="block text-sm font-semibold text-gray-700">License Plate</label>
                      <div className="mt-2">
                        <input type="text" name="licensePlate" value={formData.licensePlate} onChange={handleChange} placeholder="MH-12-AB-1234" className="block w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 uppercase" />
                      </div>
                    </div>

                    <div>
                      <label className="block text-sm font-semibold text-gray-700">Vehicle Type</label>
                      <div className="mt-2">
                        <select name="vehicleType" value={formData.vehicleType} onChange={handleChange} className="block w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white">
                          <option value="Sedan">Sedan</option>
                          <option value="SUV">SUV</option>
                          <option value="Hatchback">Hatchback</option>
                          <option value="Van">Van</option>
                        </select>
                      </div>
                    </div>

                    <div>
                      <label className="block text-sm font-semibold text-gray-700">Capacity (Seats)</label>
                      <div className="mt-2">
                        <input type="number" name="vehicleCapacity" value={formData.vehicleCapacity} onChange={handleChange} min="1" max="8" placeholder="4" className="block w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500" />
                      </div>
                    </div>
                  </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>

            <motion.div 
              className="pt-4"
              variants={{ hidden: { opacity: 0, y: 10 }, visible: { opacity: 1, y: 0 } }}
            >
              <motion.button 
                whileHover={{ scale: 1.02 }} 
                whileTap={{ scale: 0.98 }}
                type="submit" 
                disabled={loading} 
                className="w-full flex justify-center py-4 px-4 border border-transparent rounded-xl shadow-sm text-lg font-bold text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all shadow-blue-200 relative z-20"
              >
                {loading ? 'Creating Account...' : 'Create Account'}
              </motion.button>
            </motion.div>
          </motion.form>

          <div className="mt-8 text-center text-sm text-gray-600">
            Already have an account?{' '}
            <Link to="/login" className="font-bold text-blue-600 hover:text-blue-500 transition-colors">
              Log in now
            </Link>
          </div>
        </div>
      </motion.div>
    </div>
  );
};

export default Register;
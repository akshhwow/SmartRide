import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { rideAPI } from '../services/api';
import { toast } from 'react-toastify';

const PostRide = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    source: '',
    destination: '',
    rideDate: '',
    departureTime: '',
    seatsOffered: 1,
    baseFare: '',
    farePerKm: '',
    pricePerSeat: '',
    notes: '',
    distanceKm: ''
  });
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await rideAPI.postRide({
        ...formData,
        seatsOffered: parseInt(formData.seatsOffered),
        baseFare: parseFloat(formData.baseFare),
        farePerKm: parseFloat(formData.farePerKm),
        pricePerSeat: parseFloat(formData.pricePerSeat),
        distanceKm: formData.distanceKm ? parseFloat(formData.distanceKm) : null
      });

      toast.success(response.data.message);
      navigate('/my-rides');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to post ride');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-gray-50 min-h-screen py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-3xl mx-auto">
        <div className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden">
          
          <div className="bg-blue-600 px-8 py-10 text-center relative overflow-hidden">
            <div className="absolute top-0 right-0 -mt-10 -mr-10 opacity-10">
               <svg className="w-48 h-48" fill="currentColor" viewBox="0 0 24 24"><path d="M19 19H5V8h14m-3-7v2H8V1H6v2H5c-1.11 0-2 .89-2 2v14a2 2 0 002 2h14a2 2 0 002-2V5a2 2 0 00-2-2h-1V1m-1 11h-5v5h5v-5z"></path></svg>
            </div>
            <h2 className="relative text-4xl font-extrabold text-white tracking-tight mb-2">🚗 Post a Ride</h2>
            <p className="relative text-blue-100 text-lg font-medium">Share your journey and earn money</p>
          </div>

          <form onSubmit={handleSubmit} className="p-8 sm:p-10 space-y-8">
            {/* Route Details */}
            <div>
              <h3 className="text-xl font-bold text-gray-900 mb-4 border-b pb-2 border-gray-100">🛣️ Route Details</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">From (Source)</label>
                  <input
                    type="text"
                    name="source"
                    value={formData.source}
                    onChange={handleChange}
                    required
                    placeholder="e.g. Mumbai"
                    className="w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">To (Destination)</label>
                  <input
                    type="text"
                    name="destination"
                    value={formData.destination}
                    onChange={handleChange}
                    required
                    placeholder="e.g. Pune"
                    className="w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
              </div>
            </div>

            {/* Timings */}
            <div>
              <h3 className="text-xl font-bold text-gray-900 mb-4 border-b pb-2 border-gray-100">📅 Schedule</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">Date</label>
                  <input
                    type="date"
                    name="rideDate"
                    value={formData.rideDate}
                    onChange={handleChange}
                    required
                    min={new Date().toISOString().split('T')[0]}
                    className="w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white"
                  />
                </div>
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">Departure Time</label>
                  <input
                    type="time"
                    name="departureTime"
                    value={formData.departureTime}
                    onChange={handleChange}
                    required
                    className="w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white"
                  />
                </div>
              </div>
            </div>

            {/* Pricing Details */}
            <div>
              <h3 className="text-xl font-bold text-gray-900 mb-4 border-b pb-2 border-gray-100">💰 Pricing & Seats</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">Seats Available</label>
                  <input
                    type="number"
                    name="seatsOffered"
                    value={formData.seatsOffered}
                    onChange={handleChange}
                    required
                    min="1"
                    max="8"
                    className="w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">Base Fare (₹)</label>
                  <input
                    type="number"
                    name="baseFare"
                    value={formData.baseFare}
                    onChange={handleChange}
                    required
                    min="0"
                    step="0.01"
                    placeholder="50"
                    className="w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">Fare per KM (₹)</label>
                  <input
                    type="number"
                    name="farePerKm"
                    value={formData.farePerKm}
                    onChange={handleChange}
                    required
                    min="0"
                    step="0.01"
                    placeholder="12"
                    className="w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2 text-blue-600">Price / Seat (₹)</label>
                  <input
                    type="number"
                    name="pricePerSeat"
                    value={formData.pricePerSeat}
                    onChange={handleChange}
                    required
                    min="1"
                    step="0.01"
                    placeholder="500"
                    className="w-full px-4 py-3 border-2 border-blue-400 bg-blue-50 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-600 focus:border-blue-600 font-bold text-gray-900"
                  />
                </div>
              </div>
            </div>

            {/* Extra Info */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-2">Distance (km) - Optional</label>
                <input
                  type="number"
                  name="distanceKm"
                  value={formData.distanceKm}
                  onChange={handleChange}
                  min="1"
                  placeholder="e.g. 150"
                  className="w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-2">Additional Notes (Optional)</label>
                <textarea
                  name="notes"
                  value={formData.notes}
                  onChange={handleChange}
                  placeholder="E.g., No smoking, AC available..."
                  rows="2"
                  className="w-full px-4 py-3 border border-gray-300 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-none"
                />
              </div>
            </div>

            <div className="pt-4 mt-8 border-t border-gray-100">
              <button
                type="submit"
                disabled={loading}
                className="w-full flex justify-center py-4 px-4 border border-transparent rounded-xl shadow-sm text-xl font-bold text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all shadow-blue-200"
              >
                {loading ? '⏳ Posting...' : '✅ Post Your Ride'}
              </button>
            </div>
          </form>

        </div>
      </div>
    </div>
  );
};

export default PostRide;
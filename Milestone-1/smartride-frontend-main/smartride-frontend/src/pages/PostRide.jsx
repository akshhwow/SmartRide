import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { rideAPI } from '../services/api';
import { toast } from 'react-toastify';
import './Auth.css';

const PostRide = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    source: '',
    destination: '',
    rideDate: '',
    departureTime: '',
    seatsOffered: 1,
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
    <div className="auth-container">
      <div className="auth-card" style={{ maxWidth: '600px' }}>
        <h2 className="auth-title">🚗 Post a Ride</h2>
        <p className="auth-subtitle">Share your journey and earn money</p>

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label>📍 From (Source)</label>
            <input
              type="text"
              name="source"
              value={formData.source}
              onChange={handleChange}
              required
              placeholder="Mumbai"
            />
          </div>

          <div className="form-group">
            <label>📍 To (Destination)</label>
            <input
              type="text"
              name="destination"
              value={formData.destination}
              onChange={handleChange}
              required
              placeholder="Pune"
            />
          </div>

          <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label>📅 Date</label>
              <input
                type="date"
                name="rideDate"
                value={formData.rideDate}
                onChange={handleChange}
                required
                min={new Date().toISOString().split('T')[0]}
              />
            </div>

            <div className="form-group">
              <label>🕐 Departure Time</label>
              <input
                type="time"
                name="departureTime"
                value={formData.departureTime}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label>💺 Seats Available</label>
              <input
                type="number"
                name="seatsOffered"
                value={formData.seatsOffered}
                onChange={handleChange}
                required
                min="1"
                max="8"
              />
            </div>

            <div className="form-group">
              <label>💰 Price per Seat (₹)</label>
              <input
                type="number"
                name="pricePerSeat"
                value={formData.pricePerSeat}
                onChange={handleChange}
                required
                min="1"
                step="0.01"
                placeholder="500"
              />
            </div>
          </div>

          <div className="form-group">
            <label>📏 Distance (km) - Optional</label>
            <input
              type="number"
              name="distanceKm"
              value={formData.distanceKm}
              onChange={handleChange}
              min="1"
              placeholder="150"
            />
          </div>

          <div className="form-group">
            <label>💡 Additional Notes (Optional)</label>
            <textarea
              name="notes"
              value={formData.notes}
              onChange={handleChange}
              placeholder="E.g., No smoking, AC available, pet friendly..."
              rows="3"
              style={{ 
                resize: 'vertical', 
                padding: '0.75rem', 
                borderRadius: '8px', 
                border: '2px solid #e5e7eb',
                fontFamily: 'inherit',
                fontSize: '1rem'
              }}
            />
          </div>

          <button type="submit" className="auth-button" disabled={loading}>
            {loading ? '⏳ Posting...' : '✅ Post Ride'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default PostRide;
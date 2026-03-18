import React, { useState, useEffect } from 'react';
import { rideAPI, bookingAPI } from '../services/api';
import { toast } from 'react-toastify';
import RideControlPanel from '../components/driver/RideControlPanel';
import './MyRides.css';

const MyRides = () => {
  const [rides, setRides] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedRide, setSelectedRide] = useState(null);
  const [bookings, setBookings] = useState([]);

  useEffect(() => {
    fetchMyRides();
  }, []);

  const fetchMyRides = async () => {
    try {
      const response = await rideAPI.getMyRides();
      setRides(response.data.data || []);
    } catch (error) {
      toast.error('Failed to load rides');
    } finally {
      setLoading(false);
    }
  };

  const viewBookings = async (rideId) => {
    try {
      const response = await bookingAPI.getRideBookings(rideId);
      setBookings(response.data.data || []);
      setSelectedRide(rideId);
    } catch (error) {
      toast.error('Failed to load bookings');
    }
  };

  const closeModal = () => {
    setSelectedRide(null);
    setBookings([]);
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Loading your rides...</p>
      </div>
    );
  }

  return (
    <div className="my-rides-container">
      <div className="my-rides-card">
        <div className="page-header">
          <h2>🚗 My Posted Rides</h2>
          <p className="page-subtitle">Manage your ride offerings</p>
        </div>

        {rides.length === 0 ? (
          <div className="no-data">
            <div className="no-data-icon">🚗</div>
            <h3>No Rides Posted Yet</h3>
            <p>Start earning by posting your first ride!</p>
            <a href="/post-ride" className="cta-link">+ Post Your First Ride</a>
          </div>
        ) : (
          <div className="rides-grid">
            {rides.map((ride) => (
              <div key={ride.id} className="my-ride-card">
                <div className="ride-status-badge" data-status={ride.status || 'ACTIVE'}>
                  {ride.status || 'ACTIVE'}
                </div>

                <div className="ride-route-header">
                  <span>{ride.source}</span>
                  <span className="arrow">→</span>
                  <span>{ride.destination}</span>
                </div>

                <div className="ride-info-grid">
                  <div className="info-item">
                    <span className="icon">📅</span>
                    <div>
                      <div className="info-label">Date</div>
                      <div className="info-value">{ride.rideDate}</div>
                    </div>
                  </div>
                  <div className="info-item">
                    <span className="icon">🕐</span>
                    <div>
                      <div className="info-label">Time</div>
                      <div className="info-value">{ride.departureTime}</div>
                    </div>
                  </div>
                  <div className="info-item">
                    <span className="icon">💺</span>
                    <div>
                      <div className="info-label">Seats</div>
                      <div className="info-value">{ride.availableSeats}/{ride.seatsOffered}</div>
                    </div>
                  </div>
                  <div className="info-item">
                    <span className="icon">💰</span>
                    <div>
                      <div className="info-label">Price</div>
                      <div className="info-value">₹{ride.pricePerSeat}</div>
                    </div>
                  </div>
                </div>

                {ride.notes && (
                  <div className="ride-notes-small">
                    <strong>💡 Notes:</strong> {ride.notes}
                  </div>
                )}

                <div className="ride-stats">
                  <div className="stat">
                    <span className="stat-value">{ride.seatsOffered - ride.availableSeats}</span>
                    <span className="stat-label">Booked</span>
                  </div>
                  <div className="stat">
                    <span className="stat-value">₹{(ride.seatsOffered - ride.availableSeats) * ride.pricePerSeat}</span>
                    <span className="stat-label">Earnings</span>
                  </div>
                </div>

                <button
                  onClick={() => viewBookings(ride.id)}
                  className="view-bookings-btn"
                >
                  👥 View Bookings ({ride.seatsOffered - ride.availableSeats})
                </button>
                
                {/* NEW: Ride Control Panel for Drivers */}
                <RideControlPanel 
                  ride={ride} 
                  onUpdate={(updatedRide) => {
                    setRides(prev => prev.map(r => r.id === ride.id ? updatedRide : r));
                  }}
                />
              </div>
            ))}
          </div>
        )}
      </div>

      {selectedRide && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <button className="modal-close" onClick={closeModal}>×</button>
            
            <h3>👥 Passenger Bookings</h3>

            {bookings.length === 0 ? (
              <div className="no-bookings">
                <p>No bookings yet for this ride</p>
              </div>
            ) : (
              <div className="bookings-list">
                {bookings.map((booking) => (
                  <div key={booking.bookingId} className="booking-item">
                    <div className="booking-header">
                      <div className="passenger-info">
                        <span className="passenger-icon">👤</span>
                        <strong>{booking.passengerName}</strong>
                      </div>
                      <span className="booking-status" data-status={booking.status}>
                        {booking.status}
                      </span>
                    </div>
                    <div className="booking-details">
                      <div className="booking-detail-row">
                        <span className="detail-icon">💺</span>
                        <span><strong>Seats:</strong> {booking.seatsBooked}</span>
                      </div>
                      <div className="booking-detail-row">
                        <span className="detail-icon">💰</span>
                        <span><strong>Fare:</strong> ₹{booking.totalFare}</span>
                      </div>
                      {booking.pickupLocation && (
                        <div className="booking-detail-row">
                          <span className="detail-icon">📍</span>
                          <span><strong>Pickup:</strong> {booking.pickupLocation}</span>
                        </div>
                      )}
                      {booking.dropLocation && (
                        <div className="booking-detail-row">
                          <span className="detail-icon">📍</span>
                          <span><strong>Drop:</strong> {booking.dropLocation}</span>
                        </div>
                      )}
                      {booking.passengerNotes && (
                        <div className="passenger-notes">
                          <strong>💬 Note:</strong> {booking.passengerNotes}
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default MyRides;
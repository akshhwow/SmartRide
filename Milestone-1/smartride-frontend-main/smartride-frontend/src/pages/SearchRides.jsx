import React, { useState } from 'react';
import { rideAPI, bookingAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-toastify';
import './SearchRides.css';

const SearchRides = () => {
  const { isAuthenticated, isPassenger } = useAuth();
  const [searchParams, setSearchParams] = useState({
    source: '',
    destination: '',
    date: '',
    seats: 1
  });
  const [rides, setRides] = useState([]);
  const [loading, setLoading] = useState(false);
  const [bookingRideId, setBookingRideId] = useState(null);

  const handleSearch = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await rideAPI.searchRides(searchParams);
      setRides(response.data.data || []);
      toast.success(response.data.message);
    } catch (error) {
      toast.error('Failed to search rides');
    } finally {
      setLoading(false);
    }
  };

  const handleBook = async (rideId) => {
    if (!isAuthenticated()) {
      toast.info('Please login to book rides');
      return;
    }

    if (!isPassenger()) {
      toast.error('Only passengers can book rides');
      return;
    }

    const seatsToBook = prompt('How many seats do you want to book?', '1');
    if (!seatsToBook) return;

    setBookingRideId(rideId);

    try {
      const response = await bookingAPI.bookRide({
        rideId,
        seatsToBook: parseInt(seatsToBook),
        pickupLocation: searchParams.source,
        dropLocation: searchParams.destination
      });

      toast.success(response.data.message);
      handleSearch(new Event('submit'));
    } catch (error) {
      toast.error(error.response?.data?.message || 'Booking failed');
    } finally {
      setBookingRideId(null);
    }
  };

  return (
    <div className="search-container">
      <div className="search-card">
        <h2>🔍 Find Your Perfect Ride</h2>
        <p className="search-subtitle">Search thousands of rides and travel affordably</p>

        <form onSubmit={handleSearch} className="search-form">
          <div className="form-row">
            <div className="form-group">
              <label>📍 From</label>
              <input
                type="text"
                value={searchParams.source}
                onChange={(e) => setSearchParams({...searchParams, source: e.target.value})}
                required
                placeholder="Mumbai"
              />
            </div>

            <div className="form-group">
              <label>📍 To</label>
              <input
                type="text"
                value={searchParams.destination}
                onChange={(e) => setSearchParams({...searchParams, destination: e.target.value})}
                required
                placeholder="Pune"
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>📅 Date</label>
              <input
                type="date"
                value={searchParams.date}
                onChange={(e) => setSearchParams({...searchParams, date: e.target.value})}
                min={new Date().toISOString().split('T')[0]}
              />
            </div>

            <div className="form-group">
              <label>👥 Seats</label>
              <input
                type="number"
                value={searchParams.seats}
                onChange={(e) => setSearchParams({...searchParams, seats: e.target.value})}
                min="1"
                max="8"
              />
            </div>
          </div>

          <button type="submit" className="search-button" disabled={loading}>
            {loading ? '🔄 Searching...' : '🔍 Search Rides'}
          </button>
        </form>

        {rides.length > 0 && (
          <div className="rides-list">
            <h3>✨ {rides.length} Ride{rides.length > 1 ? 's' : ''} Found</h3>
            
            {rides.map((ride) => (
              <div key={ride.id} className="ride-card">
                <div className="ride-header">
                  <div className="ride-route">
                    <span className="location">{ride.source}</span>
                    <span className="arrow">→</span>
                    <span className="location">{ride.destination}</span>
                  </div>
                  <div className="ride-price">₹{ride.pricePerSeat}/seat</div>
                </div>

                <div className="ride-details">
                  <div className="detail-item">
                    <span className="icon">👤</span>
                    <span>{ride.driverName}</span>
                  </div>
                  <div className="detail-item">
                    <span className="icon">📅</span>
                    <span>{ride.rideDate}</span>
                  </div>
                  <div className="detail-item">
                    <span className="icon">🕐</span>
                    <span>{ride.departureTime}</span>
                  </div>
                  <div className="detail-item">
                    <span className="icon">💺</span>
                    <span>{ride.availableSeats} seats</span>
                  </div>
                  <div className="detail-item">
                    <span className="icon">🚗</span>
                    <span>{ride.driverVehicleType}</span>
                  </div>
                  {ride.distanceKm && (
                    <div className="detail-item">
                      <span className="icon">📏</span>
                      <span>{ride.distanceKm} km</span>
                    </div>
                  )}
                </div>

                {ride.notes && (
                  <div className="ride-notes">
                    <strong>💡 Notes:</strong> {ride.notes}
                  </div>
                )}

                <button
                  onClick={() => handleBook(ride.id)}
                  className="book-button"
                  disabled={bookingRideId === ride.id}
                >
                  {bookingRideId === ride.id ? '⏳ Booking...' : '✅ Book Now'}
                </button>
              </div>
            ))}
          </div>
        )}

        {rides.length === 0 && !loading && (
          <div className="no-rides">
            <div className="no-rides-icon">🔍</div>
            <p>No rides found matching your search</p>
            <p className="no-rides-hint">Try different locations or dates</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default SearchRides;
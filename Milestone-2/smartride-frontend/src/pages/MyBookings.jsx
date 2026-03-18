import React, { useState, useEffect } from 'react';
import { bookingAPI, paymentAPI } from '../services/api';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';
import RideStatusTracker from '../components/passenger/RideStatusTracker';
import './MyRides.css';

const MyBookings = () => {
  const { user } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cancellingId, setCancellingId] = useState(null);
  const [payingBookingId, setPayingBookingId] = useState(null);

  const loadRazorpayScript = () => {
    return new Promise((resolve) => {
      if (window.Razorpay) {
        resolve(true);
        return;
      }

      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.onload = () => resolve(true);
      script.onerror = () => resolve(false);
      document.body.appendChild(script);
    });
  };

  const handlePayment = async (booking) => {
    setPayingBookingId(booking.bookingId);

    try {
      const response = await paymentAPI.createOrder({
        bookingId: booking.bookingId,
        amount: booking.totalFare,
      });

      const order = response.data.data;
      const loaded = await loadRazorpayScript();
      if (!loaded) {
        throw new Error('Failed to load payment gateway script');
      }

      const options = {
        key: order.keyId,
        amount: order.amount,
        currency: order.currency,
        name: 'SmartRide',
        description: `Payment for booking #${booking.bookingId}`,
        order_id: order.orderId,
        handler: async (paymentResponse) => {
          try {
            await paymentAPI.verifyPayment({
              bookingId: booking.bookingId,
              orderId: paymentResponse.razorpay_order_id,
              paymentId: paymentResponse.razorpay_payment_id,
              signature: paymentResponse.razorpay_signature,
            });

            toast.success('Payment successful! Booking is now confirmed.');
            fetchMyBookings();
          } catch (verifyError) {
            toast.error(verifyError.response?.data?.message || 'Payment verification failed');
          } finally {
            setPayingBookingId(null);
          }
        },
        prefill: {
          name: user?.name || '',
          email: user?.email || '',
          contact: user?.phone || '',
        },
        theme: {
          color: '#3399cc',
        },
        modal: {
          ondismiss: () => setPayingBookingId(null),
        },
      };

      const rzp = new window.Razorpay(options);
      rzp.open();
    } catch (error) {
      toast.error(error.response?.data?.message || error.message || 'Payment initialization failed');
      setPayingBookingId(null);
    }
  };

  useEffect(() => {
    fetchMyBookings();
  }, []);

  const fetchMyBookings = async () => {
    try {
      const response = await bookingAPI.getMyBookings();
      setBookings(response.data.data || []);
    } catch (error) {
      toast.error('Failed to load bookings');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (bookingId) => {
    if (!window.confirm('Are you sure you want to cancel this booking?')) {
      return;
    }

    setCancellingId(bookingId);
    try {
      const response = await bookingAPI.cancelBooking(bookingId);
      toast.success(response.data.message);
      fetchMyBookings();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to cancel booking');
    } finally {
      setCancellingId(null);
    }
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Loading your bookings...</p>
      </div>
    );
  }

  return (
    <div className="my-rides-container">
      <div className="my-rides-card">
        <div className="page-header">
          <h2>🎫 My Bookings</h2>
          <p className="page-subtitle">Track your ride bookings</p>
        </div>

        {bookings.length === 0 ? (
          <div className="no-data">
            <div className="no-data-icon">🎫</div>
            <h3>No Bookings Yet</h3>
            <p>Start your journey by booking a ride!</p>
            <a href="/search" className="cta-link">🔍 Search for Rides</a>
          </div>
        ) : (
          <div className="rides-grid">
            {bookings.map((booking) => (
              <div key={booking.bookingId} className="my-ride-card">
                <div className="ride-status-badge" data-status={booking.status}>
                  {booking.status}
                </div>

                <div className="ride-route-header">
                  <span>{booking.source}</span>
                  <span className="arrow">→</span>
                  <span>{booking.destination}</span>
                </div>

                <div className="ride-info-grid">
                  <div className="info-item">
                    <span className="icon">📅</span>
                    <div>
                      <div className="info-label">Date</div>
                      <div className="info-value">{booking.rideDate}</div>
                    </div>
                  </div>
                  <div className="info-item">
                    <span className="icon">🕐</span>
                    <div>
                      <div className="info-label">Time</div>
                      <div className="info-value">{booking.departureTime}</div>
                    </div>
                  </div>
                  <div className="info-item">
                    <span className="icon">💺</span>
                    <div>
                      <div className="info-label">Seats</div>
                      <div className="info-value">{booking.seatsBooked}</div>
                    </div>
                  </div>
                  <div className="info-item">
                    <span className="icon">💰</span>
                    <div>
                      <div className="info-label">Total Fare</div>
                      <div className="info-value">₹{booking.totalFare}</div>
                    </div>
                  </div>
                </div>

                {booking.driverName && (
                  <div className="ride-notes-small">
                    <strong>🚗 Driver:</strong> {booking.driverName}
                  </div>
                )}

                {booking.pickupLocation && (
                  <div className="booking-location">
                    <span className="location-icon">📍</span>
                    <div>
                      <div className="location-label">Pickup</div>
                      <div className="location-value">{booking.pickupLocation}</div>
                    </div>
                  </div>
                )}

                {booking.dropLocation && (
                  <div className="booking-location">
                    <span className="location-icon">📍</span>
                    <div>
                      <div className="location-label">Drop</div>
                      <div className="location-value">{booking.dropLocation}</div>
                    </div>
                  </div>
                )}

                {booking.paymentStatus === 'PENDING' && (
                  <div className="payment-status">
                    <strong>💳 Payment:</strong> Pending
                  </div>
                )}

                {booking.paymentStatus === 'PAID' && (
                  <div className="payment-status paid">
                    <strong>✅ Payment:</strong> Completed
                  </div>
                )}

                {booking.paymentStatus === 'PENDING' && (
                  <button
                    onClick={() => handlePayment(booking)}
                    className="pay-now-btn"
                    disabled={payingBookingId === booking.bookingId}
                  >
                    {payingBookingId === booking.bookingId ? '⏳ Processing...' : '💳 Pay Now'}
                  </button>
                )}

                {(booking.status === 'CONFIRMED' || booking.status === 'PENDING') && (
                  <button
                    onClick={() => handleCancel(booking.bookingId)}
                    className="cancel-booking-btn"
                    disabled={cancellingId === booking.bookingId}
                  >
                    {cancellingId === booking.bookingId ? '⏳ Cancelling...' : '❌ Cancel Booking'}
                  </button>
                )}

                {booking.status === 'COMPLETED' && (
                  <button className="rate-btn" onClick={() => toast.info('Rating feature coming soon!')}>
                    ⭐ Rate Driver
                  </button>
                )}

                {/* NEW: Live Status Tracker for Passengers */}
                <RideStatusTracker booking={booking} />
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default MyBookings;
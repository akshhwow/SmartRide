import React, { useState, useEffect } from 'react';
import { bookingAPI, paymentAPI } from '../services/api';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';
import RideStatusTracker from '../components/passenger/RideStatusTracker';
import ReviewModal from '../components/shared/ReviewModal';
import { subscribeToNotifications } from '../services/socketService';
import { motion } from 'framer-motion';
import EmptyAnimation from '../components/animations/EmptyAnimation';

const MyBookings = () => {
  const { user } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cancellingId, setCancellingId] = useState(null);
  const [payingBookingId, setPayingBookingId] = useState(null);
  const [activeTab, setActiveTab] = useState('upcoming');
  
  // Review Modal State
  const [isReviewOpen, setIsReviewOpen] = useState(false);
  const [reviewTarget, setReviewTarget] = useState(null); // { rideId, driverId }

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
      if (!loaded) throw new Error('Failed to load payment gateway script');

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
          color: '#2563eb', // text-blue-600
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

    const unsubscribe = subscribeToNotifications((notification) => {
      const relevantEvents = ['RIDE_ACCEPTED', 'RIDE_STARTED', 'RIDE_ENDED', 'PAYMENT_CONFIRMED', 'DRIVER_CANCELLED'];
      if (relevantEvents.includes(notification.type)) {
        fetchMyBookings();
      }
    });

    return () => unsubscribe();
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
    if (!window.confirm('Are you sure you want to cancel this booking?')) return;
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

  const upcomingBookings = bookings.filter(b => b.status && ['PENDING', 'CONFIRMED'].includes(b.status.toUpperCase()));
  const completedBookings = bookings.filter(b => b.status && ['COMPLETED', 'CANCELLED'].includes(b.status.toUpperCase()));
  const displayBookings = activeTab === 'upcoming' ? upcomingBookings : completedBookings;

  if (loading) {
    return (
      <div className="bg-gray-50 min-h-screen py-8">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mb-8">
            <div className="h-10 bg-gray-200 rounded w-1/3 mb-2 animate-pulse"></div>
            <div className="h-6 bg-gray-200 rounded w-1/4 animate-pulse"></div>
          </div>
          <div className="grid gap-6 grid-cols-1 md:grid-cols-2">
            {[1, 2, 3, 4].map(i => (
              <div key={i} className="bg-white rounded-2xl h-64 border border-gray-100 animate-pulse flex flex-col p-5">
                <div className="flex justify-between mb-4">
                   <div className="w-20 h-6 bg-gray-200 rounded-full"></div>
                   <div className="w-24 h-8 bg-gray-200 rounded"></div>
                </div>
                <div className="w-full h-12 bg-gray-100 rounded mb-4"></div>
                <div className="grid grid-cols-2 gap-4">
                   <div className="w-full h-10 bg-gray-100 rounded"></div>
                   <div className="w-full h-10 bg-gray-100 rounded"></div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-gray-50 min-h-screen py-8">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
        
        <div className="mb-8">
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">My Rides</h1>
          <p className="mt-2 text-gray-500 text-lg">Your upcoming and past trips at a glance.</p>
        </div>

        {/* Tabs */}
        <div className="flex space-x-1 bg-white p-1 rounded-xl shadow-sm border border-gray-100 max-w-sm mb-8">
          <button
            onClick={() => setActiveTab('upcoming')}
            className={`flex-1 py-2.5 px-4 rounded-lg font-medium text-sm transition-all duration-200 ${
              activeTab === 'upcoming' 
                ? 'bg-blue-600 text-white shadow' 
                : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'
            }`}
          >
            Upcoming
          </button>
          <button
            onClick={() => setActiveTab('completed')}
            className={`flex-1 py-2.5 px-4 rounded-lg font-medium text-sm transition-all duration-200 ${
              activeTab === 'completed' 
                ? 'bg-blue-600 text-white shadow' 
                : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'
            }`}
          >
            Past Rides
          </button>
        </div>

        {displayBookings.length === 0 ? (
          <motion.div 
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            className="bg-white rounded-2xl shadow-sm p-12 text-center border border-gray-100 flex flex-col items-center"
          >
            <EmptyAnimation className="w-48 h-48 mb-4 opacity-70" />
            <h3 className="text-xl font-bold text-gray-800 mb-2">No {activeTab} bookings</h3>
            <p className="text-gray-500 mb-6">You don't have any {activeTab} trips booked yet.</p>
            {activeTab === 'upcoming' && (
              <a href="/search" className="inline-block bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-6 rounded-xl transition-colors">
                🔍 Find a ride
              </a>
            )}
          </motion.div>
        ) : (
          <motion.div 
            initial="hidden"
            animate="visible"
            variants={{
              hidden: { opacity: 0 },
              visible: { opacity: 1, transition: { staggerChildren: 0.1 } }
            }}
            className="grid gap-6 grid-cols-1 md:grid-cols-2"
          >
            {displayBookings.map((booking) => (
              <motion.div 
                variants={{
                  hidden: { opacity: 0, y: 20 },
                  visible: { opacity: 1, y: 0 }
                }}
                whileHover={{ y: -5 }}
                key={booking.bookingId} 
                className="bg-white rounded-2xl shadow-sm border border-gray-100 flex flex-col hover:shadow-md transition-shadow overflow-hidden"
              >
                <div className="p-5 flex-1">
                  <div className="flex justify-between items-start mb-4">
                    <span className={`px-2.5 py-1 text-xs font-semibold rounded-full ${
                      booking.status === 'CONFIRMED' ? 'bg-green-100 text-green-700' :
                      booking.status === 'CANCELLED' ? 'bg-red-100 text-red-700' :
                      booking.status === 'COMPLETED' ? 'bg-blue-100 text-blue-700' :
                      'bg-orange-100 text-orange-700'
                    }`}>
                      {booking.status}
                    </span>
                    <div className="text-right">
                      <div className="text-xs text-gray-400 mb-0.5 font-bold tracking-tight">
                        (₹{(Number(booking.totalFare || 0) / (booking.seatsBooked || 1)).toFixed(2)} × {booking.seatsBooked} seats)
                      </div>
                      <div className="font-extrabold text-2xl text-gray-900 leading-none">₹{Number(booking.totalFare || 0).toFixed(2)}</div>
                      <div className="text-xs font-bold text-gray-500 mt-1 uppercase tracking-wide">
                        {booking.paymentStatus === 'PAID' ? (
                          <span className="text-green-600 inline-flex items-center gap-1">✅ Paid</span>
                        ) : (
                          <span className="text-orange-500">Payment Pending</span>
                        )}
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-3 mb-6">
                    <div className="flex flex-col items-center">
                      <span className="w-2.5 h-2.5 rounded-full bg-blue-500"></span>
                      <span className="w-0.5 h-6 bg-gray-200"></span>
                      <span className="w-2.5 h-2.5 rounded-full border-2 border-blue-500 bg-white"></span>
                    </div>
                    <div className="flex flex-col gap-3 font-semibold text-gray-800">
                      <div>{booking.source}</div>
                      <div>{booking.destination}</div>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-y-4 mb-4 text-sm bg-gray-50 p-3 rounded-xl border border-gray-100 shadow-inner">
                    <div>
                      <div className="text-gray-400 text-xs font-bold uppercase tracking-wider mb-1">Date & Time</div>
                      <div className="font-bold text-gray-800">{booking.rideDate} at {booking.departureTime}</div>
                    </div>
                    <div>
                      <div className="text-gray-400 text-xs font-bold uppercase tracking-wider mb-1">Driver</div>
                      <div className="font-bold text-gray-800">{booking.driverName || 'N/A'}</div>
                    </div>
                    <div className="col-span-2">
                       <div className="text-gray-400 text-xs font-bold uppercase tracking-wider mb-1">Pickup Information</div>
                       <div className="font-bold text-gray-800 truncate">{booking.pickupLocation || 'Default Pickup Location'}</div>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="space-y-3 mt-4">
                    {booking.status === 'PENDING' && !['ACCEPTED', 'IN_PROGRESS', 'COMPLETED'].includes(booking.rideStatus) && (
                      <div className="w-full bg-orange-50 border border-orange-200 text-orange-700 font-bold py-3 rounded-xl text-center flex items-center justify-center gap-2">
                        ⏳ Waiting for Driver to Accept...
                      </div>
                    )}

                    {booking.status === 'PENDING' && ['ACCEPTED', 'IN_PROGRESS', 'COMPLETED'].includes(booking.rideStatus) && (
                      <button
                        onClick={() => handlePayment(booking)}
                        className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-3 rounded-xl transition-all shadow-md shadow-green-200 flex items-center justify-center gap-2"
                        disabled={payingBookingId === booking.bookingId}
                      >
                        {payingBookingId === booking.bookingId ? '⏳ Processing...' : '💳 Pay Now Securely'}
                      </button>
                    )}

                    {(booking.status === 'CONFIRMED' || booking.status === 'PENDING') && (
                      <button
                        onClick={() => handleCancel(booking.bookingId)}
                        className="w-full bg-white border-2 border-red-100 text-red-500 hover:bg-red-50 hover:border-red-200 font-bold py-3 rounded-xl transition-all"
                        disabled={cancellingId === booking.bookingId}
                      >
                        {cancellingId === booking.bookingId ? '⏳ Cancelling...' : '❌ Cancel Booking'}
                      </button>
                    )}

                    {booking.status === 'COMPLETED' && (
                      <button 
                        className="w-full bg-yellow-50 border border-yellow-200 text-yellow-700 hover:bg-yellow-100 font-medium py-2.5 rounded-xl transition-colors flex items-center justify-center gap-2"
                        onClick={() => {
                          if (!booking.driverId) {
                            toast.info("Please wait for the server to load the Driver's profile ID!");
                            return;
                          }
                          setReviewTarget({ rideId: booking.rideId, driverId: booking.driverId });
                          setIsReviewOpen(true);
                        }}
                      >
                        ⭐ Rate Driver
                      </button>
                    )}
                  </div>
                  
                  {activeTab === 'upcoming' && (
                    <div className="mt-4 pt-4 border-t border-gray-100">
                      <RideStatusTracker booking={booking} />
                    </div>
                  )}
                </div>
              </motion.div>
            ))}
          </motion.div>
        )}
      </div>

      {isReviewOpen && reviewTarget && (
        <ReviewModal
          isOpen={isReviewOpen}
          onClose={() => setIsReviewOpen(false)}
          rideId={reviewTarget.rideId}
          revieweeId={reviewTarget.driverId}
          roleContext="PASSENGER"
        />
      )}
    </div>
  );
};

export default MyBookings;
import React, { useState, useEffect } from 'react';
import { rideAPI, bookingAPI } from '../services/api';
import { toast } from 'react-toastify';
import RideControlPanel from '../components/driver/RideControlPanel';
import ReviewModal from '../components/shared/ReviewModal';
import { motion } from 'framer-motion';
import EmptyAnimation from '../components/animations/EmptyAnimation';

const MyRides = () => {
  const [rides, setRides] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedRide, setSelectedRide] = useState(null);
  const [bookings, setBookings] = useState([]);
  const [activeTab, setActiveTab] = useState('upcoming');

  // Review Modal State
  const [isReviewOpen, setIsReviewOpen] = useState(false);
  const [reviewTarget, setReviewTarget] = useState(null); // { rideId, passengerId }

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

  const upcomingRides = rides.filter(r => r.status && !['COMPLETED', 'CANCELLED'].includes(r.status.toUpperCase()));
  const completedRides = rides.filter(r => r.status && ['COMPLETED', 'CANCELLED'].includes(r.status.toUpperCase()));
  const displayRides = activeTab === 'upcoming' ? upcomingRides : completedRides;

  if (loading) {
    return (
      <div className="bg-gray-50 min-h-screen py-8">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mb-8">
            <div className="h-10 bg-gray-200 rounded w-1/3 mb-2 animate-pulse"></div>
            <div className="h-6 bg-gray-200 rounded w-1/4 animate-pulse"></div>
          </div>
          <div className="grid gap-6 grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
            {[1, 2, 3].map(i => (
              <div key={i} className="bg-white rounded-2xl h-64 border border-gray-100 animate-pulse flex flex-col p-5">
                <div className="flex justify-between mb-4">
                   <div className="w-20 h-6 bg-gray-200 rounded-full"></div>
                   <div className="w-16 h-8 bg-gray-200 rounded"></div>
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
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">My Offered Rides</h1>
          <p className="mt-2 text-gray-500 text-lg">Manage the rides you've created.</p>
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

        {displayRides.length === 0 ? (
          <motion.div 
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            className="bg-white rounded-2xl shadow-sm p-12 text-center border border-gray-100 flex flex-col items-center"
          >
            <EmptyAnimation className="w-48 h-48 mb-4 opacity-70" />
            <h3 className="text-xl font-bold text-gray-800 mb-2">No {activeTab} rides found</h3>
            <p className="text-gray-500 mb-6">You don't have any {activeTab} rides at the moment.</p>
            {activeTab === 'upcoming' && (
              <a href="/post-ride" className="inline-block bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-6 rounded-xl transition-colors">
                + Offer a ride
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
            className="grid gap-6 grid-cols-1 md:grid-cols-2 lg:grid-cols-3"
          >
            {displayRides.map((ride) => {
              const bookedSeats = ride.seatsOffered - ride.availableSeats;
              const earnings = bookedSeats * Number(ride.pricePerSeat || 0);

              return (
                <motion.div 
                  variants={{
                    hidden: { opacity: 0, y: 20 },
                    visible: { opacity: 1, y: 0 }
                  }}
                  whileHover={{ y: -5 }}
                  key={ride.id} 
                  className="bg-white rounded-2xl shadow-sm border border-gray-100 flex flex-col hover:shadow-md transition-shadow overflow-hidden"
                >
                  <div className="p-5 flex-1">
                    <div className="flex justify-between items-start mb-4">
                      <span className={`px-2.5 py-1 text-xs font-semibold rounded-full ${
                        ride.status === 'COMPLETED' ? 'bg-green-100 text-green-700' :
                        ride.status === 'CANCELLED' ? 'bg-red-100 text-red-700' :
                        'bg-blue-100 text-blue-700'
                      }`}>
                        {ride.status || 'ACTIVE'}
                      </span>
                      <div className="text-right">
                        <div className="font-bold text-lg text-gray-900">₹{Number(ride.pricePerSeat || 0).toFixed(2)}</div>
                        <div className="text-xs text-gray-500 font-medium">per seat</div>
                      </div>
                    </div>

                    <div className="flex items-center gap-3 mb-6">
                      <div className="flex flex-col items-center">
                        <span className="w-2.5 h-2.5 rounded-full bg-blue-500"></span>
                        <span className="w-0.5 h-6 bg-gray-200"></span>
                        <span className="w-2.5 h-2.5 rounded-full border-2 border-blue-500 bg-white"></span>
                      </div>
                      <div className="flex flex-col gap-3 font-medium text-gray-800">
                        <div>{ride.source}</div>
                        <div>{ride.destination}</div>
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4 mb-2 text-sm bg-gray-50 p-3 rounded-xl border border-gray-100 shadow-inner">
                      <div>
                        <div className="text-gray-400 text-xs font-bold uppercase tracking-wider mb-1">Date</div>
                        <div className="font-bold text-gray-800">{ride.rideDate}</div>
                      </div>
                      <div>
                        <div className="text-gray-400 text-xs font-bold uppercase tracking-wider mb-1">Time</div>
                        <div className="font-bold text-gray-800">{ride.departureTime}</div>
                      </div>
                      <div>
                        <div className="text-gray-400 text-xs font-bold uppercase tracking-wider mb-1">Seats Booked</div>
                        <div className="font-bold text-gray-800">{bookedSeats} / {ride.seatsOffered}</div>
                      </div>
                      <div>
                         <div className="text-gray-400 text-xs font-bold uppercase tracking-wider mb-1">Est. Earnings</div>
                         <div className="text-xs text-gray-400 font-bold mb-0.5 tracking-tight">(₹{Number(ride.pricePerSeat || 0).toFixed(2)} × {bookedSeats} seats)</div>
                         <div className="font-extrabold text-lg text-green-600 leading-none">₹{earnings.toFixed(2)}</div>
                      </div>
                    </div>
                  </div>

                  <div className="bg-gray-50 p-4 border-t border-gray-100 space-y-3">
                    <button
                      onClick={() => viewBookings(ride.id)}
                      className="w-full flex items-center justify-center gap-2 bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 font-medium py-2 rounded-xl transition-colors text-sm shadow-sm"
                    >
                      👥 View Bookings ({bookedSeats})
                    </button>
                    
                    {activeTab === 'upcoming' && (
                      <RideControlPanel 
                        ride={ride} 
                        onUpdate={(updatedRide) => {
                          setRides(prev => prev.map(r => r.id === ride.id ? updatedRide : r));
                        }}
                      />
                    )}
                  </div>
                </motion.div>
              );
            })}
          </motion.div>
        )}
      </div>

      {/* Bookings Modal */}
      {selectedRide && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg max-h-[90vh] flex flex-col overflow-hidden animate-fade-in-up">
            <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50">
              <h3 className="text-lg font-bold text-gray-800 flex items-center gap-2">
                👥 Passenger Bookings
              </h3>
              <button 
                className="w-8 h-8 flex items-center justify-center rounded-full bg-gray-200 text-gray-600 hover:bg-gray-300 transition-colors"
                onClick={closeModal}
              >
                ✕
              </button>
            </div>
            
            <div className="p-6 overflow-y-auto">
              {bookings.length === 0 ? (
                <div className="text-center py-8">
                  <p className="text-gray-500">No bookings yet for this ride.</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {bookings.map((booking) => (
                    <div key={booking.bookingId} className="bg-white border border-gray-100 rounded-xl p-4 shadow-sm">
                      <div className="flex justify-between items-start mb-3">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 font-bold">
                            {booking.passengerName?.charAt(0) || 'P'}
                          </div>
                          <div>
                            <div className="font-semibold text-gray-800">{booking.passengerName}</div>
                            <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${
                              booking.status === 'CONFIRMED' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'
                            }`}>
                              {booking.status}
                            </span>
                          </div>
                        </div>
                      </div>

                      <div className="grid grid-cols-2 gap-3 text-sm mt-4">
                        <div className="bg-gray-50 p-2 rounded-lg">
                          <span className="text-gray-500 text-xs block">Seats Booked</span>
                          <span className="font-medium text-gray-800">{booking.seatsBooked}</span>
                        </div>
                        <div className="bg-gray-50 p-2 rounded-lg">
                          <span className="text-gray-500 text-xs block">Fare</span>
                          <span className="font-medium text-green-600">₹{Number(booking.totalFare || booking.fare || 0).toFixed(2)}</span>
                        </div>
                        {booking.pickupLocation && (
                          <div className="col-span-2 bg-gray-50 p-2 rounded-lg">
                            <span className="text-gray-500 text-xs block">Pickup</span>
                            <span className="font-medium text-gray-800">{booking.pickupLocation}</span>
                          </div>
                        )}
                        {booking.dropLocation && (
                          <div className="col-span-2 bg-gray-50 p-2 rounded-lg">
                            <span className="text-gray-500 text-xs block">Drop</span>
                            <span className="font-medium text-gray-800">{booking.dropLocation}</span>
                          </div>
                        )}
                      </div>
                      
                      {booking.passengerNotes && (
                        <div className="mt-3 p-3 bg-blue-50 text-blue-800 text-sm rounded-lg">
                          <span className="font-semibold block mb-1">Message:</span>
                          {booking.passengerNotes}
                        </div>
                      )}
                      
                      {booking.status === 'COMPLETED' && (
                        <div className="mt-4 pt-4 border-t border-gray-100">
                          <button
                            className="w-full bg-white border border-yellow-400 text-yellow-600 hover:bg-yellow-50 font-medium py-2 rounded-lg transition-colors text-sm"
                            onClick={() => {
                              if (!booking.passengerId) {
                                toast.info("Please refresh the page to load Passenger IDs securely!");
                                return;
                              }
                              setReviewTarget({ rideId: booking.rideId, passengerId: booking.passengerId });
                              setIsReviewOpen(true);
                            }}
                          >
                            ⭐ Rate Passenger
                          </button>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Review Modal */}
      {isReviewOpen && reviewTarget && (
        <ReviewModal
          isOpen={isReviewOpen}
          onClose={() => setIsReviewOpen(false)}
          rideId={reviewTarget.rideId}
          revieweeId={reviewTarget.passengerId}
          roleContext="DRIVER"
        />
      )}
    </div>
  );
};

export default MyRides;
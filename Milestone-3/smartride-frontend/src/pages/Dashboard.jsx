import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { rideAPI, reviewAPI } from '../services/api';
import { toast } from 'react-toastify';
import { motion } from 'framer-motion';

const Dashboard = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const promises = [];
        if (user?.role === 'DRIVER') {
          promises.push(rideAPI.getStatistics().then(res => setStats(res.data.data)));
        }
        if (user?.id) {
          promises.push(reviewAPI.getUserReviews(user.id).then(res => setReviews(res.data.data)));
        }
        await Promise.all(promises);
      } catch (error) {
        toast.error('Failed to load some dashboard data');
      } finally {
        setLoading(false);
      }
    };
    
    if (user) {
      fetchDashboardData();
    }
  }, [user]);

  return (
    <div className="bg-gray-50 min-h-screen py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
        
        <motion.div 
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="bg-white rounded-3xl p-8 shadow-sm border border-gray-100 flex flex-col md:flex-row justify-between items-start md:items-center gap-6"
        >
          <div>
            <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">
              Welcome back, {user?.fullName}! 👋
            </h1>
            <p className="mt-2 text-lg text-gray-500 font-medium">
              You are logged in as a <span className="text-blue-600 font-bold">{user?.role}</span>
            </p>
          </div>
          <motion.div 
            whileHover={{ scale: 1.05 }}
            className="bg-orange-50 text-orange-700 px-6 py-3 rounded-2xl flex items-center gap-3 border border-orange-100 shadow-sm"
          >
            <span className="text-2xl animate-bounce">⭐</span>
            <div className="flex flex-col">
              <span className="font-bold text-lg leading-tight">
                {user?.rating ? Number(user.rating).toFixed(1) : 'New'}
              </span>
              <span className="text-xs font-semibold opacity-80">
                {user?.totalRatings || 0} Ratings
              </span>
            </div>
          </motion.div>
        </motion.div>

        {/* Stats for Driver */}
        {user?.role === 'DRIVER' && stats && (
          <motion.div 
            initial="hidden"
            animate="visible"
            variants={{
              hidden: { opacity: 0 },
              visible: { opacity: 1, transition: { staggerChildren: 0.1 } }
            }}
            className="grid grid-cols-2 md:grid-cols-4 gap-4"
          >
            {[
              { label: 'Total Earnings', value: `₹${Number(stats.totalEarnings || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`, color: 'text-green-600' },
              { label: 'Completed Rides', value: stats.completedRides || 0, color: 'text-blue-600' },
              { label: 'Active Rides', value: stats.activeRides || 0, color: 'text-purple-600' },
              { label: 'Total Posted', value: stats.totalRides || 0, color: 'text-gray-800' }
            ].map((stat, idx) => (
              <motion.div 
                key={idx}
                variants={{
                  hidden: { opacity: 0, scale: 0.9 },
                  visible: { opacity: 1, scale: 1 }
                }}
                whileHover={{ y: -5 }}
                className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 flex flex-col justify-center items-center text-center"
              >
                <span className="text-gray-500 text-sm font-semibold mb-2">{stat.label}</span>
                <span className={`text-3xl font-extrabold ${stat.color}`}>{stat.value}</span>
              </motion.div>
            ))}
          </motion.div>
        )}

        {/* Action Cards */}
        <motion.div
           initial={{ opacity: 0 }}
           animate={{ opacity: 1 }}
           transition={{ delay: 0.3, duration: 0.5 }}
        >
          <h2 className="text-2xl font-bold text-gray-900 mb-6">What would you like to do?</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {user?.role === 'DRIVER' ? (
              <>
                <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                  <Link to="/post-ride" className="h-full bg-white p-8 rounded-3xl shadow-sm border border-gray-100 hover:shadow-md hover:border-blue-200 transition-all flex flex-col justify-center items-center text-center group cursor-pointer">
                    <div className="w-16 h-16 bg-blue-50 rounded-full flex items-center justify-center text-3xl mb-4 group-hover:scale-110 transition-transform">🚗</div>
                    <h3 className="text-xl font-bold text-gray-900 mb-2">Post a New Ride</h3>
                    <p className="text-gray-500 text-sm">Offer your empty seats to passengers and save on travel costs</p>
                  </Link>
                </motion.div>

                <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                  <Link to="/my-rides" className="h-full bg-white p-8 rounded-3xl shadow-sm border border-gray-100 hover:shadow-md hover:border-blue-200 transition-all flex flex-col justify-center items-center text-center group cursor-pointer">
                    <div className="w-16 h-16 bg-blue-50 rounded-full flex items-center justify-center text-3xl mb-4 group-hover:scale-110 transition-transform">📋</div>
                    <h3 className="text-xl font-bold text-gray-900 mb-2">My Rides</h3>
                    <p className="text-gray-500 text-sm">Manage your upcoming and completed rides</p>
                  </Link>
                </motion.div>

                <motion.div whileHover={{ scale: 1.02 }} className="bg-white p-8 rounded-3xl shadow-sm border border-gray-100 flex flex-col">
                  <div className="flex items-center gap-3 mb-6">
                    <div className="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center text-xl">🚘</div>
                    <h3 className="text-lg font-bold text-gray-900">Your Vehicle</h3>
                  </div>
                  <div className="space-y-4">
                    <div className="flex justify-between items-center bg-gray-50 p-3 rounded-xl">
                      <span className="text-gray-500 text-sm font-semibold">Model</span>
                      <span className="text-gray-900 font-bold">{user?.carModel}</span>
                    </div>
                    <div className="flex justify-between items-center bg-gray-50 p-3 rounded-xl">
                      <span className="text-gray-500 text-sm font-semibold">Plate</span>
                      <span className="text-gray-900 font-bold bg-yellow-100 text-yellow-800 px-2 py-1 rounded tracking-widest">{user?.licensePlate}</span>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div className="bg-gray-50 p-3 rounded-xl flex flex-col">
                        <span className="text-gray-500 text-xs font-semibold">Type</span>
                        <span className="text-gray-900 font-bold">{user?.vehicleType}</span>
                      </div>
                      <div className="bg-gray-50 p-3 rounded-xl flex flex-col">
                        <span className="text-gray-500 text-xs font-semibold">Capacity</span>
                        <span className="text-gray-900 font-bold">{user?.vehicleCapacity} seats</span>
                      </div>
                    </div>
                  </div>
                </motion.div>
              </>
            ) : (
              <>
                <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                  <Link to="/search" className="h-full bg-white p-8 rounded-3xl shadow-sm border border-gray-100 hover:shadow-md hover:border-blue-200 transition-all flex flex-col justify-center items-center text-center group cursor-pointer">
                    <div className="w-16 h-16 bg-blue-50 rounded-full flex items-center justify-center text-3xl mb-4 group-hover:scale-110 transition-transform">🔍</div>
                    <h3 className="text-xl font-bold text-gray-900 mb-2">Search Rides</h3>
                    <p className="text-gray-500 text-sm">Find affordable rides to your destination</p>
                  </Link>
                </motion.div>

                <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                  <Link to="/my-bookings" className="h-full bg-white p-8 rounded-3xl shadow-sm border border-gray-100 hover:shadow-md hover:border-blue-200 transition-all flex flex-col justify-center items-center text-center group cursor-pointer">
                    <div className="w-16 h-16 bg-blue-50 rounded-full flex items-center justify-center text-3xl mb-4 group-hover:scale-110 transition-transform">🎫</div>
                    <h3 className="text-xl font-bold text-gray-900 mb-2">My Bookings</h3>
                    <p className="text-gray-500 text-sm">View and manage your booked trips</p>
                  </Link>
                </motion.div>
              </>
            )}
          </div>
        </motion.div>

        {/* Reviews Section */}
        <div className="mt-12">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">
            {user?.role === 'PASSENGER' ? 'Reviews from Drivers' : 'Reviews from Passengers'}
          </h2>
          
          {loading ? (
             <p className="text-gray-500 format-medium animate-pulse">Loading reviews...</p>
          ) : reviews && reviews.length > 0 ? (
            <motion.div 
              initial="hidden"
              whileInView="visible"
              viewport={{ once: true }}
              variants={{
                hidden: { opacity: 0 },
                visible: { opacity: 1, transition: { staggerChildren: 0.1 } }
              }}
              className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
            >
              {reviews.map(review => (
                <motion.div 
                  key={review.id} 
                  variants={{
                    hidden: { opacity: 0, y: 20 },
                    visible: { opacity: 1, y: 0 }
                  }}
                  className="bg-white p-6 rounded-3xl border border-gray-100 shadow-sm flex flex-col h-full hover:shadow-md transition-shadow"
                >
                  <div className="flex justify-between items-start mb-4">
                    <div className="flex gap-1">
                      {Array.from({ length: 5 }).map((_, i) => (
                        <span key={i} className={`text-xl ${i < review.rating ? 'text-yellow-400' : 'text-gray-200'}`}>★</span>
                      ))}
                    </div>
                    <span className="text-xs text-gray-400 font-bold bg-gray-50 px-2 py-1 rounded-full">
                      {new Date(review.createdAt).toLocaleDateString()}
                    </span>
                  </div>
                  <p className="text-gray-800 italic mb-6 flex-1 text-lg">"{review.comment}"</p>
                  
                  <div className="mt-auto bg-gray-50 rounded-xl p-4 text-sm flex flex-col gap-2">
                    <div className="flex justify-between items-center text-gray-600 font-medium">
                      <span className="flex items-center gap-2"><span className="text-lg">👤</span> {review.reviewerName || 'Anonymous'}</span>
                    </div>
                    {review.rideRoute && (
                      <div className="flex justify-between items-center text-gray-600 font-medium border-t border-gray-200 pt-2 mt-1">
                        <span className="flex items-center gap-2 truncate"><span className="text-lg">🚗</span> {review.rideRoute}</span>
                      </div>
                    )}
                  </div>
                </motion.div>
              ))}
            </motion.div>
          ) : (
            <div className="bg-white p-12 rounded-3xl text-center border border-dashed border-gray-300">
               <div className="text-5xl mb-4 opacity-30">⭐</div>
               <h3 className="text-xl font-bold text-gray-800 mb-2">No Reviews Yet</h3>
               <p className="text-gray-500 font-medium">After completing a ride, you will receive your reviews here.</p>
            </div>
          )}
        </div>

      </div>
    </div>
  );
};

export default Dashboard;
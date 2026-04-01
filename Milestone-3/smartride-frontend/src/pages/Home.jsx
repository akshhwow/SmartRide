import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, useScroll, useTransform } from 'framer-motion';
import CarAnimation from '../components/animations/CarAnimation';

const Home = () => {
  const navigate = useNavigate();
  const { scrollY } = useScroll();
  const backgroundY = useTransform(scrollY, [0, 500], [0, 150]);
  const opacity = useTransform(scrollY, [0, 300], [0.1, 0.05]);

  const [search, setSearch] = useState({
    source: '',
    destination: '',
    date: '',
    seats: 1
  });

  const handleSearch = (e) => {
    e.preventDefault();
    // Navigate to search page with state
    navigate('/search', { state: search });
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Hero Section */}
      <div className="relative bg-blue-600 bg-opacity-95 py-24 px-4 sm:px-6 lg:px-8 overflow-hidden z-0">
        {/* Background Decorative Pattern with Parallax */}
        <motion.div 
          style={{ y: backgroundY, opacity }}
          className="absolute inset-0 z-[-1]"
        >
          <svg className="h-[200%] w-full" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <pattern id="pattern" width="60" height="60" patternUnits="userSpaceOnUse">
                <path d="M30 30L60 0v15L30 45 0 15V0l30 30z" fill="currentColor" />
              </pattern>
            </defs>
            <rect width="100%" height="100%" fill="url(#pattern)" />
          </svg>
        </motion.div>

        <div className="relative max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-8 text-center md:text-left z-10">
          <motion.div 
            initial={{ opacity: 0, x: -50 }} 
            animate={{ opacity: 1, x: 0 }} 
            transition={{ duration: 0.8 }}
            className="flex-1"
          >
            <h1 className="text-4xl md:text-6xl font-extrabold text-white mb-6 tracking-tight">
              Your ride, your choice
            </h1>
            <p className="text-lg md:text-xl text-blue-100 mb-10 max-w-2xl">
              Join thousands of users sharing rides, saving money, and traveling together across the country.
            </p>
          </motion.div>

          <motion.div 
            initial={{ opacity: 0, scale: 0.8 }} 
            animate={{ opacity: 1, scale: 1 }} 
            transition={{ duration: 0.8, delay: 0.2 }}
            className="flex-1 hidden md:flex justify-end"
          >
            <CarAnimation className="w-[400px] h-auto drop-shadow-xl" />
          </motion.div>
        </div>

        <motion.div 
          initial={{ y: 50, opacity: 0 }} 
          animate={{ y: 0, opacity: 1 }} 
          transition={{ duration: 0.8, delay: 0.4 }}
          className="relative max-w-5xl mx-auto flex flex-col items-center mt-8 z-10"
        >

          {/* Search Box - BlaBlaCar style */}
          <form 
            onSubmit={handleSearch}
            className="w-full max-w-4xl bg-white rounded-2xl md:rounded-full p-2 md:p-3 shadow-[0_8px_30px_rgb(0,0,0,0.12)] flex flex-col md:flex-row items-center gap-2 relative z-10"
          >
            <div className="w-full md:flex-1 relative border-b md:border-b-0 md:border-r border-gray-200">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
              </span>
              <input
                type="text"
                placeholder="Leaving from"
                value={search.source}
                onChange={(e) => setSearch({...search, source: e.target.value})}
                className="w-full py-4 pl-12 pr-4 bg-transparent outline-none text-gray-700 placeholder-gray-500 font-medium"
                required
              />
            </div>

            <div className="w-full md:flex-1 relative border-b md:border-b-0 md:border-r border-gray-200">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
              </span>
              <input
                type="text"
                placeholder="Going to"
                value={search.destination}
                onChange={(e) => setSearch({...search, destination: e.target.value})}
                className="w-full py-4 pl-12 pr-4 bg-transparent outline-none text-gray-700 placeholder-gray-500 font-medium"
                required
              />
            </div>

            <div className="w-full md:w-auto relative border-b md:border-b-0 md:border-r border-gray-200">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
              </span>
              <input
                type="date"
                value={search.date}
                onChange={(e) => setSearch({...search, date: e.target.value})}
                min={new Date().toISOString().split('T')[0]}
                className="w-full md:w-40 py-4 pl-12 pr-4 bg-transparent outline-none text-gray-700 font-medium cursor-pointer"
              />
            </div>

            <div className="w-full md:w-auto relative border-b md:border-b-0 md:border-r md:border-none border-gray-200">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
              </span>
              <input
                type="number"
                min="1"
                max="8"
                value={search.seats}
                onChange={(e) => setSearch({...search, seats: e.target.value})}
                className="w-full md:w-28 py-4 pl-12 pr-4 bg-transparent outline-none text-gray-700 font-medium"
                placeholder="1"
              />
            </div>

            <button 
              type="submit"
              className="w-full md:w-auto mt-2 md:mt-0 bg-blue-600 hover:bg-blue-700 text-white md:rounded-e-full rounded-xl md:rounded-full font-bold text-lg py-4 md:py-3.5 px-8 transition-colors shadow-none md:mr-1"
            >
              Search
            </button>
          </form>
        </motion.div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 flex-1">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 md:gap-12">
          {/* Feature 1 */}
          <motion.div 
            initial={{ opacity: 0, y: 30 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5, delay: 0.1 }}
            whileHover={{ scale: 1.05 }}
            className="flex text-left items-start gap-4 p-4 rounded-2xl hover:bg-white hover:shadow-xl transition-all cursor-default relative overflow-hidden group"
          >
            <div className="flex-shrink-0 text-3xl group-hover:scale-125 transition-transform">💰</div>
            <div>
              <h3 className="text-xl font-bold text-gray-900 mb-2">Your pick of rides at low prices</h3>
              <p className="text-gray-500">No matter where you're going, find the perfect ride from our wide range of destinations and routes at low prices.</p>
            </div>
          </motion.div>

          {/* Feature 2 */}
          <motion.div 
            initial={{ opacity: 0, y: 30 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5, delay: 0.3 }}
            whileHover={{ scale: 1.05 }}
            className="flex text-left items-start gap-4 p-4 rounded-2xl hover:bg-white hover:shadow-xl transition-all cursor-default relative overflow-hidden group"
          >
            <div className="flex-shrink-0 text-3xl group-hover:scale-125 transition-transform">🛡️</div>
            <div>
              <h3 className="text-xl font-bold text-gray-900 mb-2">Trust who you travel with</h3>
              <p className="text-gray-500">We take the time to get to know each of our members and bus partners. We check reviews, profiles and IDs, so you know who you are travelling with.</p>
            </div>
          </motion.div>

          {/* Feature 3 */}
          <motion.div 
            initial={{ opacity: 0, y: 30 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5, delay: 0.5 }}
            whileHover={{ scale: 1.05 }}
            className="flex text-left items-start gap-4 p-4 rounded-2xl hover:bg-white hover:shadow-xl transition-all cursor-default relative overflow-hidden group"
          >
            <div className="flex-shrink-0 text-3xl group-hover:scale-125 transition-transform">⚡</div>
            <div>
              <h3 className="text-xl font-bold text-gray-900 mb-2">Scroll, click, tap and go!</h3>
              <p className="text-gray-500">Booking a ride has never been easier! Thanks to our simple app powered by great technology, you can book a ride close to you in just minutes.</p>
            </div>
          </motion.div>
        </div>
      </div>
    </div>
  );
};

export default Home;

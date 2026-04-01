import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { rideAPI, bookingAPI, fareAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-toastify';
import { motion } from 'framer-motion';
import SearchAnimation from '../components/animations/SearchAnimation';
import EmptyAnimation from '../components/animations/EmptyAnimation';

const SearchRides = () => {
  const { isAuthenticated, isPassenger } = useAuth();
  const location = useLocation();
  const [searchParams, setSearchParams] = useState({
    source: location.state?.source || '',
    destination: location.state?.destination || '',
    date: location.state?.date || '',
    seats: location.state?.seats || 1
  });
  const [rides, setRides] = useState([]);
  const [loading, setLoading] = useState(false);
  const [bookingRideId, setBookingRideId] = useState(null);

  const [fareEstimates, setFareEstimates] = useState({});
  const [estimateLoading, setEstimateLoading] = useState(false);
  const [estimatingRideId, setEstimatingRideId] = useState(null);
  const [coordsByRideId, setCoordsByRideId] = useState({});

  useEffect(() => {
    if (location.state?.source) {
      handleSearch(new Event('submit'));
    }
  }, []);

  const handleSearch = async (e) => {
    if (e) e.preventDefault();
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

  const geocodeAddress = async (address) => {
    const url = `https://nominatim.openstreetmap.org/search?format=json&limit=1&q=${encodeURIComponent(
      address
    )}`;
    const response = await fetch(url, { headers: { 'Accept-Language': 'en' } });
    const results = await response.json();
    if (!Array.isArray(results) || results.length === 0) {
      throw new Error('Could not find location: ' + address);
    }
    return { lat: parseFloat(results[0].lat), lon: parseFloat(results[0].lon) };
  };

  const handleEstimate = async (rideId) => {
    if (!searchParams.source || !searchParams.destination) {
      toast.error('Please enter both origin and destination to estimate fare');
      return;
    }

    setEstimateLoading(true);
    setEstimatingRideId(rideId);

    try {
      const origin = await geocodeAddress(searchParams.source);
      const destination = await geocodeAddress(searchParams.destination);

      const response = await fareAPI.estimateFare({
        rideId,
        originLat: origin.lat,
        originLng: origin.lon,
        destinationLat: destination.lat,
        destinationLng: destination.lon,
        seatsBooked: parseInt(searchParams.seats)
      });

      setFareEstimates((prev) => ({
        ...prev,
        [rideId]: response.data.data,
      }));

      setCoordsByRideId((prev) => ({
        ...prev,
        [rideId]: {
          originLat: origin.lat,
          originLng: origin.lon,
          destinationLat: destination.lat,
          destinationLng: destination.lon,
        },
      }));

      toast.success('Fare estimate calculated');
    } catch (error) {
      toast.error(error.message || 'Failed to estimate fare');
    } finally {
      setEstimateLoading(false);
      setEstimatingRideId(null);
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
      const basePayload = {
        rideId,
        seatsToBook: parseInt(seatsToBook),
        pickupLocation: searchParams.source,
        dropLocation: searchParams.destination,
      };

      const coords = coordsByRideId[rideId];
      const payload = coords
        ? {
            ...basePayload,
            originLat: coords.originLat,
            originLng: coords.originLng,
            destinationLat: coords.destinationLat,
            destinationLng: coords.destinationLng,
          }
        : basePayload;

      const response = await bookingAPI.bookRide(payload);

      toast.success(response.data.message);
      handleSearch();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Booking failed');
    } finally {
      setBookingRideId(null);
    }
  };

  return (
    <div className="bg-gray-50 min-h-screen">
      <div className="max-w-4xl mx-auto px-4 py-8">
        {/* Search Header Form */}
        <div className="bg-white rounded-2xl shadow-sm p-4 md:p-6 mb-8 border border-gray-100">
          <form onSubmit={handleSearch} className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="relative">
              <input
                type="text"
                placeholder="Leaving from"
                value={searchParams.source}
                onChange={(e) => setSearchParams({...searchParams, source: e.target.value})}
                className="w-full bg-gray-100 rounded-xl px-4 py-3 outline-none text-gray-700 font-medium"
                required
              />
            </div>
            <div className="relative">
              <input
                type="text"
                placeholder="Going to"
                value={searchParams.destination}
                onChange={(e) => setSearchParams({...searchParams, destination: e.target.value})}
                className="w-full bg-gray-100 rounded-xl px-4 py-3 outline-none text-gray-700 font-medium"
                required
              />
            </div>
            <div className="relative">
              <input
                type="date"
                value={searchParams.date}
                onChange={(e) => setSearchParams({...searchParams, date: e.target.value})}
                min={new Date().toISOString().split('T')[0]}
                className="w-full bg-gray-100 rounded-xl px-4 py-3 outline-none text-gray-700 font-medium"
              />
            </div>
            <div className="relative flex gap-2">
              <input
                type="number"
                min="1"
                max="8"
                value={searchParams.seats}
                onChange={(e) => setSearchParams({...searchParams, seats: e.target.value})}
                className="w-20 bg-gray-100 rounded-xl px-4 py-3 outline-none text-gray-700 font-medium"
              />
              <button 
                type="submit" 
                className="flex-1 bg-blue-600 hover:bg-blue-700 text-white rounded-xl font-bold transition-colors"
                disabled={loading}
              >
                {loading ? '...' : 'Search'}
              </button>
            </div>
          </form>
        </div>

        {/* Results */}
        <div className="space-y-6">
          <h2 className="text-2xl font-bold text-gray-800">
            {new Date(searchParams.date || new Date()).toLocaleDateString('en-GB', { weekday: 'long', day: 'numeric', month: 'long' })}
          </h2>

          <div className="text-gray-600 text-sm mb-4">
            {rides.length} ride{rides.length !== 1 ? 's' : ''} available
          </div>

          {loading ? (
            <div className="flex flex-col items-center justify-center py-16 bg-white rounded-2xl shadow-sm border border-gray-100">
              <SearchAnimation className="w-64 h-64" />
              <h3 className="text-xl font-bold text-gray-800 mt-4">Searching for rides...</h3>
              <p className="text-gray-500">Finding the best options for your journey</p>
            </div>
          ) : rides.length > 0 ? (
            <motion.div 
              initial="hidden"
              animate="visible"
              variants={{
                hidden: { opacity: 0 },
                visible: { opacity: 1, transition: { staggerChildren: 0.1 } }
              }}
              className="grid gap-4"
            >
              {rides.map((ride) => {
                const fareObj = fareEstimates[ride.id];
                const displayPrice = fareObj ? fareObj.passengerFare : (Number(ride.pricePerSeat || 0) * Number(searchParams.seats || 1));
                return (
                  <motion.div 
                    variants={{
                      hidden: { opacity: 0, y: 20 },
                      visible: { opacity: 1, y: 0, transition: { duration: 0.4 } }
                    }}
                    whileHover={{ scale: 1.01 }}
                    key={ride.id} 
                    className="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 hover:shadow-md transition-shadow"
                  >
                    <div className="flex justify-between items-start mb-4">
                      <div className="flex flex-col">
                        <div className="flex items-center gap-4">
                          <div className="flex flex-col items-center">
                            <span className="text-lg font-bold text-gray-800">{ride.departureTime || 'TBD'}</span>
                            <div className="w-0.5 h-8 bg-gray-300 my-1"></div>
                            <span className="text-lg font-bold text-gray-800">EST</span>
                          </div>
                          <div className="flex flex-col gap-5">
                            <div className="text-gray-800 font-semibold">{ride.source}</div>
                            <div className="text-gray-800 font-semibold">{ride.destination}</div>
                          </div>
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="text-xs text-gray-400 font-bold tracking-tight mb-0.5">
                          (₹{fareObj ? (Number(fareObj.passengerFare) / Number(searchParams.seats || 1)).toFixed(2) : Number(ride.pricePerSeat || 0).toFixed(2)} × {searchParams.seats} seats)
                        </div>
                        <div className="text-3xl font-extrabold text-gray-900 leading-none">
                          ₹{displayPrice ? Number(displayPrice).toFixed(2) : '--'}
                        </div>
                        {fareObj && <div className="text-xs text-green-600 font-bold uppercase tracking-wider mt-1">Dynamic Fare</div>}
                      </div>
                    </div>

                    <div className="flex items-center justify-between border-t border-gray-100 pt-4 mt-2">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 font-bold text-lg">
                          {ride.driverName?.charAt(0) || 'D'}
                        </div>
                        <div>
                          <div className="font-semibold text-gray-800">{ride.driverName}</div>
                          <div className="flex items-center text-sm text-gray-500 gap-2">
                            <span className="flex items-center text-yellow-500">
                              ★ 4.8
                            </span>
                            <span>• {ride.driverVehicleType} • {ride.availableSeats} seats left</span>
                          </div>
                        </div>
                      </div>
                      <div className="flex gap-2">
                        <button
                          onClick={() => handleEstimate(ride.id)}
                          className="text-blue-600 font-medium px-4 py-2 hover:bg-blue-50 rounded-xl transition-colors text-sm border border-transparent hover:border-blue-100"
                          disabled={estimateLoading && estimatingRideId === ride.id}
                        >
                          {estimateLoading && estimatingRideId === ride.id ? 'Loading...' : fareObj ? 'Re-estimate' : 'Estimate'}
                        </button>
                        <button
                          onClick={() => handleBook(ride.id)}
                          className="bg-blue-600 hover:bg-blue-700 text-white font-semibold px-6 py-2 rounded-xl shadow-sm transition-colors text-sm"
                          disabled={bookingRideId === ride.id}
                        >
                          {bookingRideId === ride.id ? 'Booking...' : 'Book'}
                        </button>
                      </div>
                    </div>

                    {/* Fare Prediction Breakdown */}
                    {fareObj && (
                      <div className="mt-4 bg-green-50 rounded-xl p-4 border border-green-100 text-sm shadow-inner transition-all">
                        <div className="font-bold text-green-800 mb-2 border-b border-green-200 pb-2">Dynamic Fare Breakdown</div>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-y-2 gap-x-6 text-green-700">
                          <div className="flex justify-between items-center">
                            <span>Base Fare:</span> <span className="font-semibold">₹{Number(fareObj.baseFare || 0).toFixed(2)}</span>
                          </div>
                          <div className="flex justify-between items-center">
                            <span>Route Distance:</span> <span className="font-semibold">{Number(fareObj.distanceKm || 0).toFixed(1)} km</span>
                          </div>
                          <div className="flex justify-between items-center">
                            <span>Rate per Km:</span> <span className="font-semibold">₹{Number(fareObj.farePerKm || 0).toFixed(2)}/km</span>
                          </div>
                          <div className="flex justify-between items-center">
                            <span>Seats Requested:</span> <span className="font-semibold">{searchParams.seats}</span>
                          </div>
                          <div className="md:col-span-2 flex justify-between items-center border-t border-green-200/60 mt-2 pt-2 text-base font-bold text-green-900">
                            <span>Total Estimated Fare:</span> <span>₹{Number(fareObj.passengerFare || 0).toFixed(2)}</span>
                          </div>
                        </div>
                      </div>
                    )}
                  </motion.div>
                );
              })}
            </motion.div>
          ) : (
            <motion.div 
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.5 }}
              className="text-center py-16 bg-white rounded-2xl shadow-sm border border-gray-100 flex flex-col items-center"
            >
              <EmptyAnimation className="w-64 h-64 mb-4" />
              <h3 className="text-xl font-bold text-gray-800 mb-2">No rides found</h3>
              <p className="text-gray-500">Try modifying your search criteria to find available rides.</p>
            </motion.div>
          )}
        </div>
      </div>
    </div>
  );
};

export default SearchRides;
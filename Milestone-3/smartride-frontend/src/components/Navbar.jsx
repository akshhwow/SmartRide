import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import NotificationBell from './shared/NotificationBell';

const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="sticky top-0 z-50 bg-white shadow-sm border-b border-gray-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16 items-center">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2 text-2xl font-bold text-blue-600 tracking-tight">
            <span className="text-3xl">🚙</span>
            SmartRide
          </Link>

          {/* Desktop Links */}
          <div className="hidden md:flex items-center space-x-8">
            <Link to="/search" className="text-gray-600 hover:text-blue-600 font-medium transition-colors flex items-center gap-1">
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
              Search Rides
            </Link>

            {isAuthenticated() ? (
              <>
                <Link to="/dashboard" className="text-gray-600 hover:text-blue-600 font-medium transition-colors">Dashboard</Link>
                
                {user?.role === 'DRIVER' && (
                  <>
                    <Link to="/post-ride" className="text-gray-600 hover:text-blue-600 font-medium transition-colors flex items-center gap-1">
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4v16m8-8H4"></path></svg>
                      Offer a Ride
                    </Link>
                    <Link to="/my-rides" className="text-gray-600 hover:text-blue-600 font-medium transition-colors">My Rides</Link>
                  </>
                )}
                
                {user?.role === 'PASSENGER' && (
                  <Link to="/my-bookings" className="text-gray-600 hover:text-blue-600 font-medium transition-colors">My Bookings</Link>
                )}

                {user?.role === 'ADMIN' && (
                  <Link to="/admin" className="text-blue-700 font-semibold bg-blue-50 px-3 py-1 rounded-md">Admin Panel</Link>
                )}

                <div className="flex items-center gap-4 border-l pl-4 border-gray-200">
                  <NotificationBell />
                  
                  <div className="relative group cursor-pointer">
                    <div className="flex items-center gap-2">
                      <div className="w-9 h-9 bg-gray-100 flex items-center justify-center rounded-full text-blue-600 font-bold border border-gray-200 shadow-sm">
                        {user?.fullName?.charAt(0) || 'U'}
                      </div>
                      <div className="flex flex-col">
                        <span className="text-sm font-semibold text-gray-800 leading-tight">{user?.fullName}</span>
                      </div>
                    </div>
                    {/* Dropdown hook */}
                    <div className="absolute right-0 top-full mt-2 w-48 bg-white border border-gray-100 rounded-xl shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 overflow-hidden">
                      <button onClick={handleLogout} className="w-full text-left px-4 py-3 text-sm text-red-600 hover:bg-gray-50 font-medium transition-colors">
                        Logout
                      </button>
                    </div>
                  </div>
                </div>
              </>
            ) : (
              <div className="flex items-center gap-4">
                <Link to="/login" className="text-blue-600 font-medium hover:text-blue-700 transition-colors">Login</Link>
                <Link to="/register" className="bg-blue-600 hover:bg-blue-700 text-white px-5 py-2.5 rounded-full font-medium transition-colors shadow-sm">
                  Sign Up
                </Link>
              </div>
            )}
          </div>

          {/* Mobile menu button */}
          <div className="md:hidden flex items-center gap-4">
            {isAuthenticated() && <NotificationBell />}
            <button
              onClick={() => setMenuOpen(!menuOpen)}
              className="text-gray-600 hover:text-gray-900 focus:outline-none"
            >
              <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                {menuOpen ? (
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                ) : (
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16M4 18h16" />
                )}
              </svg>
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu */}
      {menuOpen && (
        <div className="md:hidden bg-white border-t border-gray-100 px-4 py-4 space-y-3 shadow-lg absolute w-full left-0">
          <Link to="/search" className="block text-gray-700 font-medium py-2">Search Rides</Link>
          
          {isAuthenticated() ? (
            <>
              <Link to="/dashboard" className="block text-gray-700 font-medium py-2">Dashboard</Link>
              {user?.role === 'DRIVER' && (
                <>
                  <Link to="/post-ride" className="block text-gray-700 font-medium py-2">Offer a Ride</Link>
                  <Link to="/my-rides" className="block text-gray-700 font-medium py-2">My Rides</Link>
                </>
              )}
              {user?.role === 'PASSENGER' && (
                <Link to="/my-bookings" className="block text-gray-700 font-medium py-2">My Bookings</Link>
              )}
              <div className="pt-4 mt-2 border-t border-gray-100">
                <div className="flex items-center gap-3 mb-4">
                  <div className="w-10 h-10 bg-blue-100 flex items-center justify-center rounded-full text-blue-600 font-bold">
                    {user?.fullName?.charAt(0) || 'U'}
                  </div>
                  <div>
                    <div className="font-semibold text-gray-800">{user?.fullName}</div>
                    <div className="text-xs text-gray-500">{user?.role}</div>
                  </div>
                </div>
                <button
                  onClick={handleLogout}
                  className="w-full text-center bg-red-50 text-red-600 py-2.5 rounded-lg font-medium"
                >
                  Logout
                </button>
              </div>
            </>
          ) : (
            <div className="pt-4 mt-2 border-t border-gray-100 bg-white grid grid-cols-2 gap-3">
              <Link to="/login" className="text-center py-2.5 border border-gray-200 rounded-lg font-medium text-gray-700">Login</Link>
              <Link to="/register" className="text-center py-2.5 bg-blue-600 text-white rounded-lg font-medium shadow-sm">Sign Up</Link>
            </div>
          )}
        </div>
      )}
    </nav>
  );
};

export default Navbar;

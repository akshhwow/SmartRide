import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-logo">
          🚗 SmartRide
        </Link>

        <div className="navbar-links">
          <Link to="/search" className="nav-link">
            Search Rides
          </Link>

          {isAuthenticated() ? (
            <>
              <Link to="/dashboard" className="nav-link">
                Dashboard
              </Link>
              
              {user?.role === 'DRIVER' && (
                <>
                  <Link to="/post-ride" className="nav-link">
                    Post Ride
                  </Link>
                  <Link to="/my-rides" className="nav-link">
                    My Rides
                  </Link>
                </>
              )}
              
              {user?.role === 'PASSENGER' && (
                <Link to="/my-bookings" className="nav-link">
                  My Bookings
                </Link>
              )}

              <div className="user-info">
                <span className="user-name">{user?.fullName}</span>
                <span className="user-role">{user?.role}</span>
              </div>
              
              <button onClick={handleLogout} className="nav-button logout-btn">
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="nav-button login-btn">
                Login
              </Link>
              <Link to="/register" className="nav-button register-btn">
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;

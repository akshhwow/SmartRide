// ================== DASHBOARD.JSX ==================
// Save as: src/pages/Dashboard.jsx

import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Dashboard.css';

const Dashboard = () => {
  const { user } = useAuth();

  return (
    <div className="dashboard-container">
      <div className="dashboard-card">
        <div className="dashboard-header">
          <h1>Welcome, {user?.fullName}! 👋</h1>
          <p className="dashboard-role">Role: {user?.role}</p>
        </div>

        {user?.role === 'DRIVER' ? (
          <div className="dashboard-actions">
            <h2>What would you like to do?</h2>
            
            <Link to="/post-ride" className="dashboard-action-card">
              <div className="action-icon">🚗</div>
              <h3>Post a New Ride</h3>
              <p>Offer your seats to passengers</p>
            </Link>

            <Link to="/my-rides" className="dashboard-action-card">
              <div className="action-icon">📋</div>
              <h3>My Rides</h3>
              <p>View and manage your posted rides</p>
            </Link>

            <div className="driver-info-card">
              <h3>Your Vehicle</h3>
              <p><strong>Model:</strong> {user?.carModel}</p>
              <p><strong>Plate:</strong> {user?.licensePlate}</p>
              <p><strong>Type:</strong> {user?.vehicleType}</p>
              <p><strong>Capacity:</strong> {user?.vehicleCapacity} seats</p>
            </div>
          </div>
        ) : (
          <div className="dashboard-actions">
            <h2>What would you like to do?</h2>
            
            <Link to="/search" className="dashboard-action-card">
              <div className="action-icon">🔍</div>
              <h3>Search Rides</h3>
              <p>Find rides to your destination</p>
            </Link>

            <Link to="/my-bookings" className="dashboard-action-card">
              <div className="action-icon">🎫</div>
              <h3>My Bookings</h3>
              <p>View your booked rides</p>
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
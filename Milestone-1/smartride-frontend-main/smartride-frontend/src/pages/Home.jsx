import React from 'react';
import { Link } from 'react-router-dom';
import './Home.css';

const Home = () => {
  return (
    <div className="home-container">
      <div className="hero-section">
        <h1 className="hero-title">
          Welcome to SmartRide
        </h1>
        <p className="hero-subtitle">
          Share rides, save money, and travel together!
        </p>
        
        <div className="cta-buttons">
          <Link to="/search" className="cta-btn primary">
            🔍 Find a Ride
          </Link>
          <Link to="/register" className="cta-btn secondary">
            🚗 Become a Driver
          </Link>
        </div>

        <div className="features-grid">
          <div className="feature-card">
            <div className="feature-icon">💰</div>
            <h3>Save Money</h3>
            <p>Share travel costs and reduce expenses</p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">🌍</div>
            <h3>Go Green</h3>
            <p>Reduce carbon footprint by sharing rides</p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">👥</div>
            <h3>Meet People</h3>
            <p>Connect with like-minded travelers</p>
          </div>

          <div className="feature-card">
            <div className="feature-icon">✅</div>
            <h3>Safe & Verified</h3>
            <p>All users are verified with email</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;

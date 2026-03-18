import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { AuthProvider, useAuth } from './context/AuthContext';

// Components
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Register from './pages/Register';
import Login from './pages/Login';
import VerifyOtp from './pages/VerifyOtp';
import Dashboard from './pages/Dashboard';
import SearchRides from './pages/SearchRides';
import PostRide from './pages/PostRide';
import MyRides from './pages/MyRides';
import MyBookings from './pages/MyBookings';

// Protected Route wrapper
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) {
    return <div style={{display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', color: 'white'}}>Loading...</div>;
  }
  
  return isAuthenticated() ? children : <Navigate to="/login" />;
};

// Driver-only Route
const DriverRoute = ({ children }) => {
  const { isDriver } = useAuth();
  return isDriver() ? children : <Navigate to="/dashboard" />;
};

// Passenger-only Route
const PassengerRoute = ({ children }) => {
  const { isPassenger } = useAuth();
  return isPassenger() ? children : <Navigate to="/dashboard" />;
};

function AppContent() {
  return (
    <Router>
      <div style={{ minHeight: '100vh' }}>
        <Navbar />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/register" element={<Register />} />
          <Route path="/login" element={<Login />} />
          <Route path="/verify-otp" element={<VerifyOtp />} />
          
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
          
          <Route path="/search" element={<SearchRides />} />
          
          <Route
            path="/post-ride"
            element={
              <ProtectedRoute>
                <DriverRoute>
                  <PostRide />
                </DriverRoute>
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/my-rides"
            element={
              <ProtectedRoute>
                <DriverRoute>
                  <MyRides />
                </DriverRoute>
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/my-bookings"
            element={
              <ProtectedRoute>
                <PassengerRoute>
                  <MyBookings />
                </PassengerRoute>
              </ProtectedRoute>
            }
          />
        </Routes>
        
        <ToastContainer
          position="top-right"
          autoClose={3000}
          hideProgressBar={false}
          newestOnTop
          closeOnClick
          rtl={false}
          pauseOnFocusLoss
          draggable
          pauseOnHover
        />
      </div>
    </Router>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;

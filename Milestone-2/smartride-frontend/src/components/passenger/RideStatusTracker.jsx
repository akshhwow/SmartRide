import React, { useState, useEffect } from 'react';
import { subscribeToNotifications } from '../../services/socketService';
import './RideStatusTracker.css';

const RideStatusTracker = ({ booking }) => {
  // Determine initial step based on booking status or ride status.
  // We'll track purely via websocket events for live feel
  const [currentStep, setCurrentStep] = useState(1);
  const [liveMessage, setLiveMessage] = useState('Waiting for driver updates...');

  useEffect(() => {
    // Basic init logic mapping backend states to steps
    if (booking.status === 'COMPLETED') setCurrentStep(4);
    else if (booking.status === 'IN_PROGRESS') setCurrentStep(3);
    else if (booking.status === 'ACCEPTED') setCurrentStep(2);
    else setCurrentStep(1); // Pending / Confirmed
    
    // Subscribe to live websocket events targeting this user's rides
    const unsubscribe = subscribeToNotifications((notification) => {
      // Only react if this notification is for this specific ride
      if (notification.rideId !== booking.rideId) return;

      switch(notification.type) {
        case 'RIDE_ACCEPTED':
          setCurrentStep(2);
          setLiveMessage(notification.message);
          break;
        case 'RIDE_STARTED':
          setCurrentStep(3);
          setLiveMessage(notification.message);
          break;
        case 'RIDE_ENDED':
          setCurrentStep(4);
          setLiveMessage(notification.message);
          break;
        case 'PAYMENT_CONFIRMED':
          setLiveMessage(notification.message);
          break;
        case 'DRIVER_CANCELLED':
          setLiveMessage('Driver has cancelled the ride.');
          break;
        default:
          break;
      }
    });

    return () => unsubscribe();
  }, [booking]);

  const steps = [
    { id: 1, label: 'Booked' },
    { id: 2, label: 'Accepted' },
    { id: 3, label: 'In Progress' },
    { id: 4, label: 'Completed' }
  ];

  return (
    <div className="ride-status-tracker">
      <div className="tracker-timeline">
        {steps.map(step => (
          <div 
            key={step.id} 
            className={`tracker-step ${currentStep >= step.id ? 'active' : ''} ${currentStep === step.id ? 'current' : ''}`}
          >
            <div className="step-circle">{currentStep > step.id ? '✓' : step.id}</div>
            <div className="step-label">{step.label}</div>
          </div>
        ))}
        {/* Progress bar line behind circles */}
        <div className="tracker-line-bg">
          <div 
            className="tracker-line-fill" 
            style={{ width: `${((currentStep - 1) / (steps.length - 1)) * 100}%` }}
          ></div>
        </div>
      </div>
      
      <div className="live-message-box">
        <span className="pulse-dot"></span>
        <p>{liveMessage}</p>
      </div>
    </div>
  );
};

export default RideStatusTracker;

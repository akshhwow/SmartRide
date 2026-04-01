import React, { useState } from 'react';
import { rideActionAPI } from '../../services/api';
import { toast } from 'react-toastify';
import './RideControlPanel.css';

const RideControlPanel = ({ ride, onUpdate }) => {
  const [loading, setLoading] = useState(false);

  // We determine what action is currently possible. Wait, the ride status
  // might not natively support 'ACCEPTED'. But we mock UI states via local state or we just use ride properties if available.
  // Actually, we'll implement smart visibility based on 'ride.status'. If backend model just has ACTIVE/FULL/COMPLETED...
  // Wait, in our RideActionController we don't change status to ACCEPTED since we don't know if the backend allows it. 
  // Let's hold a local state for the UI progression if we must, or track it.
  const [localStatus, setLocalStatus] = useState(ride.status);

  const handleAction = async (actionFn, nextStatus, successMsg) => {
    setLoading(true);
    try {
      const response = await actionFn(ride.id);
      toast.success(successMsg);
      setLocalStatus(nextStatus);
      if (onUpdate) onUpdate(response.data.data);
    } catch (err) {
      toast.error(err.response?.data?.message || `Failed to perform action`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="ride-control-panel">
      <div className="status-badge" data-status={localStatus}>
        {localStatus}
      </div>
      
      <div className="rcp-actions">
        {(localStatus === 'ACTIVE' || localStatus === 'PENDING' || localStatus === 'FULL') && (
          <button 
            className="action-btn accept-btn" 
            disabled={loading}
            onClick={() => handleAction(rideActionAPI.acceptRide, 'ACCEPTED', 'Ride Accepted!')}
          >
            {loading ? '⏳ Processing...' : '✅ Accept Ride'}
          </button>
        )}

        {localStatus === 'ACCEPTED' && (
          <button 
            className="action-btn start-btn" 
            disabled={loading}
            onClick={() => handleAction(rideActionAPI.startRide, 'IN_PROGRESS', 'Ride Started!')}
          >
             {loading ? '⏳ Processing...' : '🚗 Start Ride'}
          </button>
        )}

        {localStatus === 'IN_PROGRESS' && (
          <button 
            className="action-btn end-btn" 
            disabled={loading}
            onClick={() => handleAction(rideActionAPI.endRide, 'COMPLETED', 'Ride Ended!')}
          >
             {loading ? '⏳ Processing...' : '🏁 End Ride'}
          </button>
        )}
        
        {localStatus === 'COMPLETED' && (
           <div className="completion-summary">
             <h4>Ride Completed Successfully</h4>
             <p>Awaiting passenger payment.</p>
           </div>
        )}
      </div>
    </div>
  );
};

export default RideControlPanel;

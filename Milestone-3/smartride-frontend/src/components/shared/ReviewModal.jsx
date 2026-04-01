import React, { useState } from 'react';
import { reviewAPI } from '../../services/api';
import { toast } from 'react-toastify';
import './ReviewModal.css';

const ReviewModal = ({ isOpen, onClose, rideId, revieweeId, roleContext, onReviewSuccess }) => {
  const [rating, setRating] = useState(0);
  const [hoverRating, setHoverRating] = useState(0);
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (rating === 0) {
      toast.error('Please select a star rating');
      return;
    }

    setSubmitting(true);
    try {
      await reviewAPI.createReview({
        revieweeId,
        rideId,
        rating,
        comment,
      });
      toast.success('Review submitted successfully!');
      if (onReviewSuccess) {
        onReviewSuccess();
      }
      handleClose();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to submit review. You may have already reviewed this ride.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleClose = () => {
    setRating(0);
    setHoverRating(0);
    setComment('');
    onClose();
  };

  return (
    <div className="review-modal-overlay">
      <div className="review-modal-content">
        <div className="review-modal-header">
          <h2>{roleContext === 'PASSENGER' ? 'Rate your Driver' : 'Rate your Passenger'}</h2>
          <button onClick={handleClose} className="review-modal-close" title="Close">
            &times;
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="review-star-container">
            <label className="review-star-label">Overall Rating</label>
            <div className="review-stars">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  type="button"
                  key={star}
                  onClick={() => setRating(star)}
                  onMouseEnter={() => setHoverRating(star)}
                  onMouseLeave={() => setHoverRating(0)}
                  className={`review-star ${star <= (hoverRating || rating) ? 'active' : ''}`}
                >
                  ★
                </button>
              ))}
            </div>
            <span className="review-rating-text">
              {rating === 1 && "Poor"}
              {rating === 2 && "Fair"}
              {rating === 3 && "Good"}
              {rating === 4 && "Very Good"}
              {rating === 5 && "Excellent!"}
              {rating === 0 && " "}
            </span>
          </div>

          <div className="review-comment-group">
            <label htmlFor="comment">Leave a comment</label>
            <textarea
              id="comment"
              rows={3}
              placeholder="How was your experience?"
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              required
            />
          </div>

          <div className="review-modal-actions">
            <button
              type="button"
              onClick={handleClose}
              className="review-btn review-btn-cancel"
              disabled={submitting}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting || rating === 0}
              className="review-btn review-btn-submit"
            >
              {submitting ? 'Submitting...' : 'Submit Review'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ReviewModal;

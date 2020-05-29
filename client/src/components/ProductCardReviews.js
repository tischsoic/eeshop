import React, { useState, useEffect, useContext } from 'react';
import { useParams } from 'react-router-dom';
import {
  getUrl,
  getRequestInit,
  parseJson,
  handleError,
} from '../utils/requestUtils';
import useForm from '../hooks/useForm';

import { UserContext } from '../providers/UserProvider';

import ButtonWithSpinner from './ButtonWithSpinner';
import { getToken } from '../utils/userUtils';

export default function ProductCardReviews() {
  const { user } = useContext(UserContext);
  const { productId } = useParams();
  const [reviews, setReviews] = useState(null);
  const [fields, isDuringProcessing, onChange, setIsDuringProcessing] = useForm(
    {
      content: '',
    }
  );
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [forceRefresh, setFroceRefresh] = useState(0);
  const handleAddReview = (event) => {
    event.preventDefault();
    setIsDuringProcessing(true);
    setError(null);
    setSuccess(false);

    fetch(
      getUrl('review'),
      getRequestInit(
        {
          method: 'POST',
          body: JSON.stringify({
            authorId: user.id,
            productId: parseInt(productId),
            content: fields.content,
          }),
        },
        getToken(user)
      )
    )
      .then(parseJson)
      .then((addedReview) => setReviews([...reviews, addedReview]))
      .catch(() => setError('Error while adding your review.'))
      .finally(() => setIsDuringProcessing(false));
  };
  const handleReviewDelete = (reviewId) => {
    setError(null);
    setSuccess(null);

    fetch(
      getUrl(`review/${reviewId}`),
      getRequestInit({ method: 'DELETE' }, getToken(user))
    )
      .then(handleError)
      .then(() => {
        setFroceRefresh(forceRefresh + 1);
        setSuccess('review deleted');
      })
      .catch(() => {
        setError('Error while deleting review');
        setUser(null);
      });
  };

  useEffect(() => {
    fetch(
      getUrl(`review/by-product/${productId}`),
      getRequestInit({ method: 'GET' })
    )
      .then(parseJson)
      .then((fetchedReviews) => setReviews(fetchedReviews))
      .catch(() => setError('Error while fetching reviews data'));
  }, [setReviews, forceRefresh]);

  return (
    <div>
      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}
      {success && (
        <div className="alert alert-success" role="alert">
          {success}
        </div>
      )}
      {reviews &&
        reviews.map((review) => (
          <div
            key={review.reviewId}
            style={{
              border: '1px solid black',
              borderRadius: '.25rem',
              margin: '20px',
              padding: '10px',
            }}
          >
            <i>by: {review.user.email}</i>
            <br />
            review: {review.content}
            <br />
            {user?.role === 'staff' && (
              <button
                className="btn btn-danger"
                type="button"
                onClick={() => handleReviewDelete(review.reviewId)}
              >
                Delete
              </button>
            )}
          </div>
        ))}
      {!user ? (
        <h5>Login to add your review.</h5>
      ) : (
        <div className="new-review">
          <form>
            <div className="form-group">
              <label htmlFor="content">Your review</label>
              <input
                type="text"
                className="form-control"
                id="content"
                name="content"
                placeholder="Your review"
                value={fields.content}
                onChange={onChange}
                disabled={isDuringProcessing}
              />
            </div>
            <ButtonWithSpinner
              type="submit"
              className="btn btn-primary"
              onClick={handleAddReview}
              isDuringProcessing={isDuringProcessing}
              label="Add review"
              labelProcessing="Adding review..."
            />
          </form>
        </div>
      )}
    </div>
  );
}

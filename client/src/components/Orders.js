import React from 'react';
import { useState } from 'react';

export default function Orders() {
  const [error, setError] = useState(null);
  const isFetchingData = false;

  return (
    <div className="card mb-3">
      <h5 className="card-header">Orders</h5>
      <div className="card-body">
        {error && (
          <div className="alert alert-danger" role="alert">
            {error}
          </div>
        )}
        {isFetchingData && (
          <div className="spinner-grow" role="status">
            <span className="sr-only">Loading...</span>
          </div>
        )}
        {!isFetchingData && <div className="checkout">Checkout</div>}
      </div>
    </div>
  );
}

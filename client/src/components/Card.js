import React from 'react';

export default function Card({ children, title, error, success, isLoading }) {
  return (
    <div className="card mb-3">
      <h5 className="card-header">{title}</h5>
      <div className="card-body">
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
        {isLoading && (
          <div className="spinner-grow" role="status">
            <span className="sr-only">Loading...</span>
          </div>
        )}
        {!isLoading && children}
      </div>
    </div>
  );
}

import React from 'react';

export default function Card({
  children,
  title,
  error,
  success,
  isLoading,
  headerButtons,
}) {
  return (
    <div className="card mb-3">
      <div className="card-header">
        <div className=" d-flex justify-content-between">
          <h3 className="mb-0">{title}</h3>
          <div className="btn-group" role="group">
            {headerButtons}
          </div>
        </div>
      </div>
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

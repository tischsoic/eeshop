import React from 'react';

export default function ButtonWithSpinner({
  isDuringProcessing,
  label,
  labelProcessing,
  disabled = false,
  ...buttonAttributes
}) {
  return (
    <button {...buttonAttributes} disabled={isDuringProcessing || disabled}>
      {isDuringProcessing && (
        <span
          className="spinner-border spinner-border-sm"
          role="status"
          aria-hidden="true"
        />
      )}
      {isDuringProcessing ? ` ${labelProcessing}` : label}
    </button>
  );
}

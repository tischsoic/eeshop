import React from 'react';

import NavigationBar from './NavigationBar';

export default function Panel({ children }) {
  return (
    <>
      <NavigationBar />
      <div className="container">{children}</div>
    </>
  );
}

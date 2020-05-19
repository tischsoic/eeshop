import React, { useContext } from 'react';

import { UserContext } from '../providers/UserProvider';

import NavigationBar from './NavigationBar';

export default function Panel({ children }) {
  const { user } = useContext(UserContext);

  return (
    <>
      <NavigationBar />
      <div className="container">{children}</div>
    </>
  );
}
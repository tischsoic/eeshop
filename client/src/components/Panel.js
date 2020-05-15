import React, { useContext } from 'react';
import { Redirect } from 'react-router-dom';

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

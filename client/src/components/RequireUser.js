import React, { useContext } from 'react';
import { Redirect } from 'react-router-dom';
import { UserContext } from '../providers/UserProvider';

export default function RequireUser({ children }) {
  const { user } = useContext(UserContext);

  if (!user) {
    return <Redirect to="/" exact />;
  }

  return <>{children}</>;
}

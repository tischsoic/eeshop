import React, { createContext, useState, useCallback } from 'react';

export const UserContext = createContext(null);

export default function UserProvider({ children }) {
  const [user, setUser] = useState(() => {
    const userString = window.localStorage.getItem('eeshop-user');

    return userString ? JSON.parse(userString) : null;
  });
  const handleSetUser = useCallback(
    (userData) => {
      if (userData) {
        window.localStorage.setItem('eeshop-user', JSON.stringify(userData));
      } else {
        window.localStorage.removeItem('eeshop-user');
      }

      setUser(userData);
    },
    [setUser]
  );

  return (
    <UserContext.Provider value={{ user, setUser: handleSetUser }}>
      {children}
    </UserContext.Provider>
  );
}

import React, { createContext, useState, useCallback } from 'react';

export const UserContext = createContext(null);

export default function UserProvider({ children }) {
  const [user, setUser] = useState(() => {
    const userString = window.localStorage.getItem('eeshop-user');

    return userString ? JSON.parse(userString) : null;
  });
  const handleSetUser = useCallback(
    (user) => {
      if (user) {
        window.localStorage.setItem('eeshop-user', JSON.stringify(user));
      } else {
        window.localStorage.removeItem('eeshop-user');
      }

      setUser(user);
    },
    [setUser]
  );

  return (
    <UserContext.Provider value={{ user, setUser: handleSetUser }}>
      {children}
    </UserContext.Provider>
  );
}

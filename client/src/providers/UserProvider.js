import React, { createContext, useState, useEffect } from 'react';

export const UserContext = createContext(null);

export default function UserProvider({ children }) {
  const [user, setUser] = useState(() => {
    const userString = window.localStorage.getItem('eeshop-user');

    return userString ? JSON.parse(userString) : null;
  });
  function handleSetUser(user) {
    if (user) {
      window.localStorage.setItem('eeshop-user', JSON.stringify(user));
    } else {
      window.localStorage.removeItem('eeshop-user');
    }

    setUser(user);
  }

  return (
    <UserContext.Provider value={{ user, setUser: handleSetUser }}>
      {children}
    </UserContext.Provider>
  );
}

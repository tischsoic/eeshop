import React, { createContext, useState, useEffect } from 'react';

export const UserContext = createContext(null);

export default function UserProvider({ children }) {
  const [user, setUser] = useState(() => {
    const userString = window.localStorage.getItem('eeshop-user');

    return userString ? JSON.parse(userString) : null;
  });
  function handleSetUser(user) {
    window.localStorage.setItem('eeshop-user', JSON.stringify(user));
    setUser(user);
  }

  return (
    <UserContext.Provider value={{ user, setUser: handleSetUser }}>
      {children}
    </UserContext.Provider>
  );
}

import React, { createContext, useEffect, useState, useCallback } from 'react';

export const UserContext = createContext(null);

export default function UserProvider({ children }) {
  const [user, setUser] = useState({ id: 1 });

  return <UserContext.Provider value={user}>{children}</UserContext.Provider>;
}

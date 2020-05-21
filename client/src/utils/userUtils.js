export const isAdmin = (user) => user?.role === 'staff';
export const getToken = (user) => user?.token;
export const isSignedIn = (user) => !!user;

import React, { useContext } from 'react';
import { NavLink } from 'react-router-dom';
import { getUrl, getRequestInit } from '../utils/requestUtils';
import { isSignedIn, getToken, isAdmin } from '../utils/userUtils';

import { UserContext } from '../providers/UserProvider';
import OauthButton from './OauthButton';

export default function NavigationBar() {
  const { user, setUser } = useContext(UserContext);
  const signOut = () => {
    fetch(
      getUrl('sign-out'),
      getRequestInit({ method: 'GET' }, getToken(user))
    );
    setUser(null);
  };

  return (
    <nav className="navbar navbar-expand navbar-light bg-light justify-content-between mb-5 px-5">
      <div className="navbar-nav">
        <NavLink
          to="/"
          exact
          className="nav-item nav-link"
          activeClassName="active"
        >
          Products
        </NavLink>
        <NavLink
          to="/faq"
          className="nav-item nav-link"
          activeClassName="active"
        >
          FAQ
        </NavLink>
        {user && (
          <>
            <NavLink
              to="/orders"
              className="nav-item nav-link"
              activeClassName="active"
            >
              Orders
            </NavLink>
            <NavLink
              to="/checkout"
              className="nav-item nav-link"
              activeClassName="active"
            >
              Checkout
            </NavLink>
          </>
        )}
        {isAdmin(user) && (
          <>
            <NavLink
              to="/product"
              className="nav-item nav-link"
              activeClassName="active"
            >
              Products
            </NavLink>
            <NavLink
              to="/productType"
              className="nav-item nav-link"
              activeClassName="active"
            >
              Product Types
            </NavLink>
            <NavLink
              to="/allorders"
              className="nav-item nav-link"
              activeClassName="active"
            >
              All Orders
            </NavLink>
          </>
        )}
      </div>
      {isSignedIn(user) ? (
        <button
          className="btn btn-outline-danger my-2 my-sm-0"
          type="button"
          onClick={signOut}
        >
          Logout
        </button>
      ) : (
        <div>
          <OauthButton provider="google" title="Google login" />
          <OauthButton provider="facebook" title="FB login" />
        </div>
      )}
    </nav>
  );
}

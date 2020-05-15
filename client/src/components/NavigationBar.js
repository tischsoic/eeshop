import React, { useContext } from 'react';
import { Link, NavLink } from 'react-router-dom';

import { UserContext } from '../providers/UserProvider';

export default function NavigationBar() {
  const user = useContext(UserContext);
  const signOut = () => {};

  return (
    <nav className="navbar navbar-expand navbar-light bg-light justify-content-between mb-5 px-5">
      <div className="navbar-nav">
        <NavLink to="/" exact className="nav-item nav-link" activeClassName="active">Products</NavLink>
        <NavLink to="/orders" className="nav-item nav-link" activeClassName="active">Orders</NavLink>
        <NavLink to="/checkout" className="nav-item nav-link" activeClassName="active">Checkout</NavLink>
        <NavLink to="/faq" className="nav-item nav-link" activeClassName="active">FAQ</NavLink>
      </div>
      <button
        className="btn btn-outline-danger my-2 my-sm-0"
        type="button"
        onClick={signOut}
      >
        Logout
      </button>
    </nav>
  );
}

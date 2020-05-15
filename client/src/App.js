import React from 'react';
import {
  BrowserRouter as Router,
  Switch,
  Route,
  Redirect,
} from 'react-router-dom';
import './App.css';

import UserProvider from './providers/UserProvider';

import Products from './components/Products';
import Product from './components/Product';
import Checkout from './components/Checkout';
import Panel from './components/Panel';
import Orders from './components/Orders';
import Faq from './components/Faq';

function App() {
  return (
    <>
      <Router>
        <UserProvider>
          <main className="app">
            <Panel>
              <Switch>
                <Route path="/" exact>
                  <Products />
                </Route>
                <Route path="/product/:productId">
                  <Product />
                </Route>
                <Route path="/orders">
                  <Orders />
                </Route>
                <Route path="/checkout">
                  <Checkout />
                </Route>
                <Route path="/faq">
                  <Faq />
                </Route>
                <Route render={() => <Redirect to="/" />} />
              </Switch>
            </Panel>
          </main>
        </UserProvider>
      </Router>
    </>
  );
}

export default App;

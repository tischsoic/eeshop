import React from 'react';
import {
  BrowserRouter as Router,
  Switch,
  Route,
  Redirect,
} from 'react-router-dom';
import './App.css';

import UserProvider from './providers/UserProvider';

import ProductsCard from './components/ProductsCard';
import ProductCard from './components/ProductCard';
import Checkout from './components/Checkout';
import Panel from './components/Panel';
import Faq from './components/Faq';
import Oauth from './components/Oauth';
import FaqEdit from './components/FaqEdit';
import ProductTypeEdit from './components/ProductTypeEdit';
import ProductType from './components/ProductType';
import Product from './components/Product';
import ProductEdit from './components/ProductEdit';
import UserOrders from './components/UserOrders';
import RequireUser from './components/RequireUser';

function App() {
  return (
    <>
      <Router>
        <UserProvider>
          <main className="app">
            <Panel>
              <Switch>
                <Route path="/" exact>
                  <ProductsCard />
                </Route>
                <Route path="/oauth/:provider">
                  <Oauth />
                </Route>
                <Route path="/productCard/:productId">
                  <ProductCard />
                </Route>
                <Route path="/orders">
                  <RequireUser>
                    <UserOrders />
                  </RequireUser>
                </Route>
                <Route path="/checkout">
                  <RequireUser>
                    <Checkout />
                  </RequireUser>
                </Route>
                <Route path="/faq/edit/:faqNoteId?">
                  <RequireUser>
                    <FaqEdit />
                  </RequireUser>
                </Route>
                <Route path="/faq">
                  <Faq />
                </Route>
                <Route path="/product/edit/:id?">
                  <RequireUser>
                    <ProductEdit />
                  </RequireUser>
                </Route>
                <Route path="/product">
                  <RequireUser>
                    <Product />
                  </RequireUser>
                </Route>
                <Route path="/productType/edit/:id?">
                  <RequireUser>
                    <ProductTypeEdit />
                  </RequireUser>
                </Route>
                <Route path="/productType">
                  <RequireUser>
                    <ProductType />
                  </RequireUser>
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

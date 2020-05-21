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
import Orders from './components/Orders';
import Faq from './components/Faq';
import Oauth from './components/Oauth';
import FaqEdit from './components/FaqEdit';
import ProductTypeEdit from './components/ProductTypeEdit';
import ProductType from './components/ProductType';
import Product from './components/Product';
import ProductEdit from './components/ProductEdit';

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
                  <Orders />
                </Route>
                <Route path="/checkout">
                  <Checkout />
                </Route>
                <Route path="/faq/edit/:faqNoteId?">
                  <FaqEdit />
                </Route>
                <Route path="/faq">
                  <Faq />
                </Route>
                <Route path="/product/edit/:id?">
                  <ProductEdit />
                </Route>
                <Route path="/product">
                  <Product />
                </Route>
                <Route path="/productType/edit/:id?">
                  <ProductTypeEdit />
                </Route>
                <Route path="/productType">
                  <ProductType />
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

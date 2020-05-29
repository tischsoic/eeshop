import React, { useState, useEffect, useContext } from 'react';
import { getRequestInit, getUrl, parseJson } from '../utils/requestUtils';
import { Link } from 'react-router-dom';

import Card from './Card';
import { UserContext } from '../providers/UserProvider';

export default function ProductsCard() {
  const { setUser } = useContext(UserContext);
  const [products, setProducts] = useState(null);
  const [error, setError] = useState(null);
  const isFetchingData = !products;

  useEffect(() => {
    fetch(getUrl(`product`), getRequestInit({ method: 'GET' }))
      .then(parseJson)
      .then((fetchedProducts) => setProducts(fetchedProducts))
      .catch(() => {
        setError('Error while fetching products');
        setUser(null);
      });
  }, [setProducts, setUser]);

  return (
    <Card title="Products" isLoading={isFetchingData} error={error}>
      <div className="products">
        <ul className="list-group">
          {products &&
            products.map((product) => (
              <Link
                to={`/productCard/${product.productId}`}
                key={product.productId}
                className="list-group-item"
              >
                {product.name}
              </Link>
            ))}
        </ul>
      </div>
    </Card>
  );
}

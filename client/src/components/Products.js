import React, { useState, useEffect } from 'react';
import { getRequestInit, getUrl } from '../utils/requestUtils';
import { Link } from 'react-router-dom';

import Card from './Card';

export default function Products() {
  const [products, setProducts] = useState(null);
  const [error, setError] = useState(null);
  const isFetchingData = !products;

  useEffect(() => {
    fetch(getUrl(`product`), getRequestInit({ method: 'GET' }))
      .then((response) => response.json())
      .then((fetchedProducts) => setProducts(fetchedProducts))
      .catch(() => setError('Error while fetching products'));
  }, [setProducts]);

  console.log(products);

  return (
    <Card title="Products" isLoading={isFetchingData} error={error}>
      <div className="products">
        <ul className="list-group">
          {products &&
            products.map((product) => (
              <Link
                to={`/product/${product.productId}`}
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

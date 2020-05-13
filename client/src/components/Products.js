import React, { useState, useEffect } from 'react';

export default function Products() {
  const [products, setProducts] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch('http://localhost:9000/product', {
      mode: 'cors',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        'Access-Control-Allow-Origin': 'http://localhost:3000',
      },
      method: 'GET',
    })
      .then((response) => response.json())
      .then((products) => setProducts(products))
      .catch((error) => setError('Error while fetching products'));
  }, [setProducts]);

  console.log(products)

  return (
    <div className="product">
      {error && <div className="error">{error}</div>}
      {products && (
        <div className="list">
          {products.map((product) => (
            <div key={product.productId}>{product.name}</div>
          ))}
        </div>
      )}
    </div>
  );
}

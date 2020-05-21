import React from 'react';

import Elements from './Elements';

export default function Product() {
  const getElementId = (product) => product?.productId;
  const elementMapper = (product) => {
    return [
      {
        title: 'Id',
        value: product?.productId,
      },
      {
        title: 'Pr. Type Id',
        value: product?.productTypeId,
      },
      {
        title: 'Price',
        value: product?.price,
      },
      {
        title: 'Description',
        value: product?.description,
      },
      {
        title: 'Quantity',
        value: product?.quantity,
      },
    ];
  };

  return (
    <Elements
      elementType="product"
      title="Product"
      elementMapper={elementMapper}
      getElementId={getElementId}
    />
  );
}

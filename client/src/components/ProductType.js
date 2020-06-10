import React from 'react';

import Elements from './Elements';

export default function ProductType() {
  const getElementId = (productType) => productType?.productTypeId;
  const elementMapper = (productType) => {
    return [
      {
        title: 'Id',
        value: productType?.productTypeId,
      },
      {
        title: 'Name',
        value: productType?.name,
      },
      {
        title: 'Description',
        value: productType?.description,
      },
    ];
  };

  return (
    <Elements
      elementType="productType"
      title="Product Types"
      elementMapper={elementMapper}
      getElementId={getElementId}
    />
  );
}

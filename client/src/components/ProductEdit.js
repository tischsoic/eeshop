import React, { useState, useContext, useEffect } from 'react';
import { parseJson, getUrl, getRequestInit } from '../utils/requestUtils';
import { getToken } from '../utils/userUtils';

import { UserContext } from '../providers/UserProvider';
import ElementEdit from './ElementEdit';

export default function ProductEdit() {
  const { user, setUser } = useContext(UserContext);
  const [productTypes, setProductTypes] = useState(null);
  const mapElementToFields = (product) => {
    return {
      name: product?.name || '',
      price: product?.price || '',
      description: product?.description || '',
      quantity: product?.quantity || '',
      productTypeId: product?.productTypeId || '',
    };
  };

  useEffect(() => {
    fetch(
      getUrl('productType'),
      getRequestInit({ method: 'GET' }, getToken(user))
    )
      .then(parseJson)
      .then((fetchedProductTypes) => {
        setProductTypes(fetchedProductTypes);
      })
      .catch(() => {
        console.error('Error while fetching productTypes');
        setUser(null);
      });
  }, [setProductTypes, setUser, user]);

  return (
    <ElementEdit
      elementType="product"
      title="Product"
      mapElementToFields={mapElementToFields}
      renderFormFields={({
        fields,
        onChange,
        isDuringProcessing,
        onFieldChangeCustom,
      }) =>
        productTypes && (
          <>
            <div className="form-group">
              <label htmlFor="name">Name</label>
              <input
                type="text"
                className="form-control"
                id="name"
                name="name"
                placeholder="Name"
                value={fields.name}
                onChange={onChange}
                disabled={isDuringProcessing}
              />
            </div>
            <div className="form-group">
              <label htmlFor="price">Price</label>
              <input
                type="number"
                className="form-control"
                id="price"
                name="price"
                step="0.01"
                placeholder="Price"
                value={fields.price}
                onChange={(event) =>
                  onFieldChangeCustom(
                    'price',
                    parseFloat(event.currentTarget.value)
                  )
                }
                disabled={isDuringProcessing}
              />
            </div>
            <div className="form-group">
              <label htmlFor="description">Description</label>
              <input
                type="text"
                className="form-control"
                id="description"
                name="description"
                placeholder="Description"
                value={fields.description}
                onChange={onChange}
                disabled={isDuringProcessing}
              />
            </div>
            <div className="form-group">
              <label htmlFor="quantity">Quantity</label>
              <input
                type="number"
                className="form-control"
                id="quantity"
                name="quantity"
                placeholder="Quantity"
                value={fields.quantity}
                onChange={(event) =>
                  onFieldChangeCustom(
                    'quantity',
                    parseInt(event.currentTarget.value)
                  )
                }
                disabled={isDuringProcessing}
              />
            </div>
            <div className="form-group">
              <label htmlFor="productTypeId">Type</label>
              <select
                className="form-control"
                id="productTypeId"
                name="productTypeId"
                value={fields.productTypeId}
                onChange={(event) =>
                  onFieldChangeCustom(
                    'productTypeId',
                    parseInt(event.currentTarget.value)
                  )
                }
                disabled={isDuringProcessing}
              >
                {productTypes.map((productType) => (
                  <option
                    key={productType.productTypeId}
                    value={productType.productTypeId}
                  >
                    {productType.name}
                  </option>
                ))}
              </select>
            </div>
          </>
        )
      }
    />
  );
}

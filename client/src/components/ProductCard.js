import React, { useState, useEffect, useContext } from 'react';
import { useParams } from 'react-router-dom';
import { getUrl, getRequestInit, check400Status } from '../utils/requestUtils';
import useForm from '../hooks/useForm';

import { UserContext } from '../providers/UserProvider';

import Card from './Card';
import ButtonWithSpinner from './ButtonWithSpinner';

export default function ProductCard() {
  const { user } = useContext(UserContext);
  const { productId } = useParams();
  const [product, setProduct] = useState(null);
  const [
    fields,
    isDuringProcessing,
    ,
    setIsDuringProcessing,
    onFieldChangeCustom,
  ] = useForm({ quantity: 0 });
  const [success, setSuccess] = useState(null);
  const [error, setError] = useState(null);
  const isFetchingData = !product;
  const handleChangeQuantity = (event) => {
    const { name, value } = event.currentTarget;
    const newValue = Math.max(0, Math.min(100, Math.round(value)));
    onFieldChangeCustom(name, newValue);
  };
  const handleBuy = (event) => {
    event.preventDefault();
    setIsDuringProcessing(true);
    setSuccess(null);
    setError(null);

    fetch(
      getUrl(`order/item/${productId}/${fields.quantity}/${user.id}`),
      getRequestInit({ method: 'POST' })
    )
      .then((response) => {
        check400Status(response);
        setSuccess('Added to order');
      })
      .catch(() => setError('Error while adding product to order.'))
      .finally(() => setIsDuringProcessing(false));
  };

  useEffect(() => {
    fetch(getUrl(`product/${productId}`), getRequestInit({ method: 'GET' }))
      .then((response) => response.json())
      .then((fetchedProduct) => setProduct(fetchedProduct))
      .catch(() => setError('Error while fetching product data'));
  }, [setProduct]);

  console.log(product);

  return (
    <Card
      title={product ? product.name : '...'}
      isLoading={isFetchingData}
      error={error}
      success={success}
    >
      {product && (
        <div className="product">
          <p>{product.description}</p>
          <em>Type: {product.productType.name}</em>
          <form>
            <div className="form-row align-items-center justify-content-end">
              <div className="col-sm-auto">
                <h4>{product.price}$</h4>
              </div>
              <div className="col-sm-2">
                <label className="sr-only">Quantity</label>
                <input
                  type="number"
                  className="form-control"
                  id="quantity"
                  name="quantity"
                  placeholder=""
                  value={fields.quantity}
                  onChange={handleChangeQuantity}
                  disabled={isDuringProcessing}
                />
              </div>
              <div className="col-sm-auto">
                <ButtonWithSpinner
                  type="submit"
                  className="btn btn-primary"
                  onClick={handleBuy}
                  isDuringProcessing={isDuringProcessing}
                  label="Buy"
                  labelProcessing="Adding to order..."
                  disabled={!user || fields.quantity == '0'}
                />
              </div>
            </div>
          </form>
        </div>
      )}
    </Card>
  );
}
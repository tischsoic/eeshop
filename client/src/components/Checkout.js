import React, { useState, useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
import {
  getUrl,
  getRequestInit,
  parseJson,
  handleError,
} from '../utils/requestUtils';

import { UserContext } from '../providers/UserProvider';

import Card from './Card';
import ButtonWithSpinner from './ButtonWithSpinner';
import { getToken } from '../utils/userUtils';

export default function Checkout() {
  const { user } = useContext(UserContext);
  const [order, setOrder] = useState(null);
  const [invoice, setInvoice] = useState(null);
  const [payment, setPayment] = useState(null);
  const [stage, setStage] = useState('start');
  const [isDuringProcessing, setIsDuringProcessing] = useState(false);
  const [error, setError] = useState(null);
  const isFetchingData = !order;
  const deleteOrderItem = (orderItemId) => {
    setIsDuringProcessing(true);
    setError(null);

    console.log(orderItemId);
    fetch(
      getUrl(`orderItem/${orderItemId}`),
      getRequestInit({ method: 'DELETE' }, getToken(user))
    )
      .then(handleError)
      .then(() => {
        setOrder({
          ...order,
          items: [
            ...order.items.filter(
              (orderItem) => orderItem.orderItemId !== orderItemId
            ),
          ],
        });
        setIsDuringProcessing(false);
      })
      .catch(() => setError('Error while deleting order item'))
      .finally(() => setIsDuringProcessing(false));
  };
  const handleBuy = () => {
    setIsDuringProcessing(true);
    setError(null);

    fetch(
      getUrl(`invoice/${order.orderId}`),
      getRequestInit({ method: 'POST' })
    )
      .then(parseJson)
      .then((fetchedInvoice) => {
        setInvoice(fetchedInvoice);
        setStage('invoice');
      })
      .catch(() => setError('Error while buying'))
      .finally(() => setIsDuringProcessing(false));
  };
  const handlePay = () => {
    setIsDuringProcessing(true);
    setError(null);

    fetch(
      getUrl(`payment/${invoice.invoiceId}`),
      getRequestInit({ method: 'POST' }, getToken(user))
    )
      .then(parseJson)
      .then((fetchedPayment) => {
        setPayment(fetchedPayment);
        setStage('paid');
      })
      .catch(() => setError('Error while buying'))
      .finally(() => setIsDuringProcessing(false));
  };
  const computeTotalPrice = (orderItems) =>
    orderItems
      .reduce(
        (totalPrice, orderItem) =>
          totalPrice + orderItem.quantity * orderItem.price,
        0
      )
      .toFixed(2);

  useEffect(() => {
    setIsDuringProcessing(true);
    setError(null);

    fetch(
      getUrl(`order/checkout/${user.id}`),
      getRequestInit({ method: 'GET' }, getToken(user))
    )
      .then(parseJson)
      .then((fetchedOrder) => setOrder(fetchedOrder))
      .catch(() => setError('Error while fetching order data'))
      .finally(() => setIsDuringProcessing(false));
  }, [setOrder, user]);

  console.log(order);

  return (
    <Card title="Checkout" isLoading={isFetchingData} error={error}>
      {stage === 'start' && (
        <div>
          <table className="table table-striped mb-5">
            <thead>
              <tr>
                <th scope="col">Product</th>
                <th scope="col">Type</th>
                <th scope="col">Quantity</th>
                <th scope="col">Price</th>
                <th scope="col"></th>
              </tr>
            </thead>
            <tbody>
              {order &&
                order.items.map((orderItem) => (
                  <tr key={orderItem.orderItemId}>
                    <td>{orderItem.product.name}</td>
                    <td>{orderItem.product.productType.name}</td>
                    <td>{orderItem.quantity}</td>
                    <td>{orderItem.price}</td>
                    <td>
                      <div className="d-flex justify-content-end">
                        <ButtonWithSpinner
                          type="button"
                          className="btn btn-danger"
                          onClick={() => deleteOrderItem(orderItem.orderItemId)}
                          isDuringProcessing={isDuringProcessing}
                          label="Delete"
                          labelProcessing="Deleting..."
                        />
                      </div>
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
          {order && (
            <>
              <h4>Total price: {computeTotalPrice(order.items)}</h4>
              <ButtonWithSpinner
                type="button"
                className="btn btn-danger"
                onClick={handleBuy}
                isDuringProcessing={isDuringProcessing}
                label="Buy"
                labelProcessing="Buying..."
                disabled={order.items.length === 0}
              />
            </>
          )}
        </div>
      )}
      {stage === 'invoice' && (
        <div>
          <h3>Invoice no. {invoice.invoiceId}</h3>
          <h4>Total cost: {invoice.totalCost.toFixed(2)}</h4>
          <ButtonWithSpinner
            type="button"
            className="btn btn-danger"
            onClick={handlePay}
            isDuringProcessing={isDuringProcessing}
            label="Pay"
            labelProcessing="Paying..."
          />
        </div>
      )}
      {stage === 'paid' && (
        <div>
          <h3>
            {error
              ? 'Something went wrong.'
              : 'We received your payment. We will ship your products soon...'}
          </h3>
          <h2>
            <Link to="/" className="link">
              Go back to shopping
            </Link>
          </h2>
        </div>
      )}
    </Card>
  );
}

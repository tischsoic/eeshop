import React, { useState, useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
import { getUrl, getRequestInit } from '../utils/requestUtils';

import { UserContext } from '../providers/UserProvider';

import Card from './Card';
import ButtonWithSpinner from './ButtonWithSpinner';

export default function Checkout() {
  const { user } = useContext(UserContext);
  const [order, setOrder] = useState(null);
  const [invoice, setInvoice] = useState(null);
  const [payment, setPayment] = useState(null);
  const [stage, setStage] = useState('start');
  // const [orderItemsDuringDeleting, setOrderItemsDuringDeleting] = useState([])
  const [error, setError] = useState(null);
  const isFetchingData = !order;
  const deleteOrderItem = (orderItemId) => {
    console.log(orderItemId);
    fetch(
      getUrl(`orderItem/${orderItemId}`),
      getRequestInit({ method: 'DELETE' })
    )
      .then(() =>
        setOrder({
          ...order,
          items: [
            ...order.items.filter(
              (orderItem) => orderItem.orderItemId !== orderItemId
            ),
          ],
        })
      )
      .catch(() => setError('Error while deleting order item'));
  };
  const handleBuy = () => {
    fetch(
      getUrl(`invoice/${order.orderId}`),
      getRequestInit({ method: 'POST' })
    )
      .then((response) => response.json())
      .then((fetchedInvoice) => {
        setInvoice(fetchedInvoice);
        setStage('invoice');
      })
      .catch(() => setError('Error while buying'));
  };
  const handlePay = () => {
    fetch(
      getUrl(`payment/${invoice.invoiceId}`),
      getRequestInit({ method: 'POST' })
    )
      .then((response) => response.json())
      .then((fetchedPayment) => {
        setPayment(fetchedPayment);
        setStage('paid');
      })
      .catch(() => setError('Error while buying'));
  };
  const computeTotalPrice = (orderItems) =>
    orderItems.reduce(
      (totalPrice, orderItem) =>
        totalPrice + orderItem.quantity * orderItem.price,
      0
    );

  useEffect(() => {
    fetch(
      getUrl(`order/checkout/${user.id}`),
      getRequestInit({ method: 'GET' })
    )
      .then((response) => response.json())
      .then((fetchedOrder) => setOrder(fetchedOrder))
      .catch(() => setError('Error while fetching order data'));
  }, [setOrder]);

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
                          // isDuringProcessing={isDuringProcessing}
                          label="Delete"
                          labelProcessing="Deleting..."
                        />
                      </div>
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>{' '}
          {order && (
            <>
              <h4>Total price: {computeTotalPrice(order.items)}</h4>
              <ButtonWithSpinner
                type="button"
                className="btn btn-danger"
                onClick={handleBuy}
                // isDuringProcessing={isDuringProcessing}
                label="Buy"
                labelProcessing="Buying..."
              />
            </>
          )}
        </div>
      )}
      {stage === 'invoice' && (
        <div>
          {JSON.stringify(invoice)}
          <ButtonWithSpinner
            type="button"
            className="btn btn-danger"
            onClick={handlePay}
            // isDuringProcessing={isDuringProcessing}
            label="Pay"
            labelProcessing="Paying..."
          />
        </div>
      )}
      {stage === 'paid' && (
        <div>
          {JSON.stringify(payment)}
          <Link to="/" className="link">
            Go back to shoppin
          </Link>
        </div>
      )}
    </Card>
  );
}

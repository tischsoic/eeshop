import React, { useEffect, useContext } from 'react';
import { useState } from 'react';
import { parseJson, getUrl, getRequestInit } from '../utils/requestUtils';
import { getToken } from '../utils/userUtils';

import { UserContext } from '../providers/UserProvider';
import Card from './Card';
import AllOrdersOrder from './AllOrdersOrder';

export default function AllOrders() {
  const { user, setUser } = useContext(UserContext);
  const [error, setError] = useState(null);
  const [orders, setOrders] = useState(null);
  const [isDuringProcessing, setIsDuringProcessing] = useState(null);
  const isFetchingData = !orders;
  const handleSent = (orderId, trackingCode) => (event) => {
    event.preventDefault();
    setIsDuringProcessing(true);
    setError(null);

    fetch(
      getUrl('shipment/for-order'),
      getRequestInit(
        {
          method: 'POST',
          body: JSON.stringify({
            orderId,
            date: new Date(),
            trackingCode,
          }),
        },
        getToken(user)
      )
    )
      .then(parseJson)
      .then((fetchedOrders) => setOrders(fetchedOrders))
      .catch((requestError) => {
        setError(requestError.message);
        setUser(null);
      })
      .finally(() => setIsDuringProcessing(false));
  };
  const handleDelivered = (orderId) => (event) => {
    event.preventDefault();
    setIsDuringProcessing(true);
    setError(null);

    fetch(
      getUrl(`order/${orderId}/delivered`),
      getRequestInit({ method: 'PUT' }, getToken(user))
    )
      .then(parseJson)
      .then((fetchedOrders) => setOrders(fetchedOrders))
      .catch((requestError) => {
        setError(requestError.message);
        setUser(null);
      })
      .finally(() => setIsDuringProcessing(false));
  };

  useEffect(() => {
    setError(null);

    fetch(getUrl(`order`), getRequestInit({ method: 'GET' }, getToken(user)))
      .then(parseJson)
      .then((fetchedOrders) => setOrders(fetchedOrders))
      .catch(() => {
        setError('Error while fetching orders data');
        setUser(null);
      });
  }, [setOrders, user, setUser]);

  return (
    <Card title="All Orders" isLoading={isFetchingData} error={error}>
      {orders && (
        <table className="table table-striped mb-5">
          <thead>
            <tr>
              <th scope="col">Order id</th>
              <th scope="col">Status</th>
              <th scope="col"></th>
            </tr>
          </thead>
          <tbody>
            {orders &&
              orders.map((order) => (
                <AllOrdersOrder
                  key={order.orderId}
                  order={order}
                  handleSent={handleSent}
                  handleDelivered={handleDelivered}
                  isDuringProcessing={isDuringProcessing}
                />
              ))}
          </tbody>
        </table>
      )}
    </Card>
  );
}

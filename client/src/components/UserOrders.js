import React, { useEffect, useContext } from 'react';
import { useState } from 'react';
import { parseJson, getUrl, getRequestInit } from '../utils/requestUtils';
import { getToken } from '../utils/userUtils';

import { UserContext } from '../providers/UserProvider';
import Card from './Card';

export default function UserOrders() {
  const { user, setUser } = useContext(UserContext);
  const [error, setError] = useState(null);
  const [orders, setOrders] = useState(null);
  const isFetchingData = !orders;

  useEffect(() => {
    setError(null);

    fetch(
      getUrl(`order/by-user`),
      getRequestInit({ method: 'GET' }, getToken(user))
    )
      .then(parseJson)
      .then((fetchedOrders) => setOrders(fetchedOrders))
      .catch(() => setError('Error while fetching orders data'));
  }, [setOrders]);

  console.log(orders);

  return (
    <Card title="Your Orders" isLoading={isFetchingData} error={error}>
      {orders && (
        <table className="table table-striped mb-5">
          <thead>
            <tr>
              <th scope="col">Order id</th>
              <th scope="col">Status</th>
            </tr>
          </thead>
          <tbody>
            {orders &&
              orders.map((order) => (
                <tr key={order.orderId}>
                  <td>{order.orderId}</td>
                  <td>{order.status}</td>
                </tr>
              ))}
          </tbody>
        </table>
      )}
    </Card>
  );
}

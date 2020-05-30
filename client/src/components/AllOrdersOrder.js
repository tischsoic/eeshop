import React from 'react';
import useForm from '../hooks/useForm';

import ButtonWithSpinner from './ButtonWithSpinner';

export default function AllOrdersOrder({
  order,
  handleSent,
  handleDelivered,
  isDuringProcessing,
}) {
  const [fields, , onChange, , , ,] = useForm({ code: '' });

  return (
    <tr>
      <td>{order.orderId}</td>
      <td>{order.status}</td>
      {order.status === 'awaiting_payment' && (
        <td style={{ textAlign: 'center' }}>--------</td>
      )}
      {order.status === 'pack' && (
        <td>
          <form className="form-inline justify-content-end">
            <div className="form-group mr-2">
              <input
                type="text"
                className="form-control"
                id="code"
                name="code"
                placeholder="Shipment code"
                value={fields.code}
                onChange={onChange}
                disabled={isDuringProcessing}
              />
            </div>
            <ButtonWithSpinner
              type="submit"
              className="btn btn-primary"
              onClick={handleSent(order.orderId, fields.code)}
              isDuringProcessing={isDuringProcessing}
              label="Ship"
              labelProcessing="Ship"
            />
          </form>
        </td>
      )}
      {order.status === 'sent' && (
        <td style={{ textAlign: 'end' }}>
          <ButtonWithSpinner
            type="button"
            className="btn btn-primary"
            onClick={handleDelivered(order.orderId)}
            isDuringProcessing={isDuringProcessing}
            label="Mark as delivered"
            labelProcessing="Mark as delivered"
          />
        </td>
      )}
      {order.status === 'delivered' && (
        <td style={{ textAlign: 'center' }}>
          <i>Completed</i>
        </td>
      )}
    </tr>
  );
}

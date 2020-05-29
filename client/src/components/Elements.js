import React, { useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
import { useState } from 'react';
import {
  getUrl,
  getRequestInit,
  parseJson,
  handleError,
} from '../utils/requestUtils';
import { isAdmin, getToken } from '../utils/userUtils';
import { UserContext } from '../providers/UserProvider';

import Card from './Card';

export default function Elements({
  elementType,
  title,
  elementMapper,
  getElementId,
}) {
  const { user, setUser } = useContext(UserContext);
  const [elements, setElements] = useState(null);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [forceRefresh, setFroceRefresh] = useState(0);
  const isFetchingData = !elements;
  const handleElementDelete = (elementId) => {
    setError(null);
    setSuccess(null);

    fetch(
      getUrl(`${elementType}/${elementId}`),
      getRequestInit({ method: 'DELETE' }, getToken(user))
    )
      .then(handleError)
      .then(() => {
        setFroceRefresh(forceRefresh + 1);
        setSuccess('Element deleted.');
      })
      .catch(() => {
        setError('Something went wrong while deleting element.');
        // setUser(null);
      });
  };

  useEffect(() => {
    fetch(
      getUrl(elementType),
      getRequestInit({ method: 'GET' }, getToken(user))
    )
      .then(parseJson)
      .then((fetchedElements) => {
        setElements(fetchedElements);
      })
      .catch(() => {
        setError('Error while fetching');
        setUser(null);
      });
  }, [setElements, forceRefresh, setUser, user]);

  const headerButtons = isAdmin(user) ? (
    <Link to={`/${elementType}/edit`} className="btn btn-success">
      Add
    </Link>
  ) : null;

  return (
    <Card
      title={title}
      isLoading={isFetchingData}
      error={error}
      success={success}
      headerButtons={headerButtons}
    >
      <table className="table table-striped">
        <thead>
          <tr>
            {elementMapper(null).map(({ title }) => (
              <th scope="col" key={title}>
                {title}
              </th>
            ))}
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          {elements &&
            elements.map((element) => (
              <tr key={getElementId(element)}>
                {elementMapper(element).map(({ value, title }) => (
                  <td key={title}>{value}</td>
                ))}
                <td>
                  <div className="d-flex justify-content-end">
                    <Link
                      to={`${elementType}/edit/${getElementId(element)}`}
                      className="btn btn-secondary mr-2"
                    >
                      Edit
                    </Link>
                    <button
                      className="btn btn-danger"
                      type="button"
                      onClick={() => handleElementDelete(getElementId(element))}
                    >
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
        </tbody>
      </table>
    </Card>
  );
}

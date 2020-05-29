import React, { useEffect, useContext } from 'react';
import { Redirect, useParams } from 'react-router-dom';
import { useState } from 'react';
import {
  getUrl,
  getRequestInit,
  parseJson,
  handleError,
} from '../utils/requestUtils';
import { getToken } from '../utils/userUtils';
import useForm from '../hooks/useForm';

import { UserContext } from '../providers/UserProvider';
import Card from './Card';
import ButtonWithSpinner from './ButtonWithSpinner';

export default function ElementEdit({
  elementType,
  title: cardTitle,
  mapElementToFields,
  renderFormFields,
}) {
  const { user, setUser } = useContext(UserContext);
  const { id } = useParams();
  const isNew = !id;
  const [error, setError] = useState(null);
  const [redirect, setRedirect] = useState(false);
  const [isFetchingData, setIsFetchingData] = useState(!isNew);
  const [
    fields,
    isDuringProcessing,
    onChange,
    setIsDuringProcessing,
    onFieldChangeCustom,
    resetFields,
  ] = useForm(mapElementToFields(null));
  const handleCreate = (event) => {
    event.preventDefault();
    setIsDuringProcessing(true);
    setError(null);

    fetch(
      getUrl(elementType),
      getRequestInit(
        { method: 'POST', body: JSON.stringify(fields) },
        getToken(user)
      )
    )
      .then(handleError)
      .then(() => setRedirect(true))
      .catch((requestError) => {
        setError(requestError.message);
        setIsDuringProcessing(false);
        setUser(null);
      });
  };
  const handleUpdate = (event) => {
    event.preventDefault();
    setIsDuringProcessing(true);
    setError(null);

    fetch(
      getUrl(`${elementType}/${id}`),
      getRequestInit(
        { method: 'PUT', body: JSON.stringify(fields) },
        getToken(user)
      )
    )
      .then(handleError)
      .then(() => {
        setRedirect(true);
      })
      .catch((requestError) => {
        setError(requestError.message);
        setIsDuringProcessing(false);
        setUser(null);
      });
  };

  useEffect(() => {
    if (isNew) {
      return;
    }

    fetch(
      getUrl(`${elementType}/${id}`),
      getRequestInit({ method: 'GET' }, getToken(user))
    )
      .then(parseJson)
      .then((fetchedElement) => {
        resetFields(mapElementToFields(fetchedElement));
        setIsFetchingData(false);
      })
      .catch(() => {
        setError('Error while fetching element.');
        setUser(null);
      });
  }, [resetFields, setIsFetchingData, id, user, setUser, elementType, mapElementToFields, isNew]);

  if (redirect) {
    return <Redirect to={`/${elementType}`} />;
  }

  return (
    <Card title={cardTitle} isLoading={isFetchingData} error={error}>
      <form>
        {renderFormFields({
          fields,
          onChange,
          isDuringProcessing,
          onFieldChangeCustom,
        })}
        {isNew && (
          <ButtonWithSpinner
            type="submit"
            className="btn btn-primary"
            onClick={handleCreate}
            isDuringProcessing={isDuringProcessing}
            label="Create"
            labelProcessing="Creating..."
          />
        )}
        {!isNew && (
          <ButtonWithSpinner
            type="submit"
            className="btn btn-primary"
            onClick={handleUpdate}
            isDuringProcessing={isDuringProcessing}
            label="Update"
            labelProcessing="Updating..."
          />
        )}
      </form>
    </Card>
  );
}

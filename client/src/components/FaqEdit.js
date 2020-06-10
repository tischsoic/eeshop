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
import { UserContext } from '../providers/UserProvider';
import useForm from '../hooks/useForm';

import Card from './Card';
import ButtonWithSpinner from './ButtonWithSpinner';

export default function FaqEdit() {
  const { user, setUser } = useContext(UserContext);
  const { faqNoteId } = useParams();
  const isNew = !faqNoteId;
  const [error, setError] = useState(null);
  const [redirect, setRedirect] = useState(false);
  const [isFetchingData, setIsFetchingData] = useState(!isNew);
  const [
    fields,
    isDuringProcessing,
    onChange,
    setIsDuringProcessing,
    ,
    resetFields,
  ] = useForm({ title: '', message: '' });
  const handleCreate = (event) => {
    event.preventDefault();
    setIsDuringProcessing(true);
    setError(null);

    fetch(
      getUrl('faqNote'),
      getRequestInit(
        { method: 'POST', body: JSON.stringify(fields) },
        getToken(user)
      )
    )
      .then(handleError)
      .then(() => setRedirect(true))
      .catch((requestError) => {
        setError(requestError.message);
        setUser(null);
      })
      .finally(() => setIsDuringProcessing(false));
  };
  const handleUpdate = (event) => {
    event.preventDefault();
    setIsDuringProcessing(true);
    setError(null);

    fetch(
      getUrl(`faqNote/${faqNoteId}`),
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
        setUser(null);
      })
      .finally(() => setIsDuringProcessing(false));
  };

  useEffect(() => {
    if (isNew) {
      return;
    }

    fetch(
      getUrl(`faqNote/${faqNoteId}`),
      getRequestInit({ method: 'GET' }, getToken(user))
    )
      .then(parseJson)
      .then((fetchedFaqNote) => {
        resetFields(fetchedFaqNote);
        setIsFetchingData(false);
      })
      .catch(() => {
        setError('Error while fetching FAQ');
        setUser(null);
      });
  }, [resetFields, setIsFetchingData, faqNoteId, user, isNew, setUser]);

  if (redirect) {
    return <Redirect to="/faq" />;
  }

  return (
    <Card title="FAQ" isLoading={isFetchingData} error={error}>
      <form>
        <div className="form-group">
          <label htmlFor="email">Title</label>
          <input
            type="text"
            className="form-control"
            id="title"
            name="title"
            placeholder="Title"
            value={fields.title}
            onChange={onChange}
            disabled={isDuringProcessing}
          />
        </div>
        <div className="form-group">
          <label htmlFor="password">Message</label>
          <input
            type="text"
            className="form-control"
            id="message"
            name="message"
            placeholder="Message"
            value={fields.message}
            onChange={onChange}
            disabled={isDuringProcessing}
          />
        </div>
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

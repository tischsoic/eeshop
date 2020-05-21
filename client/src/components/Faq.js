import React, { useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
import { useState } from 'react';
import {
  getUrl,
  getRequestInit,
  check400Status,
  parseJson,
} from '../utils/requestUtils';
import { isAdmin, getToken } from '../utils/userUtils';
import { UserContext } from '../providers/UserProvider';

import Card from './Card';

export default function Faq() {
  const { user, setUser } = useContext(UserContext);
  const [faqNotes, setFaqNotes] = useState(null);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [forceRefresh, setFroceRefresh] = useState(0);
  const isFetchingData = !faqNotes;
  const handleFaqNoteDelete = (faqNoteId) => {
    setError(null);
    setSuccess(null);

    fetch(
      getUrl(`faqNote/${faqNoteId}`),
      getRequestInit({ method: 'DELETE' }, getToken(user))
    )
      .then((response) => {
        check400Status(response);
        setFroceRefresh(forceRefresh + 1);
        setSuccess('FAQ Note deleted');
      })
      .catch(() => {
        setError('Error while deleting FAQ note');
        setUser(null);
      });
  };

  useEffect(() => {
    fetch(getUrl('faqNote'), getRequestInit({ method: 'GET' }, getToken(user)))
      .then(parseJson)
      .then((fetchedFaqNotes) => {
        setFaqNotes(fetchedFaqNotes);
      })
      .catch(() => {
        setError('Error while fetching FAQ');
        setUser(null);
      });
  }, [setFaqNotes, forceRefresh, setUser, user]);

  const headerButtons = isAdmin(user) ? (
    <Link to="/faq/edit" className="btn btn-success">
      Add
    </Link>
  ) : null;

  return (
    <Card
      title="FAQ"
      isLoading={isFetchingData}
      error={error}
      success={success}
      headerButtons={headerButtons}
    >
      <ul className="list-group">
        {faqNotes &&
          faqNotes.map((faqNote) => (
            <li key={faqNote.faqNoteId} className="list-group-item">
              <div className="d-flex justify-content-between">
                <div>
                  <h4>{faqNote.title}</h4>
                  <p className="text-left">{faqNote.message}</p>
                </div>
                {isAdmin(user) && (
                  <div>
                    <div className="d-flex">
                      <Link
                        to={`faq/edit/${faqNote.faqNoteId}`}
                        className="btn btn-secondary mr-2"
                      >
                        Edit
                      </Link>
                      <button
                        className="btn btn-danger"
                        type="button"
                        onClick={() => handleFaqNoteDelete(faqNote.faqNoteId)}
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </li>
          ))}
      </ul>
    </Card>
  );
}

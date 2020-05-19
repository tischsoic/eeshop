import React, { useEffect, useContext } from 'react';
import { useState } from 'react';
import { getUrl, getRequestInit } from '../utils/requestUtils';
import { UserContext } from '../providers/UserProvider';

import Card from './Card';

export default function Faq() {
  const { user } = useContext(UserContext);
  const token = user ? user.token : '';
  const [faqNotes, setFaqNotes] = useState(null);
  const [error, setError] = useState(null);
  const isFetchingData = !faqNotes;

  useEffect(() => {
    fetch(getUrl('faqNote'), getRequestInit({ method: 'GET' }, token))
      .then((response) => {
        if (response.status >= 400 && response.status < 600) {
          throw new Error('Bad response from server');
        }
        return response.json();
      })
      .then((fetchedFaqNotes) => {
        setFaqNotes(fetchedFaqNotes);
      })
      .catch(() => setError('Error while fetching FAQ'));
  }, [setFaqNotes]);

  return (
    <Card title="FAQ" isLoading={isFetchingData} error={error}>
      <ul className="list-group">
        {faqNotes &&
          faqNotes.map((faqNote) => (
            <li key={faqNote.faqNoteId} className="list-group-item">
              <h4>{faqNote.title}</h4>
              <p className="text-left">{faqNote.message}</p>
            </li>
          ))}
      </ul>
    </Card>
  );
}

import React, { useEffect } from 'react';
import { useState } from 'react';
import { getUrl, getRequestInit } from '../utils/requestUtils';

import Card from './Card';

export default function Faq() {
  const [faqNotes, setFaqNotes] = useState(null);
  const [error, setError] = useState(null);
  const isFetchingData = !faqNotes;

  useEffect(() => {
    fetch(getUrl('faqNote'), getRequestInit({ method: 'GET' }))
      .then((response) => response.json())
      .then((fetchedFaqNotes) => setFaqNotes(fetchedFaqNotes))
      .catch(() => setError('Error while fetching FAQ'));
  }, [setFaqNotes]);

  return (
    <Card title="FAQ" isLoading={isFetchingData} error={error}>
      <ul class="list-group">
        {faqNotes &&
          faqNotes.map((faqNote) => (
            <li key={faqNote.faqNoteId} class="list-group-item">
              <h4>{faqNote.title}</h4>
              <p class="text-left">{faqNote.message}</p>
            </li>
          ))}
      </ul>
    </Card>
  );
}

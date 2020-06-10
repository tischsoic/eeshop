export const getUrl = (endpoint) => `http://localhost:9000/api/${endpoint}`;

export const getRequestInit = (init, token) => ({
  mode: 'cors',
  headers: new Headers({
    Accept: 'application/json',
    'Content-Type': 'application/json',
    'Access-Control-Allow-Origin': 'http://localhost:3000',
    'X-Auth-Token': token,
  }),
  ...init,
});

export const parseJson = (response) => {
  handleError(response);
  return response.json();
};

export const handleError = (response) => {
  if (!response.ok) {
    throw new Error('Bad response from server');
  }
  return response;
};

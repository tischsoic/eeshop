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
  check400Status(response);
  return response.json();
};

export const check400Status = (response) => {
  if (response.status >= 400 && response.status < 500) {
    throw new Error('Bad response from server');
  }
};

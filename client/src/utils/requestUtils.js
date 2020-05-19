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

import React, { useEffect, useContext } from 'react';
import { getUrl, getRequestInit } from '../utils/requestUtils';
import { UserContext } from '../providers/UserProvider';

let existingWindow = null;

export default function OauthButton({ provider }) {
  const { setUser } = useContext(UserContext);
  function handleAuthentication() {
    window.socialProviderCallback = function (provider, queryParams) {
      fetch(
        `${getUrl('auth')}?${queryParams}`,
        getRequestInit({ method: 'GET' })
      )
        .then((response) => response.json())
        .then((fetchedUser) => {
          console.log(fetchedUser);
          setUser(fetchedUser)
        })
        .catch(function (response) {
          console.log('Error on social auth');
          console.log(response);
        });
    };

    if (existingWindow) {
      existingWindow.close();
    }

    const w = 500;
    const h = 500;
    const dualScreenLeft =
      window.screenLeft != undefined ? window.screenLeft : window.screenX;
    const dualScreenTop =
      window.screenTop != undefined ? window.screenTop : window.screenY;

    const width = window.innerWidth
      ? window.innerWidth
      : document.documentElement.clientWidth
      ? document.documentElement.clientWidth
      : screen.width;
    const height = window.innerHeight
      ? window.innerHeight
      : document.documentElement.clientHeight
      ? document.documentElement.clientHeight
      : screen.height;

    const systemZoom = width / window.screen.availWidth;
    const left = (width - w) / 2 / systemZoom + dualScreenLeft;
    const top = (height - h) / 2 / systemZoom + dualScreenTop;
    existingWindow = window.open(
      '/api/auth',
      'Authentication',
      'scrollbars=yes, width=' +
        w / systemZoom +
        ', height=' +
        h / systemZoom +
        ', top=' +
        top +
        ', left=' +
        left
    );
  }

  return (
    <button
      type="button"
      className="btn btn-primary"
      onClick={handleAuthentication}
    >
      Google Auth
    </button>
  );
}

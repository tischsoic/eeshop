/* global gapi */
import React, { useState, useEffect } from 'react';

import Card from './Card';

export default function SignIn() {
  const [user, setUser] = useState(null);
  const [error, setError] = useState(null);
  const isFetchingData = false;
  function handleSuccess(googleUser) {
    console.log(arguments);
    const profile = googleUser.getBasicProfile();
    console.log('ID: ' + profile.getId()); // Do not send to your backend! Use an ID token instead.
    console.log('Name: ' + profile.getName());
    console.log('Image URL: ' + profile.getImageUrl());
    console.log('Email: ' + profile.getEmail()); // This is null if the 'email' scope is not present.
  }
  function handleFailure() {
    console.log(arguments);
  }
  function signOut() {
    var auth2 = gapi.auth2.getAuthInstance();
    auth2.signOut().then(function () {
      console.log('User signed out.');
    });
  }

  useEffect(() => {
    window.gapi.load('auth2', () => {
      gapi.auth2.init({
        client_id:
          '598094074635-rtrdt7qrharegegae73nerbobrsk27lr.apps.googleusercontent.com',
      });

      window.gapi.load('signin2', function () {
        gapi.signin2.render('sing-in-btn', {
          scope: 'profile email',
          width: 240,
          height: 50,
          longtitle: true,
          theme: 'dark',
          onsuccess: handleSuccess,
          onfailure: handleFailure,
        });
      });
    });
  }, []);

  return (
    <Card title="Sign In" isLoading={isFetchingData} error={error}>
      {/* <button id="loginButton">Login with Google</button> */}
      <div id="sing-in-btn"></div>
      <button onClick={signOut}>Sign out</button>
    </Card>
  );
}

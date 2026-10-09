# Browser client

`@nextramice/client` is the framework-neutral TypeScript client for the BFF HTTP
contract. It always sends browser credentials and automatically obtains and
applies a CSRF token before unsafe requests.

The package currently lives in this repository and is not published to npm.

## Build

```bash
npm install
npm run build
npm test
```

## Create a client

```ts
import { NextramiceClient } from "@nextramice/client";

const auth = new NextramiceClient({
  baseUrl: "/nextramice",
});
```

For a separately hosted backend, provide its absolute URL and configure CORS,
cookie domain/SameSite behavior, trusted origins, and HTTPS on the backend:

```ts
const auth = new NextramiceClient({
  baseUrl: "https://api.example.test/nextramice",
});
```

## Read the session

```ts
const session = await auth.getSession();

if (session.authenticated) {
  console.log(session.principal?.displayName);
}
```

## External provider login

```ts
const providers = await auth.getProviders();
const authentik = providers.find((provider) => provider.id === "authentik");

if (authentik) {
  window.location.assign(auth.resolveLoginUrl(authentik));
}
```

Only use provider URLs returned by the backend. The client resolves relative
login URLs against the current page origin.

## Internal login

```ts
const session = await auth.login({
  username: "mouse@example.test",
  password: enteredPassword,
});
```

The password is sent only in the HTTPS request body and is never retained by the
client instance.

## Registration

```ts
const result = await auth.register({
  username: "mouse@example.test",
  password: enteredPassword,
  attributes: {
    displayName: "Mouse User",
  },
});

if (result.status === "VERIFICATION_REQUIRED") {
  // Show the application's verification instructions.
}
```

## Logout

```ts
await auth.logout();
```

`login`, `register`, and `logout` automatically fetch `/csrf`, cache the token,
and send the configured CSRF header. A failed unsafe request clears the cached
token so the next attempt obtains a fresh one.

## Custom fetch

Tests, server runtimes, and applications with tracing can provide a compatible
fetch implementation:

```ts
const auth = new NextramiceClient({
  fetch: tracedFetch,
});
```

The client throws `NextramiceHttpError` for non-success responses. The error
contains the HTTP status and parsed response body but never stores submitted
credentials.

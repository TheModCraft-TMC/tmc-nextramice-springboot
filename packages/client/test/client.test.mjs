import assert from "node:assert/strict";
import test from "node:test";

import {
  NextramiceClient,
  NextramiceHttpError,
} from "../dist/index.js";

test("reads a session with browser credentials", async () => {
  const requests = [];
  const client = new NextramiceClient({
    fetch: async (url, init) => {
      requests.push({ url, init });
      return jsonResponse({ authenticated: false });
    },
  });

  const session = await client.getSession();

  assert.equal(session.authenticated, false);
  assert.equal(requests[0].url, "/nextramice/session");
  assert.equal(requests[0].init.credentials, "include");
});

test("loads one CSRF token and applies it to unsafe requests", async () => {
  const requests = [];
  const client = new NextramiceClient({
    fetch: async (url, init) => {
      requests.push({ url, init });
      if (url.endsWith("/csrf")) {
        return jsonResponse({
          headerName: "X-CSRF-TOKEN",
          parameterName: "_csrf",
          token: "csrf-value",
        });
      }
      return jsonResponse({
        authenticated: true,
        principal: { id: "1", username: "mouse", displayName: null },
        authorities: [],
        claims: {},
        expiresAt: null,
        loginMethod: "local",
        provider: "local",
      });
    },
  });

  await client.login({ username: "mouse", password: "secret" });
  await client.login({ username: "mouse", password: "secret" });

  assert.equal(requests.filter((request) => request.url.endsWith("/csrf")).length, 1);
  const login = requests.find((request) => request.url.endsWith("/login"));
  assert.equal(login.init.headers.get("X-CSRF-TOKEN"), "csrf-value");
  assert.equal(login.init.method, "POST");
});

test("throws a typed error without retaining submitted credentials", async () => {
  const client = new NextramiceClient({
    fetch: async (url) => url.endsWith("/csrf")
      ? jsonResponse({
          headerName: "X-CSRF-TOKEN",
          parameterName: "_csrf",
          token: "csrf-value",
        })
      : jsonResponse({ message: "Authentication failed" }, 401),
  });

  await assert.rejects(
    client.login({ username: "mouse", password: "do-not-retain" }),
    (error) => {
      assert.ok(error instanceof NextramiceHttpError);
      assert.equal(error.status, 401);
      assert.equal(JSON.stringify(error).includes("do-not-retain"), false);
      return true;
    },
  );
});

function jsonResponse(body, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

import type {
  NextramiceClientOptions,
  NextramiceCsrfToken,
  NextramiceLoginRequest,
  NextramiceProvider,
  NextramiceRegistrationRequest,
  NextramiceRegistrationResult,
  NextramiceSession,
} from "./types.js";

export class NextramiceHttpError extends Error {
  readonly status: number;
  readonly body: unknown;

  constructor(status: number, body: unknown) {
    super(`Nextramice request failed with status ${status}`);
    this.name = "NextramiceHttpError";
    this.status = status;
    this.body = body;
  }
}

export class NextramiceClient {
  readonly baseUrl: string;
  readonly fetch: typeof globalThis.fetch;

  #csrfToken: NextramiceCsrfToken | undefined;
  #csrfRequest: Promise<NextramiceCsrfToken> | undefined;

  constructor(options: NextramiceClientOptions = {}) {
    this.baseUrl = normalizeBaseUrl(options.baseUrl ?? "/nextramice");
    this.fetch = options.fetch ?? globalThis.fetch.bind(globalThis);
  }

  getSession(): Promise<NextramiceSession> {
    return this.#request<NextramiceSession>("/session");
  }

  getProviders(): Promise<NextramiceProvider[]> {
    return this.#request<NextramiceProvider[]>("/providers");
  }

  async getCsrfToken(options: { refresh?: boolean } = {}): Promise<NextramiceCsrfToken> {
    if (options.refresh) {
      this.#csrfToken = undefined;
      this.#csrfRequest = undefined;
    }
    if (this.#csrfToken) {
      return this.#csrfToken;
    }
    if (!this.#csrfRequest) {
      this.#csrfRequest = this.#request<NextramiceCsrfToken>("/csrf")
        .then((token) => {
          this.#csrfToken = token;
          return token;
        })
        .finally(() => {
          this.#csrfRequest = undefined;
        });
    }
    return this.#csrfRequest;
  }

  login(credentials: NextramiceLoginRequest): Promise<NextramiceSession> {
    return this.#unsafeRequest<NextramiceSession>("/login", credentials);
  }

  register(request: NextramiceRegistrationRequest): Promise<NextramiceRegistrationResult> {
    return this.#unsafeRequest<NextramiceRegistrationResult>("/register", request);
  }

  async logout(): Promise<void> {
    await this.#unsafeRequest<void>("/logout");
    this.#csrfToken = undefined;
  }

  resolveLoginUrl(provider: NextramiceProvider, origin?: string): string {
    const baseOrigin = origin ?? globalThis.location?.origin;
    if (!baseOrigin) {
      throw new Error("An origin is required outside a browser environment");
    }
    return new URL(provider.loginUrl, baseOrigin).toString();
  }

  async #unsafeRequest<T>(path: string, body?: unknown): Promise<T> {
    const csrf = await this.getCsrfToken();
    try {
      return await this.#request<T>(path, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          [csrf.headerName]: csrf.token,
        },
        ...(body === undefined ? {} : { body: JSON.stringify(body) }),
      });
    } catch (error) {
      this.#csrfToken = undefined;
      throw error;
    }
  }

  async #request<T>(path: string, init: RequestInit = {}): Promise<T> {
    const headers = new Headers(init.headers);
    headers.set("Accept", "application/json");
    const response = await this.fetch(`${this.baseUrl}${path}`, {
      ...init,
      headers,
      credentials: "include",
    });

    const body = await parseBody(response);
    if (!response.ok) {
      throw new NextramiceHttpError(response.status, body);
    }
    return body as T;
  }
}

function normalizeBaseUrl(baseUrl: string): string {
  const trimmed = baseUrl.trim();
  if (trimmed.length === 0) {
    throw new Error("Nextramice baseUrl must not be blank");
  }
  return trimmed.endsWith("/") ? trimmed.slice(0, -1) : trimmed;
}

async function parseBody(response: Response): Promise<unknown> {
  if (response.status === 204) {
    return undefined;
  }
  const contentType = response.headers.get("content-type") ?? "";
  if (contentType.includes("application/json")) {
    return response.json();
  }
  const text = await response.text();
  return text.length === 0 ? undefined : text;
}

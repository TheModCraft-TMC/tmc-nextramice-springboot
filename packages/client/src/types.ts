export interface NextramicePrincipal {
  id: string;
  username: string;
  displayName: string | null;
}

export interface NextramiceSession {
  authenticated: boolean;
  principal: NextramicePrincipal | null;
  authorities: string[];
  claims: Record<string, unknown>;
  expiresAt: string | null;
  loginMethod: "bearer" | "oidc" | "local" | "custom" | null;
  provider: string | null;
}

export interface NextramiceProvider {
  id: string;
  name: string;
  loginUrl: string;
}

export interface NextramiceCsrfToken {
  headerName: string;
  parameterName: string;
  token: string;
}

export interface NextramiceLoginRequest {
  username: string;
  password: string;
}

export interface NextramiceRegistrationRequest {
  username: string;
  password: string;
  attributes?: Record<string, unknown>;
}

export interface NextramiceRegistrationResult {
  status: "COMPLETE" | "VERIFICATION_REQUIRED" | "APPROVAL_REQUIRED";
  userId: string | null;
}

export interface NextramiceClientOptions {
  baseUrl?: string;
  fetch?: typeof globalThis.fetch;
}

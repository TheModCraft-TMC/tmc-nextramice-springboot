package net.themodcraft.nextramice.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ConfigurationProperties("nextramice")
public class NextramiceProperties {

    private final Web web = new Web();
    private final Bff bff = new Bff();
    private final LocalLogin localLogin = new LocalLogin();

    public Web getWeb() {
        return web;
    }

    public Bff getBff() {
        return bff;
    }

    public LocalLogin getLocalLogin() {
        return localLogin;
    }

    public static class Web {
        private NextramiceMode mode = NextramiceMode.RESOURCE_SERVER;
        private String bffMatcher = "/app/**";
        private String resourceServerMatcher = "/api/**";

        public NextramiceMode getMode() {
            return mode;
        }

        public void setMode(NextramiceMode mode) {
            this.mode = mode;
        }

        public String getBffMatcher() {
            return bffMatcher;
        }

        public void setBffMatcher(String bffMatcher) {
            this.bffMatcher = bffMatcher;
        }

        public String getResourceServerMatcher() {
            return resourceServerMatcher;
        }

        public void setResourceServerMatcher(String resourceServerMatcher) {
            this.resourceServerMatcher = resourceServerMatcher;
        }
    }

    public static class Bff {
        private String basePath = "/nextramice";
        private String postLoginRedirect = "/";
        private String postLogoutRedirect = "/";
        private boolean providerLogout;
        private Duration refreshSkew = Duration.ofMinutes(1);
        private final List<String> allowedOrigins = new ArrayList<>();
        private final Set<String> exposedClaims = new LinkedHashSet<>();
        private final Session session = new Session();

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }

        public String getPostLoginRedirect() {
            return postLoginRedirect;
        }

        public void setPostLoginRedirect(String postLoginRedirect) {
            this.postLoginRedirect = postLoginRedirect;
        }

        public String getPostLogoutRedirect() {
            return postLogoutRedirect;
        }

        public void setPostLogoutRedirect(String postLogoutRedirect) {
            this.postLogoutRedirect = postLogoutRedirect;
        }

        public boolean isProviderLogout() {
            return providerLogout;
        }

        public void setProviderLogout(boolean providerLogout) {
            this.providerLogout = providerLogout;
        }

        public Duration getRefreshSkew() {
            return refreshSkew;
        }

        public void setRefreshSkew(Duration refreshSkew) {
            this.refreshSkew = refreshSkew;
        }

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public Set<String> getExposedClaims() {
            return exposedClaims;
        }

        public Session getSession() {
            return session;
        }

        public static class Session {
            private Duration idleTimeout = Duration.ofMinutes(30);
            private Duration absoluteTimeout = Duration.ofHours(8);
            private final Cookie cookie = new Cookie();

            public Duration getIdleTimeout() {
                return idleTimeout;
            }

            public void setIdleTimeout(Duration idleTimeout) {
                this.idleTimeout = idleTimeout;
            }

            public Duration getAbsoluteTimeout() {
                return absoluteTimeout;
            }

            public void setAbsoluteTimeout(Duration absoluteTimeout) {
                this.absoluteTimeout = absoluteTimeout;
            }

            public Cookie getCookie() {
                return cookie;
            }
        }

        public static class Cookie {
            private String name = "NEXTRAMICE_SESSION";
            private String path = "/";
            private boolean httpOnly = true;
            private boolean secure = true;
            private SameSite sameSite = SameSite.LAX;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getPath() {
                return path;
            }

            public void setPath(String path) {
                this.path = path;
            }

            public boolean isHttpOnly() {
                return httpOnly;
            }

            public void setHttpOnly(boolean httpOnly) {
                this.httpOnly = httpOnly;
            }

            public boolean isSecure() {
                return secure;
            }

            public void setSecure(boolean secure) {
                this.secure = secure;
            }

            public SameSite getSameSite() {
                return sameSite;
            }

            public void setSameSite(SameSite sameSite) {
                this.sameSite = sameSite;
            }
        }

        public enum SameSite {
            LAX,
            STRICT,
            NONE
        }
    }

    public static class LocalLogin {
        private boolean enabled;
        private final Registration registration = new Registration();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Registration getRegistration() {
            return registration;
        }

        public static class Registration {
            private boolean enabled;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        }
    }
}

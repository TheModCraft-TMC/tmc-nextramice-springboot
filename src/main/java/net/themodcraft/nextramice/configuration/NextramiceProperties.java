package net.themodcraft.nextramice.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

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

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
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

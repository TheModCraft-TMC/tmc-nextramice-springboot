package net.themodcraft.nextramice.configuration;

import org.springframework.beans.factory.InitializingBean;

import java.net.URI;
import java.time.Duration;

public final class NextramiceConfigurationValidator implements InitializingBean {

    private final NextramiceProperties properties;

    public NextramiceConfigurationValidator(NextramiceProperties properties) {
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() {
        NextramiceProperties.Bff bff = properties.getBff();
        requirePath("nextramice.bff.base-path", bff.getBasePath());
        requireSafeRedirect("nextramice.bff.post-login-redirect", bff.getPostLoginRedirect());
        requireSafeRedirect("nextramice.bff.post-logout-redirect", bff.getPostLogoutRedirect());
        requirePositive("nextramice.bff.refresh-skew", bff.getRefreshSkew(), true);
        requirePositive("nextramice.bff.session.idle-timeout", bff.getSession().getIdleTimeout(), false);
        requirePositive("nextramice.bff.session.absolute-timeout", bff.getSession().getAbsoluteTimeout(), false);
        requirePath("nextramice.bff.session.cookie.path", bff.getSession().getCookie().getPath());
        if (isBlank(bff.getSession().getCookie().getName())) {
            fail("nextramice.bff.session.cookie.name must not be blank");
        }
        if (bff.getSession().getCookie().getSameSite() == NextramiceProperties.Bff.SameSite.NONE
                && !bff.getSession().getCookie().isSecure()) {
            fail("SameSite=None requires nextramice.bff.session.cookie.secure=true");
        }
        for (String origin : bff.getAllowedOrigins()) {
            validateOrigin(origin);
        }

        if (properties.getWeb().getMode() == NextramiceMode.HYBRID) {
            String bffMatcher = properties.getWeb().getBffMatcher();
            String resourceMatcher = properties.getWeb().getResourceServerMatcher();
            requirePath("nextramice.web.bff-matcher", bffMatcher);
            requirePath("nextramice.web.resource-server-matcher", resourceMatcher);
            if (patternsOverlap(bffMatcher, resourceMatcher)) {
                fail("Hybrid BFF and resource-server matchers must not overlap: "
                        + bffMatcher + " and " + resourceMatcher);
            }
        }
    }

    private static void validateOrigin(String origin) {
        try {
            URI uri = URI.create(origin);
            if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getRawQuery() != null || uri.getRawFragment() != null
                    || (uri.getPath() != null && !uri.getPath().isEmpty())) {
                fail("Invalid allowed origin: " + origin);
            }
        } catch (IllegalArgumentException exception) {
            fail("Invalid allowed origin: " + origin);
        }
    }

    private static void requireSafeRedirect(String name, String redirect) {
        if (isBlank(redirect) || !redirect.startsWith("/") || redirect.startsWith("//")
                || redirect.contains("\\") || redirect.contains("\r") || redirect.contains("\n")) {
            fail(name + " must be a local absolute path");
        }
    }

    private static void requirePath(String name, String path) {
        if (isBlank(path) || !path.startsWith("/") || path.startsWith("//")) {
            fail(name + " must start with exactly one '/'");
        }
    }

    private static void requirePositive(String name, Duration value, boolean zeroAllowed) {
        if (value == null || value.isNegative() || (!zeroAllowed && value.isZero())) {
            fail(name + " must be " + (zeroAllowed ? "zero or positive" : "positive"));
        }
    }

    static boolean patternsOverlap(String first, String second) {
        String left = prefix(first);
        String right = prefix(second);
        return left.equals("/") || right.equals("/") || left.startsWith(right) || right.startsWith(left);
    }

    private static String prefix(String pattern) {
        int wildcard = pattern.indexOf('*');
        String value = wildcard < 0 ? pattern : pattern.substring(0, wildcard);
        if (!value.endsWith("/")) {
            value += "/";
        }
        return value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static void fail(String message) {
        throw new IllegalStateException(message);
    }
}

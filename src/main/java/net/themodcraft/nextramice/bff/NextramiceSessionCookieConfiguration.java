package net.themodcraft.nextramice.bff;

import jakarta.servlet.SessionCookieConfig;
import net.themodcraft.nextramice.configuration.NextramiceProperties;
import org.springframework.boot.web.server.Cookie;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;

public final class NextramiceSessionCookieConfiguration {

    private NextramiceSessionCookieConfiguration() {
    }

    public static ServletContextInitializer initializer(NextramiceProperties properties) {
        return servletContext -> {
            NextramiceProperties.Bff.Session session = properties.getBff().getSession();
            NextramiceProperties.Bff.Cookie configured = session.getCookie();
            SessionCookieConfig cookie = servletContext.getSessionCookieConfig();
            cookie.setName(configured.getName());
            cookie.setPath(configured.getPath());
            cookie.setHttpOnly(configured.isHttpOnly());
            cookie.setSecure(configured.isSecure());
            long seconds = session.getIdleTimeout().toSeconds();
            servletContext.setSessionTimeout(Math.toIntExact(Math.max(1, (seconds + 59) / 60)));
        };
    }

    public static CookieSameSiteSupplier sameSiteSupplier(NextramiceProperties properties) {
        NextramiceProperties.Bff.Cookie configured = properties.getBff().getSession().getCookie();
        Cookie.SameSite sameSite = Cookie.SameSite.valueOf(configured.getSameSite().name());
        return CookieSameSiteSupplier.of(sameSite).whenHasName(configured.getName());
    }
}

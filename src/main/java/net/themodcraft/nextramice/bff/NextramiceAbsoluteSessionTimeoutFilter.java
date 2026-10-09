package net.themodcraft.nextramice.bff;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

final class NextramiceAbsoluteSessionTimeoutFilter extends OncePerRequestFilter {

    private final long absoluteTimeoutMillis;

    NextramiceAbsoluteSessionTimeoutFilter(Duration absoluteTimeout) {
        this.absoluteTimeoutMillis = absoluteTimeout.toMillis();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && System.currentTimeMillis() - session.getCreationTime() >= absoluteTimeoutMillis) {
            session.invalidate();
        }
        filterChain.doFilter(request, response);
    }
}

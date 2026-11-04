package br.com.joao.pagamentos;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Authenticates every request except the exact public status endpoint. */
@Component
public final class PaymentAccessFilter extends OncePerRequestFilter {
    private final byte[] readToken;
    private final byte[] adminToken;
    private static final int MAX_AUTHORIZATION_HEADER_LENGTH = 8192;

    public PaymentAccessFilter(
            @Value("${PAYMENT_READ_TOKEN:}") String readToken,
            @Value("${PAYMENT_ADMIN_TOKEN:}") String adminToken) {
        this.readToken = readToken.getBytes(StandardCharsets.UTF_8);
        this.adminToken = adminToken.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        // Default-deny avoids differences between raw URI and MVC route normalization
        // (context paths, encoded characters and matrix parameters).
        String publicStatus = request.getContextPath() + "/api/pagamentos/status";
        if (request.getRequestURI().equals(publicStatus) && request.getMethod().equals("GET")) {
            chain.doFilter(request, response);
            return;
        }
        if (readToken.length < 32 || adminToken.length < 32 ||
                MessageDigest.isEqual(readToken, adminToken)) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Payment tokens are not configured");
            return;
        }
        // More than one Authorization value is ambiguous across proxies and containers.
        // Reject it even if the first value is a valid privileged token.
        Enumeration<String> authHeaders = request.getHeaders("Authorization");
        String authorization = authHeaders.hasMoreElements() ? authHeaders.nextElement() : null;
        if (authHeaders.hasMoreElements()
                || (authorization != null && (authorization.length() > MAX_AUTHORIZATION_HEADER_LENGTH
                        || authorization.indexOf(',') >= 0))) {
            response.setHeader("WWW-Authenticate", "Bearer");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        byte[] supplied = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7).getBytes(StandardCharsets.UTF_8) : new byte[0];
        boolean admin = MessageDigest.isEqual(supplied, adminToken);
        boolean reader = MessageDigest.isEqual(supplied, readToken);
        if (!admin && !reader) {
            response.setHeader("WWW-Authenticate", "Bearer");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (!admin && !request.getMethod().equals("GET") && !request.getMethod().equals("HEAD")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }
}

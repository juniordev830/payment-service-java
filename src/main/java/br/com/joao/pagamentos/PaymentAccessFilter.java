package br.com.joao.pagamentos;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Restricts payment data to configured service tokens; status remains public. */
@Component
public final class PaymentAccessFilter extends OncePerRequestFilter {
    private final byte[] readToken;
    private final byte[] adminToken;

    public PaymentAccessFilter(
            @Value("${PAYMENT_READ_TOKEN:}") String readToken,
            @Value("${PAYMENT_ADMIN_TOKEN:}") String adminToken) {
        this.readToken = readToken.getBytes(StandardCharsets.UTF_8);
        this.adminToken = adminToken.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!path.equals("/api/pagamentos") && !path.startsWith("/api/pagamentos/")) {
            chain.doFilter(request, response);
            return;
        }
        if (path.equals("/api/pagamentos/status") && request.getMethod().equals("GET")) {
            chain.doFilter(request, response);
            return;
        }
        if (readToken.length < 32 || adminToken.length < 32 ||
                MessageDigest.isEqual(readToken, adminToken)) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Payment tokens are not configured");
            return;
        }
        String authorization = request.getHeader("Authorization");
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

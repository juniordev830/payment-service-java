package br.com.joao.pagamentos;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentAccessFilterTests {
    private static final String READ = "ci-read-token-0123456789abcdef0123456789";
    private static final String ADMIN = "ci-admin-token-0123456789abcdef0123456789";

    private int request(PaymentAccessFilter filter, String method, String path, String token) throws Exception {
        var req = new MockHttpServletRequest(method, path);
        if (token != null) req.addHeader("Authorization", "Bearer " + token);
        var res = new MockHttpServletResponse();
        filter.doFilter(req, res, new MockFilterChain());
        return res.getStatus();
    }

    @Test
    void rejectsMissingOrInvalidTokensAndReaderWrites() throws Exception {
        var filter = new PaymentAccessFilter(READ, ADMIN);
        assertEquals(401, request(filter, "GET", "/api/pagamentos", null));
        assertEquals(401, request(filter, "POST", "/api/pagamentos", "invalid"));
        assertEquals(403, request(filter, "POST", "/api/pagamentos", READ));
        assertEquals(200, request(filter, "GET", "/api/pagamentos", READ));
        assertEquals(200, request(filter, "POST", "/api/pagamentos", ADMIN));
        assertEquals(200, request(filter, "GET", "/api/pagamentos/status", null));
    }

    @Test
    void rejectsMultipleAuthorizationHeadersEvenWhenOneIsValid() throws Exception {
        var filter = new PaymentAccessFilter(READ, ADMIN);
        var req = new MockHttpServletRequest("POST", "/api/pagamentos");
        req.addHeader("Authorization", "Bearer " + ADMIN);
        req.addHeader("Authorization", "Bearer " + READ);
        var res = new MockHttpServletResponse();
        filter.doFilter(req, res, new MockFilterChain());
        assertEquals(401, res.getStatus());
        assertEquals("Bearer", res.getHeader("WWW-Authenticate"));
    }

    @Test
    void rejectsCombinedAndOversizedAuthorizationHeaders() throws Exception {
        var filter = new PaymentAccessFilter(READ, ADMIN);
        for (String value : new String[]{
                "Bearer " + ADMIN + ", Bearer " + READ,
                "Bearer " + "x".repeat(8193)
        }) {
            var req = new MockHttpServletRequest("POST", "/api/pagamentos");
            req.addHeader("Authorization", value);
            var res = new MockHttpServletResponse();
            filter.doFilter(req, res, new MockFilterChain());
            assertEquals(401, res.getStatus());
        }
    }

    @Test
    void paymentResponsesDoNotAllowCaching() throws Exception {
        var filter = new PaymentAccessFilter(READ, ADMIN);
        for (String token : new String[]{null, READ, ADMIN}) {
            var req = new MockHttpServletRequest("GET", "/api/pagamentos");
            if (token != null) req.addHeader("Authorization", "Bearer " + token);
            var res = new MockHttpServletResponse();
            filter.doFilter(req, res, new MockFilterChain());
            assertEquals("no-store", res.getHeader("Cache-Control"));
        }
    }

    @Test
    void failsClosedWithoutDistinctConfiguredSecrets() throws Exception {
        assertEquals(503, request(new PaymentAccessFilter("", ""), "GET", "/api/pagamentos", READ));
        assertEquals(503, request(new PaymentAccessFilter(READ, READ), "GET", "/api/pagamentos", READ));
    }
}

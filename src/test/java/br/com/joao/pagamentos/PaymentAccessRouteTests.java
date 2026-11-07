package br.com.joao.pagamentos;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "PAYMENT_READ_TOKEN=ci-read-token-0123456789abcdef0123456789",
    "PAYMENT_ADMIN_TOKEN=ci-admin-token-0123456789abcdef0123456789"
})
@AutoConfigureMockMvc
class PaymentAccessRouteTests {
    private static final String READ = "ci-read-token-0123456789abcdef0123456789";

    @Autowired
    private MockMvc mvc;

    @Test
    void protectsPaymentRoutesWithApplicationContextPath() throws Exception {
        mvc.perform(get("/lab/api/pagamentos").contextPath("/lab"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"));
        mvc.perform(get("/lab/api/pagamentos").contextPath("/lab")
                        .header("Authorization", "Bearer " + READ))
                .andExpect(status().isOk());
    }

    @Test
    void protectsRawUriVariantsBeforeMvcNormalization() throws Exception {
        for (String path : new String[]{
                "/api/pagamentos;version=1",
                "/api/%70agamentos",
                "/api/pagamentos/",
                "/api//pagamentos",
                "/api/pagamentos/status;version=1",
                "/api/pagamentos/status/"
        }) {
            mvc.perform(get(URI.create(path))).andExpect(status().isUnauthorized());
        }
    }

    @Test
    void onlyExactStatusGetIsPublicIncludingContextPath() throws Exception {
        mvc.perform(get("/api/pagamentos/status")).andExpect(status().isOk());
        mvc.perform(get("/lab/api/pagamentos/status").contextPath("/lab"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/pagamentos/status")).andExpect(status().isUnauthorized());
        mvc.perform(get("/unregistered-route")).andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateAuthorizationHeadersCannotAuthorizePaymentCreation() throws Exception {
        mvc.perform(post("/api/pagamentos")
                        .header("Authorization",
                                "Bearer ci-admin-token-0123456789abcdef0123456789",
                                "Bearer " + READ)
                        .contentType("application/json")
                        .content("{\"valor\":10.00,\"moeda\":\"BRL\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsAmbiguousAuthorizationAtReadRoutes() throws Exception {
        mvc.perform(get("/api/pagamentos")
                        .header("Authorization", "Bearer " + READ, "Bearer invalid"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"));
        mvc.perform(get("/api/pagamentos")
                        .header("Authorization", "Bearer " + READ + ",Bearer invalid"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/pagamentos")
                        .header("Authorization", "Bearer " + "x".repeat(8193)))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/pagamentos")
                        .header("Authorization", "Bearer " + READ))
                .andExpect(status().isOk());
    }

    @Test
    void contextPathDoesNotBypassReaderWriteRestriction() throws Exception {
        mvc.perform(post("/lab/api/pagamentos").contextPath("/lab")
                        .header("Authorization", "Bearer " + READ)
                        .contentType("application/json")
                        .content("{\"valor\":10.00,\"moeda\":\"BRL\"}"))
                .andExpect(status().isForbidden());
    }
}

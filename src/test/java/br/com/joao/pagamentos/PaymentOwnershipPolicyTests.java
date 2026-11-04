package br.com.joao.pagamentos;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentOwnershipPolicyTests {
    private static final UUID ACCOUNT_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ACCOUNT_B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Test
    void ownerWithPermissionCanReadOwnRecords() {
        var principal = new PaymentOwnershipPolicy.Principal(
                ACCOUNT_A, Set.of(PaymentOwnershipPolicy.Permission.READ));
        assertTrue(PaymentOwnershipPolicy.allows(
                principal, ACCOUNT_A, PaymentOwnershipPolicy.Permission.READ));
    }

    @Test
    void deniesReadingOrCreatingAcrossAccountsEvenWithAllPermissions() {
        var principal = new PaymentOwnershipPolicy.Principal(
                ACCOUNT_A, Set.of(PaymentOwnershipPolicy.Permission.READ,
                        PaymentOwnershipPolicy.Permission.CREATE));
        assertFalse(PaymentOwnershipPolicy.allows(
                principal, ACCOUNT_B, PaymentOwnershipPolicy.Permission.READ));
        assertFalse(PaymentOwnershipPolicy.allows(
                principal, ACCOUNT_B, PaymentOwnershipPolicy.Permission.CREATE));
    }

    @Test
    void readerCannotCreateForOwnAccount() {
        var principal = new PaymentOwnershipPolicy.Principal(
                ACCOUNT_A, Set.of(PaymentOwnershipPolicy.Permission.READ));
        assertFalse(PaymentOwnershipPolicy.allows(
                principal, ACCOUNT_A, PaymentOwnershipPolicy.Permission.CREATE));
    }

    @Test
    void rejectsAbsentIdentityOwnershipOrScope() {
        var principal = new PaymentOwnershipPolicy.Principal(
                ACCOUNT_A, Set.of(PaymentOwnershipPolicy.Permission.READ));
        assertFalse(PaymentOwnershipPolicy.allows(
                null, ACCOUNT_A, PaymentOwnershipPolicy.Permission.READ));
        assertFalse(PaymentOwnershipPolicy.allows(
                principal, null, PaymentOwnershipPolicy.Permission.READ));
        assertFalse(PaymentOwnershipPolicy.allows(
                principal, ACCOUNT_A, null));
        assertFalse(PaymentOwnershipPolicy.allows(
                new PaymentOwnershipPolicy.Principal(null, Set.of(
                        PaymentOwnershipPolicy.Permission.READ)),
                ACCOUNT_A, PaymentOwnershipPolicy.Permission.READ));
        assertFalse(PaymentOwnershipPolicy.allows(
                new PaymentOwnershipPolicy.Principal(ACCOUNT_A, null),
                ACCOUNT_A, PaymentOwnershipPolicy.Permission.READ));
    }
}

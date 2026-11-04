package br.com.joao.pagamentos;

import java.util.Set;
import java.util.UUID;

/**
 * Authorization decision for the future multi-account API.
 *
 * <p>The principal must come from verified authentication, never from an
 * untrusted request header or JSON ownerId. This policy is not yet wired to
 * controllers, repositories, or persisted account identities.
 */
public final class PaymentOwnershipPolicy {
    private PaymentOwnershipPolicy() {}

    public enum Permission {
        READ,
        CREATE
    }

    /** Validated caller identity and scoped permissions, not a shared service token. */
    public record Principal(UUID ownerId, Set<Permission> permissions) {
        public Principal {
            permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
        }
    }

    /** Default-deny decision: caller must own resource and hold this operation's scope. */
    public static boolean allows(Principal principal, UUID resourceOwnerId, Permission operation) {
        return principal != null
                && principal.ownerId() != null
                && resourceOwnerId != null
                && operation != null
                && principal.ownerId().equals(resourceOwnerId)
                && principal.permissions().contains(operation);
    }
}

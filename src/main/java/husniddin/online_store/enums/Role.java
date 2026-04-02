package husniddin.online_store.enums;

public enum Role {
    SUPER_ADMIN,
    ADMIN,
    DELIVERY,
    CUSTOMER,
    /** Read-only role: all GET endpoints allowed, all write endpoints (POST/PUT/DELETE/PATCH) denied. */
    VIEWER
}

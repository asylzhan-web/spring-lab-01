package kz.iitu.springlab.security;

// Reads the public header of a stored hash: the encoder id, the algorithm and its cost parameters.
// The salt and the hash that follow the header are never returned.
final class HashScheme {

    private HashScheme() {
    }

    static String describe(String stored) {
        if (stored == null || stored.isEmpty()) {
            return "—";
        }
        String id = "";
        String hash = stored;
        if (stored.startsWith("{")) {
            int end = stored.indexOf('}');
            if (end > 0) {
                id = stored.substring(0, end + 1) + " ";
                hash = stored.substring(end + 1);
            }
        }
        // BCrypt: $2a$10$<22 chars of salt><31 chars of hash>
        if (hash.matches("\\$2[aby]?\\$\\d\\d\\$.*")) {
            return id + "BCrypt " + hash.substring(0, 7) + " (cost " + hash.substring(4, 6) + ")";
        }
        // Argon2: $argon2id$v=19$m=16384,t=2,p=1$<salt>$<hash>
        if (hash.startsWith("$argon2")) {
            String[] parts = hash.split("\\$");
            if (parts.length >= 4) {
                return id + "Argon2 $" + parts[1] + "$" + parts[2] + "$" + parts[3] + "$";
            }
        }
        return id + "unknown format";
    }
}

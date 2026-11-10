package kz.iitu.springlab.security;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

// A row of app_user with its roles from user_role (V5__security.sql). No plaintext password anywhere.
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled = true;

    // Roles are stored without the ROLE_ prefix ("USER", "ADMIN"); DatabaseUserDetailsService adds it.
    // EAGER: a user is never needed without its roles, and login runs outside any transaction.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false, length = 20)
    private Set<String> roles = new HashSet<>();

    protected AppUser() {
        // for JPA
    }

    public AppUser(String username, String passwordHash, Set<String> roles) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.roles = new HashSet<>(roles);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<String> getRoles() {
        return Set.copyOf(roles);
    }

    @Override
    public String toString() {
        // never print the hash
        return "AppUser[id=" + id + ", username=" + username + ", enabled=" + enabled + ", roles=" + roles + "]";
    }
}

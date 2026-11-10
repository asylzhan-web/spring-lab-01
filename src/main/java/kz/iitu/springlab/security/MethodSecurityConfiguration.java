package kz.iitu.springlab.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// Turns on @PreAuthorize: a URL rule is not enough, because a second controller could reach the same service
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfiguration {
}

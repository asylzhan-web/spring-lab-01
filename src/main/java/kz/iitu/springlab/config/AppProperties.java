package kz.iitu.springlab.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @NotBlank String owner,
        @NotBlank String group,
        @Valid Mail mail,
        @Valid Locale locale) {

    public record Mail(
            @NotBlank @Email String from,
            @Min(1) @Max(10) @DefaultValue("3") int retryCount,
            @DefaultValue("5s") Duration timeout,
            @DefaultValue("true") boolean enabled) {
    }

    // Variant 5: app.locale
    public record Locale(
            @NotBlank @Pattern(regexp = "ru|kk|en") @DefaultValue("en") String defaultLocale,
            @NotEmpty @Size(min = 1) @DefaultValue("en") List<String> supported) {
    }
}

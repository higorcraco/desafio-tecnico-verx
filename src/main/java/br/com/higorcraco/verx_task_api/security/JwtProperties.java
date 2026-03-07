package br.com.higorcraco.verx_task_api.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    private String secret;
    private int expirationMinutes = 30;
    private int refreshExpirationDays = 7;
}

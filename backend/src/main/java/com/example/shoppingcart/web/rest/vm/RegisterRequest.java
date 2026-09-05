package com.example.shoppingcart.web.rest.vm;

import com.example.shoppingcart.config.Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Pattern(regexp = Constants.LOGIN_REGEX) @Size(max = 50) String login,
    @NotBlank @Email @Size(min = 5, max = 254) String email,
    @NotBlank @Size(min = 4, max = 100) String password
) {}

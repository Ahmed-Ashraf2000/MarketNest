package com.marketnest.ecommerce.validation;

import com.marketnest.ecommerce.annotation.PasswordValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PasswordStrengthValidator implements ConstraintValidator<PasswordValidator, String> {
    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[^A-Za-z0-9]");

    private List<String> commonWeakPasswords;

    @Override
    public void initialize(PasswordValidator constraintAnnotation) {
        commonWeakPasswords = List.of("password", "123456", "qwerty", "admin",
                "welcome", "12345678", "football", "123456789",
                "password1", "abc123", "letmein");
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        context.disableDefaultConstraintViolation();
        List<String> validationErrors = new ArrayList<>();

        if (password.length() < MIN_LENGTH) {
            validationErrors.add("Password must be at least " + MIN_LENGTH + " characters");
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            validationErrors.add("Password must contain at least one uppercase letter");
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            validationErrors.add("Password must contain at least one lowercase letter");
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            validationErrors.add("Password must contain at least one digit");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            validationErrors.add("Password must contain at least one special character");
        }

        if (commonWeakPasswords.contains(password.toLowerCase())) {
            validationErrors.add("Password is too common and easily guessable");
        }

        if (!validationErrors.isEmpty()) {
            context.buildConstraintViolationWithTemplate(
                            String.join(", ", validationErrors))
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
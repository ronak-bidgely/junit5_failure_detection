package com.example.ecommerce.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ValidationUtils Tests")
class ValidationUtilsTest {

    
    @Test
    @DisplayName("Should return true for non-empty strings")
    void shouldReturnTrueForNonEmptyStrings() {
        assertThat(ValidationUtils.isNotEmpty("hello")).isTrue();
        assertThat(ValidationUtils.isNotEmpty("a")).isTrue();
        assertThat(ValidationUtils.isNotEmpty("  test  ")).isTrue();
    }
    
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("Should return false for null, empty, or blank strings")
    void shouldReturnFalseForNullEmptyOrBlankStrings(String input) {
        assertThat(ValidationUtils.isNotEmpty(input)).isFalse();
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "test@example.com",
        "user.name@domain.co.uk",
        "user+tag@example.org",
        "user123@test-domain.com"
    })
    @DisplayName("Should return true for valid email addresses")
    void shouldReturnTrueForValidEmailAddresses(String email) {
        assertThat(ValidationUtils.isValidEmail(email)).isTrue();
    }
    
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "invalid-email",
        "@example.com",
        "user@",
        "user@.com",
        "user..name@example.com",
        "user@example.",
        "user name@example.com"
    })
    @DisplayName("Should return false for invalid email addresses")
    void shouldReturnFalseForInvalidEmailAddresses(String email) {
        assertThat(ValidationUtils.isValidEmail(email)).isFalse();
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "+1234567890",
        "1234567890",
        "+44123456789",
        "9876543210"
    })
    @DisplayName("Should return true for valid phone numbers")
    void shouldReturnTrueForValidPhoneNumbers(String phoneNumber) {
        assertThat(ValidationUtils.isValidPhoneNumber(phoneNumber)).isTrue();
    }
    
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "123",
        "+0123456789",
        "abc123456789",
        "123-456-7890",
        "+1 234 567 890"
    })
    @DisplayName("Should return false for invalid phone numbers")
    void shouldReturnFalseForInvalidPhoneNumbers(String phoneNumber) {
        assertThat(ValidationUtils.isValidPhoneNumber(phoneNumber)).isFalse();
    }
    
    @Test
    @DisplayName("Should return true for positive numbers")
    void shouldReturnTrueForPositiveNumbers() {
        assertThat(ValidationUtils.isPositive(1)).isTrue();
        assertThat(ValidationUtils.isPositive(0.1)).isTrue();
        assertThat(ValidationUtils.isPositive(100L)).isTrue();
        assertThat(ValidationUtils.isPositive(1.5f)).isTrue();
    }
    
    @Test
    @DisplayName("Should return false for non-positive numbers")
    void shouldReturnFalseForNonPositiveNumbers() {
        assertThat(ValidationUtils.isPositive(0)).isFalse();
        assertThat(ValidationUtils.isPositive(-1)).isFalse();
        assertThat(ValidationUtils.isPositive(-0.1)).isFalse();
        assertThat(ValidationUtils.isPositive(null)).isFalse();
    }
    
    @Test
    @DisplayName("Should return true for non-negative numbers")
    void shouldReturnTrueForNonNegativeNumbers() {
        assertThat(ValidationUtils.isNonNegative(0)).isTrue();
        assertThat(ValidationUtils.isNonNegative(1)).isTrue();
        assertThat(ValidationUtils.isNonNegative(0.0)).isTrue();
        assertThat(ValidationUtils.isNonNegative(100L)).isTrue();
    }
    
    @Test
    @DisplayName("Should return false for negative numbers")
    void shouldReturnFalseForNegativeNumbers() {
        assertThat(ValidationUtils.isNonNegative(-1)).isFalse();
        assertThat(ValidationUtils.isNonNegative(-0.1)).isFalse();
        assertThat(ValidationUtils.isNonNegative(null)).isFalse();
    }
    
    @Test
    @DisplayName("Should validate minimum length correctly")
    void shouldValidateMinimumLengthCorrectly() {
        assertThat(ValidationUtils.hasMinLength("hello", 3)).isTrue();
        assertThat(ValidationUtils.hasMinLength("hello", 5)).isTrue();
        assertThat(ValidationUtils.hasMinLength("hello", 6)).isFalse();
        assertThat(ValidationUtils.hasMinLength("", 1)).isFalse();
        assertThat(ValidationUtils.hasMinLength(null, 1)).isFalse();
    }
    
    @Test
    @DisplayName("Should validate maximum length correctly")
    void shouldValidateMaximumLengthCorrectly() {
        assertThat(ValidationUtils.hasMaxLength("hello", 10)).isTrue();
        assertThat(ValidationUtils.hasMaxLength("hello", 5)).isTrue();
        assertThat(ValidationUtils.hasMaxLength("hello", 3)).isFalse();
        assertThat(ValidationUtils.hasMaxLength(null, 5)).isTrue();
    }
    
    @Test
    @DisplayName("Should validate length range correctly")
    void shouldValidateLengthRangeCorrectly() {
        assertThat(ValidationUtils.isLengthInRange("hello", 3, 10)).isTrue();
        assertThat(ValidationUtils.isLengthInRange("hello", 5, 5)).isTrue();
        assertThat(ValidationUtils.isLengthInRange("hello", 6, 10)).isFalse();
        assertThat(ValidationUtils.isLengthInRange("hello", 1, 3)).isFalse();
        assertThat(ValidationUtils.isLengthInRange(null, 1, 5)).isFalse();
    }
}

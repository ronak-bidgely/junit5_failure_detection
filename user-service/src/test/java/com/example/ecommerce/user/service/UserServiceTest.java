package com.example.ecommerce.user.service;

import com.example.ecommerce.common.exception.BusinessException;
import com.example.ecommerce.user.model.User;
import com.example.ecommerce.user.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UserService Tests")
class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    // User Creation Tests

    @Test
    @DisplayName("Should create user successfully with valid input")
    void shouldCreateUserSuccessfullyWithValidInput() throws BusinessException {
        // When
        User user = userService.createUser("johndoe", "john@example.com", "John", "Doe");

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo("johndoe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "  ", "ab"})
    @DisplayName("Should throw exception for invalid username")
    void shouldThrowExceptionForInvalidUsername(String username) {
        assertThatThrownBy(() -> userService.createUser(username, "john@example.com", "John", "Doe"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Username");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalid-email", "@example.com", "user@"})
    @DisplayName("Should throw exception for invalid email")
    void shouldThrowExceptionForInvalidEmail(String email) {
        assertThatThrownBy(() -> userService.createUser("johndoe", email, "John", "Doe"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("email");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "  "})
    @DisplayName("Should throw exception for invalid first name")
    void shouldThrowExceptionForInvalidFirstName(String firstName) {
        assertThatThrownBy(() -> userService.createUser("johndoe", "john@example.com", firstName, "Doe"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("First name");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "  "})
    @DisplayName("Should throw exception for invalid last name")
    void shouldThrowExceptionForInvalidLastName(String lastName) {
        assertThatThrownBy(() -> userService.createUser("johndoe", "john@example.com", "John", lastName))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Last name");
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameAlreadyExists() throws BusinessException {
        // Given
        userService.createUser("johndoe", "john@example.com", "John", "Doe");

        // When & Then
        assertThatThrownBy(() -> userService.createUser("johndoe", "jane@example.com", "Jane", "Smith"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Username already exists");
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() throws BusinessException {
        // Given
        userService.createUser("johndoe", "john@example.com", "John", "Doe");

        // When & Then
        assertThatThrownBy(() -> userService.createUser("janedoe", "john@example.com", "Jane", "Smith"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Email already exists");
    }

    // User Lookup Tests

    @Test
    @DisplayName("Should find user by ID")
    void shouldFindUserById() throws BusinessException {
        // Given
        User testUser = userService.createUser("johndoe", "john@example.com", "John", "Doe");

        // When
        Optional<User> found = userService.findById(testUser.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("Should return empty when user ID not found")
    void shouldReturnEmptyWhenUserIdNotFound() {
        // When
        Optional<User> found = userService.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when user ID is null")
    void shouldReturnEmptyWhenUserIdIsNull() {
        // When
        Optional<User> found = userService.findById(null);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find user by username")
    void shouldFindUserByUsername() throws BusinessException {
        // Given
        User testUser = userService.createUser("johndoe", "john@example.com", "John", "Doe");

        // When
        Optional<User> found = userService.findByUsername("johndoe");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("Should find user by username case insensitive")
    void shouldFindUserByUsernameCaseInsensitive() throws BusinessException {
        // Given
        User testUser = userService.createUser("johndoe", "john@example.com", "John", "Doe");

        // When
        Optional<User> found = userService.findByUsername("JOHNDOE");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() throws BusinessException {
        // Given
        User testUser = userService.createUser("johndoe", "john@example.com", "John", "Doe");

        // When
        Optional<User> found = userService.findByEmail("john@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("Should find user by email case insensitive")
    void shouldFindUserByEmailCaseInsensitive() throws BusinessException {
        // Given
        User testUser = userService.createUser("johndoe", "john@example.com", "John", "Doe");

        // When
        Optional<User> found = userService.findByEmail("JOHN@EXAMPLE.COM");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(testUser);
    }

    // User Update Tests

    @Test
    @DisplayName("Should update user information successfully")
    void shouldUpdateUserInformationSuccessfully() throws BusinessException {
        // Given
        User testUser = userService.createUser("johndoe", "john@example.com", "John", "Doe");

        // When
        User updated = userService.updateUser(testUser.getId(), "Johnny", "Smith", "+1234567890");

        // Then
        assertThat(updated.getFirstName()).isEqualTo("Johnny");
        assertThat(updated.getLastName()).isEqualTo("Smith");
        assertThat(updated.getPhoneNumber()).isEqualTo("+1234567890");
    }

    @Test
    @DisplayName("Should throw exception for invalid phone number")
    void shouldThrowExceptionForInvalidPhoneNumber() throws BusinessException {
        // Given
        User testUser = userService.createUser("johndoe", "john@example.com", "John", "Doe");

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(testUser.getId(), "John", "Doe", "invalid-phone"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Invalid phone number");
    }

    @Test
    @DisplayName("Should throw exception when user not found for update")
    void shouldThrowExceptionWhenUserNotFoundForUpdate() {
        assertThatThrownBy(() -> userService.updateUser(999L, "John", "Doe", "+1234567890"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("User not found");
    }
    
    @Test
    @DisplayName("Should change user status successfully")
    void shouldChangeUserStatusSuccessfully() throws BusinessException {
        // Given
        User user = userService.createUser("johndoe", "john@example.com", "John", "Doe");
        
        // When
        User updated = userService.changeUserStatus(user.getId(), UserStatus.SUSPENDED);
        
        // Then
        assertThat(updated.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(updated.isActive()).isFalse();
    }
    
    @Test
    @DisplayName("Should get active users only")
    void shouldGetActiveUsersOnly() throws BusinessException {
        // Given
        User user1 = userService.createUser("user1", "user1@example.com", "User", "One");
        User user2 = userService.createUser("user2", "user2@example.com", "User", "Two");
        userService.changeUserStatus(user2.getId(), UserStatus.INACTIVE);
        
        // When
        List<User> activeUsers = userService.getActiveUsers();
        
        // Then
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0)).isEqualTo(user1);
    }
    
    @Test
    @DisplayName("Should return correct user count")
    void shouldReturnCorrectUserCount() throws BusinessException {
        // Given
        assertThat(userService.getUserCount()).isEqualTo(0);

        // When
        userService.createUser("user1", "user1@example.com", "User", "One");
        userService.createUser("user2", "user2@example.com", "User", "Two");

        // Then
        assertThat(userService.getUserCount()).isEqualTo(2);
    }

    // This test is intentionally designed to fail on first run but pass on retry
    @Test
    @DisplayName("Flaky test - Should fail first time but pass on retry")
    void flakyTestDemonstratingRetry() throws BusinessException {
        // This test will fail on first run but pass on retry to demonstrate retry mechanism
        // Use a unique counter file per test class to avoid conflicts
        String testId = this.getClass().getName() + "#flakyTestDemonstratingRetry";
        java.nio.file.Path counterFile = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"),
            "junit-retry-" + Integer.toHexString(testId.hashCode()) + ".txt");

        try {
            int attemptCount = 0;
            if (java.nio.file.Files.exists(counterFile)) {
                String content = new String(java.nio.file.Files.readAllBytes(counterFile));
                attemptCount = Integer.parseInt(content.trim());
            }

            // Increment and save counter
            attemptCount++;
            java.nio.file.Files.write(counterFile, String.valueOf(attemptCount).getBytes());

            // Fail on first attempt, pass on subsequent attempts
            if (attemptCount == 1) {
                // Clean up counter file after test completes (for next Maven run)
                counterFile.toFile().deleteOnExit();

                // Create a user and assert wrong status to trigger failure
                User user = userService.createUser("testuser", "test@example.com", "Test", "User");
                assertThat(user.getStatus())
                    .as("First attempt - this should fail and trigger retry")
                    .isEqualTo(UserStatus.INACTIVE); // Should be ACTIVE - will fail
            } else {
                // Clean up counter file after successful retry
                java.nio.file.Files.deleteIfExists(counterFile);

                // Correct assertion - will pass
                User user = userService.createUser("testuser" + attemptCount, "test" + attemptCount + "@example.com", "Test", "User");
                assertThat(user.getStatus())
                    .as("Retry attempt %d - this should pass", attemptCount)
                    .isEqualTo(UserStatus.ACTIVE); // Correct assertion - will pass
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to manage retry counter file", e);
        }
    }

    // Flaky parameterized test - only parameter #2 will be flaky
    @ParameterizedTest
    @ValueSource(strings = {"valid1@example.com", "flaky@example.com", "valid2@example.com"})
    @DisplayName("Flaky parameterized test - second parameter fails first time")
    void flakyParameterizedTest(String email) throws BusinessException {
        // Only the second parameter ("flaky@example.com") will be flaky
        if (email.equals("flaky@example.com")) {
            String testId = this.getClass().getName() + "#flakyParameterizedTest-" + email;
            java.nio.file.Path counterFile = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"),
                "junit-retry-" + Integer.toHexString(testId.hashCode()) + ".txt");

            try {
                int attemptCount = 0;
                if (java.nio.file.Files.exists(counterFile)) {
                    String content = new String(java.nio.file.Files.readAllBytes(counterFile));
                    attemptCount = Integer.parseInt(content.trim());
                }

                // Increment and save counter
                attemptCount++;
                java.nio.file.Files.write(counterFile, String.valueOf(attemptCount).getBytes());

                // Fail on first attempt, pass on subsequent attempts
                if (attemptCount == 1) {
                    counterFile.toFile().deleteOnExit();

                    User user = userService.createUser("flakyuser", email, "Flaky", "User");
                    assertThat(user.getEmail())
                        .as("First attempt with %s - should fail", email)
                        .isEqualTo("wrong@example.com"); // Wrong - will fail
                } else {
                    java.nio.file.Files.deleteIfExists(counterFile);

                    User user = userService.createUser("flakyuser" + attemptCount, email, "Flaky", "User");
                    assertThat(user.getEmail())
                        .as("Retry attempt %d with %s - should pass", attemptCount, email)
                        .isEqualTo(email); // Correct - will pass
                }
            } catch (java.io.IOException e) {
                throw new RuntimeException("Failed to manage retry counter file", e);
            }
        } else {
            // Non-flaky parameters - always pass
            User user = userService.createUser("user-" + email.hashCode(), email, "Test", "User");
            assertThat(user.getEmail()).isEqualTo(email);
        }
    }

    // NEW: Flaky test that fails TWICE and passes on the THIRD attempt
    @Test
    @DisplayName("Very flaky test - Fails 2 times, passes on 3rd attempt")
    void veryFlakyTestFailsTwicePassesThird() throws BusinessException {
        String testId = this.getClass().getName() + "#veryFlakyTestFailsTwicePassesThird";
        java.nio.file.Path counterFile = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"),
            "junit-retry-" + Integer.toHexString(testId.hashCode()) + ".txt");

        try {
            int attemptCount = 0;
            if (java.nio.file.Files.exists(counterFile)) {
                String content = new String(java.nio.file.Files.readAllBytes(counterFile));
                attemptCount = Integer.parseInt(content.trim());
            }

            // Increment and save counter
            attemptCount++;
            java.nio.file.Files.write(counterFile, String.valueOf(attemptCount).getBytes());

            if (attemptCount == 1) {
                // First attempt - FAIL
                counterFile.toFile().deleteOnExit();
                User user = userService.createUser("veryflaky1", "veryflaky1@example.com", "Very", "Flaky");
                assertThat(user.getStatus())
                    .as("Attempt 1 of 3 - This WILL FAIL (expecting INACTIVE but got ACTIVE)")
                    .isEqualTo(UserStatus.INACTIVE); // Wrong - will fail
            } else if (attemptCount == 2) {
                // Second attempt - FAIL AGAIN
                User user = userService.createUser("veryflaky2", "veryflaky2@example.com", "Very", "Flaky");
                assertThat(user.getFirstName())
                    .as("Attempt 2 of 3 - This WILL FAIL AGAIN (expecting 'Wrong' but got 'Very')")
                    .isEqualTo("Wrong"); // Wrong - will fail again
            } else {
                // Third attempt - PASS
                java.nio.file.Files.deleteIfExists(counterFile);
                User user = userService.createUser("veryflaky3", "veryflaky3@example.com", "Very", "Flaky");
                assertThat(user.getStatus())
                    .as("Attempt 3 of 3 - This WILL PASS")
                    .isEqualTo(UserStatus.ACTIVE); // Correct - will pass
                assertThat(user.getFirstName()).isEqualTo("Very");
                assertThat(user.getLastName()).isEqualTo("Flaky");
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to manage retry counter file", e);
        }
    }
}

package com.example.ecommerce.product.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Product Model Tests")
class ProductTest {
    
    private Product product;
    
    @BeforeEach
    void setUp() {
        product = new Product("Test Product", "TEST-001", new BigDecimal("99.99"), ProductCategory.ELECTRONICS);
    }
    
    @Test
    @DisplayName("Should create product with default values")
    void shouldCreateProductWithDefaultValues() {
        // Given & When
        Product newProduct = new Product();
        
        // Then
        assertThat(newProduct.isActive()).isTrue();
        assertThat(newProduct.getStockQuantity()).isEqualTo(0);
        assertThat(newProduct.isInStock()).isFalse();
        assertThat(newProduct.isAvailable()).isFalse();
    }
    
    @Test
    @DisplayName("Should create product with provided details")
    void shouldCreateProductWithProvidedDetails() {
        // Then
        assertThat(product.getName()).isEqualTo("Test Product");
        assertThat(product.getSku()).isEqualTo("TEST-001");
        assertThat(product.getPrice()).isEqualTo(new BigDecimal("99.99"));
        assertThat(product.getCategory()).isEqualTo(ProductCategory.ELECTRONICS);
        assertThat(product.isActive()).isTrue();
        assertThat(product.getStockQuantity()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should return true for isInStock when stock quantity is positive")
    void shouldReturnTrueForIsInStockWhenStockQuantityIsPositive() {
        // Given
        product.setStockQuantity(10);
        
        // When & Then
        assertThat(product.isInStock()).isTrue();
    }
    
    @Test
    @DisplayName("Should return false for isInStock when stock quantity is zero or negative")
    void shouldReturnFalseForIsInStockWhenStockQuantityIsZeroOrNegative() {
        // Given
        product.setStockQuantity(0);
        
        // When & Then
        assertThat(product.isInStock()).isFalse();
        
        // Given
        product.setStockQuantity(-1);
        
        // When & Then
        assertThat(product.isInStock()).isFalse();
    }
    
    @Test
    @DisplayName("Should return false for isInStock when stock quantity is null")
    void shouldReturnFalseForIsInStockWhenStockQuantityIsNull() {
        // Given
        product.setStockQuantity(null);
        
        // When & Then
        assertThat(product.isInStock()).isFalse();
    }
    
    @Test
    @DisplayName("Should return true for isAvailable when active and in stock")
    void shouldReturnTrueForIsAvailableWhenActiveAndInStock() {
        // Given
        product.setActive(true);
        product.setStockQuantity(10);
        
        // When & Then
        assertThat(product.isAvailable()).isTrue();
    }
    
    @Test
    @DisplayName("Should return false for isAvailable when not active")
    void shouldReturnFalseForIsAvailableWhenNotActive() {
        // Given
        product.setActive(false);
        product.setStockQuantity(10);
        
        // When & Then
        assertThat(product.isAvailable()).isFalse();
    }
    
    @Test
    @DisplayName("Should return false for isAvailable when not in stock")
    void shouldReturnFalseForIsAvailableWhenNotInStock() {
        // Given
        product.setActive(true);
        product.setStockQuantity(0);
        
        // When & Then
        assertThat(product.isAvailable()).isFalse();
    }
    
    @Test
    @DisplayName("Should add stock correctly")
    void shouldAddStockCorrectly() {
        // Given
        product.setStockQuantity(5);
        
        // When
        product.addStock(10);
        
        // Then
        assertThat(product.getStockQuantity()).isEqualTo(15);
    }
    
    @Test
    @DisplayName("Should add stock to null quantity")
    void shouldAddStockToNullQuantity() {
        // Given
        product.setStockQuantity(null);
        
        // When
        product.addStock(10);
        
        // Then
        assertThat(product.getStockQuantity()).isEqualTo(10);
    }
    
    @Test
    @DisplayName("Should not add negative or zero stock")
    void shouldNotAddNegativeOrZeroStock() {
        // Given
        product.setStockQuantity(5);
        
        // When
        product.addStock(0);
        product.addStock(-5);
        
        // Then
        assertThat(product.getStockQuantity()).isEqualTo(5);
    }
    
    @Test
    @DisplayName("Should remove stock successfully when sufficient stock available")
    void shouldRemoveStockSuccessfullyWhenSufficientStockAvailable() {
        // Given
        product.setStockQuantity(10);
        
        // When
        boolean result = product.removeStock(3);
        
        // Then
        assertThat(result).isTrue();
        assertThat(product.getStockQuantity()).isEqualTo(7);
    }
    
    @Test
    @DisplayName("Should not remove stock when insufficient stock available")
    void shouldNotRemoveStockWhenInsufficientStockAvailable() {
        // Given
        product.setStockQuantity(5);
        
        // When
        boolean result = product.removeStock(10);
        
        // Then
        assertThat(result).isFalse();
        assertThat(product.getStockQuantity()).isEqualTo(5);
    }
    
    @Test
    @DisplayName("Should not remove stock when quantity is null")
    void shouldNotRemoveStockWhenQuantityIsNull() {
        // Given
        product.setStockQuantity(null);
        
        // When
        boolean result = product.removeStock(5);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("Should not remove negative or zero stock")
    void shouldNotRemoveNegativeOrZeroStock() {
        // Given
        product.setStockQuantity(10);
        
        // When
        boolean result1 = product.removeStock(0);
        boolean result2 = product.removeStock(-5);
        
        // Then
        assertThat(result1).isFalse();
        assertThat(result2).isFalse();
        assertThat(product.getStockQuantity()).isEqualTo(10);
    }
    
    @Test
    @DisplayName("Should activate product")
    void shouldActivateProduct() {
        // Given
        product.setActive(false);
        
        // When
        product.activate();
        
        // Then
        assertThat(product.isActive()).isTrue();
    }
    
    @Test
    @DisplayName("Should deactivate product")
    void shouldDeactivateProduct() {
        // When
        product.deactivate();
        
        // Then
        assertThat(product.isActive()).isFalse();
    }
    
    @Test
    @DisplayName("Should have meaningful toString representation")
    void shouldHaveMeaningfulToStringRepresentation() {
        // Given
        product.setId(123L);
        
        // When
        String toString = product.toString();
        
        // Then
        assertThat(toString)
            .contains("Product{")
            .contains("id=123")
            .contains("name='Test Product'")
            .contains("sku='TEST-001'")
            .contains("price=99.99")
            .contains("category=ELECTRONICS")
            .contains("stockQuantity=0")
            .contains("active=true");
    }

    // Flaky test: fails first time, passes on retry
    @Test
    @DisplayName("Flaky test - Should fail first time but pass on retry")
    void flakyTestDemonstratingRetry() {
        String testId = this.getClass().getName() + "#flakyTestDemonstratingRetry";
        java.nio.file.Path counterFile = java.nio.file.Paths.get(
            System.getProperty("java.io.tmpdir"),
            "junit-retry-" + Integer.toHexString(testId.hashCode()) + ".txt");

        try {
            int attemptCount = 0;
            if (java.nio.file.Files.exists(counterFile)) {
                String content = new String(java.nio.file.Files.readAllBytes(counterFile));
                attemptCount = Integer.parseInt(content.trim());
            }

            attemptCount++;
            java.nio.file.Files.write(counterFile, String.valueOf(attemptCount).getBytes());

            if (attemptCount == 1) {
                // First attempt - intentionally fail
                counterFile.toFile().deleteOnExit();

                // Wrong expectation: price should NOT be 100.00
                assertThat(product.getPrice())
                    .as("First attempt - this should fail and trigger retry")
                    .isEqualTo(new java.math.BigDecimal("100.00"));
            } else {
                // Retry attempt(s) - correct expectation
                java.nio.file.Files.deleteIfExists(counterFile);

                assertThat(product.getPrice())
                    .as("Retry attempt %d - this should pass", attemptCount)
                    .isEqualTo(new java.math.BigDecimal("99.99"));
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to manage retry counter file", e);
        }
    }
}

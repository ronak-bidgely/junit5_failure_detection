package com.example.ecommerce.order.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Order Model Tests")
class OrderTest {

    private Order order;
    private OrderItem item1;
    private OrderItem item2;

    @BeforeEach
    void setUp() {
        order = new Order("ORD-001", 123L);
        item1 = new OrderItem(1L, "Product 1", "SKU-001", new BigDecimal("10.00"), 2);
        item2 = new OrderItem(2L, "Product 2", "SKU-002", new BigDecimal("15.00"), 1);
    }
    
    @Test
    @DisplayName("Should create order with default values")
    void shouldCreateOrderWithDefaultValues() {
        // Given & When
        Order newOrder = new Order();
        
        // Then
        assertThat(newOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(newOrder.getOrderDate()).isNotNull();
        assertThat(newOrder.getItems()).isEmpty();
        assertThat(newOrder.getTotalAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(newOrder.getTaxAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(newOrder.getShippingAmount()).isEqualTo(BigDecimal.ZERO);
    }
    
    @Test
    @DisplayName("Should create order with provided details")
    void shouldCreateOrderWithProvidedDetails() {
        // Then
        assertThat(order.getOrderNumber()).isEqualTo("ORD-001");
        assertThat(order.getCustomerId()).isEqualTo(123L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getOrderDate()).isNotNull();
    }

    // Order Item Management Tests

    @Test
    @DisplayName("Should add item and recalculate total")
    void shouldAddItemAndRecalculateTotal() {
        // When
        order.addItem(item1);

        // Then
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItemCount()).isEqualTo(1);
        assertThat(order.getSubtotal()).isEqualTo(new BigDecimal("20.00"));
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("Should add multiple items and recalculate total")
    void shouldAddMultipleItemsAndRecalculateTotal() {
        // When
        order.addItem(item1);
        order.addItem(item2);

        // Then
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getItemCount()).isEqualTo(2);
        assertThat(order.getSubtotal()).isEqualTo(new BigDecimal("35.00"));
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("35.00"));
    }

    @Test
    @DisplayName("Should remove item and recalculate total")
    void shouldRemoveItemAndRecalculateTotal() {
        // Given
        order.addItem(item1);
        order.addItem(item2);

        // When
        order.removeItem(item1);

        // Then
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItemCount()).isEqualTo(1);
        assertThat(order.getSubtotal()).isEqualTo(new BigDecimal("15.00"));
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("15.00"));
    }

    @Test
    @DisplayName("Should not add null item")
    void shouldNotAddNullItem() {
        // When
        order.addItem(null);

        // Then
        assertThat(order.getItems()).isEmpty();
        assertThat(order.getItemCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle tax and shipping in total calculation")
    void shouldHandleTaxAndShippingInTotalCalculation() {
        // Given
        order.addItem(item1);
        order.setTaxAmount(new BigDecimal("2.00"));
        order.setShippingAmount(new BigDecimal("5.00"));

        // When
        order.addItem(item2); // This should trigger recalculation

        // Then
        assertThat(order.getSubtotal()).isEqualTo(new BigDecimal("35.00"));
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("42.00")); // 35 + 2 + 5
    }

    // Order Status Management Tests

    @Test
    @DisplayName("Should confirm order and update status")
    void shouldConfirmOrderAndUpdateStatus() {
        // When
        order.confirm();

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.isConfirmed()).isTrue();
        assertThat(order.isPending()).isFalse();
    }

    @Test
    @DisplayName("Should cancel order")
    void shouldCancelOrder() {
        // When
        order.cancel();

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.isCancelled()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    @DisplayName("Should correctly identify order status")
    void shouldCorrectlyIdentifyOrderStatus(OrderStatus status) {
        // Given
        order.setStatus(status);

        // When & Then
        switch (status) {
            case PENDING:
                assertThat(order.isPending()).isTrue();
                assertThat(order.isConfirmed()).isFalse();
                assertThat(order.isShipped()).isFalse();
                assertThat(order.isDelivered()).isFalse();
                assertThat(order.isCancelled()).isFalse();
                break;
            case CONFIRMED:
                assertThat(order.isPending()).isFalse();
                assertThat(order.isConfirmed()).isTrue();
                assertThat(order.isShipped()).isFalse();
                assertThat(order.isDelivered()).isFalse();
                assertThat(order.isCancelled()).isFalse();
                break;
            case SHIPPED:
                assertThat(order.isPending()).isFalse();
                assertThat(order.isConfirmed()).isFalse();
                assertThat(order.isShipped()).isTrue();
                assertThat(order.isDelivered()).isFalse();
                assertThat(order.isCancelled()).isFalse();
                break;
            case DELIVERED:
                assertThat(order.isPending()).isFalse();
                assertThat(order.isConfirmed()).isFalse();
                assertThat(order.isShipped()).isFalse();
                assertThat(order.isDelivered()).isTrue();
                assertThat(order.isCancelled()).isFalse();
                break;
            case CANCELLED:
                assertThat(order.isPending()).isFalse();
                assertThat(order.isConfirmed()).isFalse();
                assertThat(order.isShipped()).isFalse();
                assertThat(order.isDelivered()).isFalse();
                assertThat(order.isCancelled()).isTrue();
                break;
        }
    }
    
    @Test
    @DisplayName("Should have meaningful toString representation")
    void shouldHaveMeaningfulToStringRepresentation() {
        // Given
        order.setId(123L);
        order.addItem(item1);
        
        // When
        String toString = order.toString();
        
        // Then
        assertThat(toString)
            .contains("Order{")
            .contains("id=123")
            .contains("orderNumber='ORD-001'")
            .contains("customerId=123")
            .contains("status=PENDING")
            .contains("itemCount=1");
    }
    
    // This test is intentionally designed to fail on first run but pass on retry
    @Test
    @DisplayName("Flaky test - Should fail first time but pass on retry")
    void flakyTestDemonstratingRetry() {
        // This test will fail on first run but pass on retry to demonstrate retry mechanism
        // Use a unique counter file per test class to avoid conflicts
        String testId = this.getClass().getName() + "#flakyTestDemonstratingRetry";
        Path counterFile = Paths.get(System.getProperty("java.io.tmpdir"),
            "junit-retry-" + Integer.toHexString(testId.hashCode()) + ".txt");

        try {
            int attemptCount = 0;
            if (Files.exists(counterFile)) {
                String content = new String(Files.readAllBytes(counterFile));
                attemptCount = Integer.parseInt(content.trim());
            }

            // Increment and save counter
            attemptCount++;
            Files.write(counterFile, String.valueOf(attemptCount).getBytes());

            // Fail on first attempt, pass on subsequent attempts
            if (attemptCount == 1) {
                // Clean up counter file after test completes (for next Maven run)
                counterFile.toFile().deleteOnExit();

                assertThat(order.getStatus())
                    .as("First attempt - this should fail and trigger retry")
                    .isEqualTo(OrderStatus.CONFIRMED); // Should be PENDING - will fail
            } else {
                // Clean up counter file after successful retry
                Files.deleteIfExists(counterFile);

                assertThat(order.getStatus())
                    .as("Retry attempt %d - this should pass", attemptCount)
                    .isEqualTo(OrderStatus.PENDING); // Correct assertion - will pass
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to manage retry counter file", e);
        }
    }
}

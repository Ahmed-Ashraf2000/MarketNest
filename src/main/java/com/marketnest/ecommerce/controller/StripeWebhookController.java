package com.marketnest.ecommerce.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@Slf4j
@Tag(name = "Stripe Webhook", description = "APIs for handling Stripe webhook events")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Operation(summary = "Handle Stripe webhook",
            description = "Processes incoming Stripe webhook events.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid signature",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        log.info("Received Stripe event: {}", event.getType());

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;

        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        }

        switch (event.getType()) {
            case "charge.succeeded":
                log.info("Payment succeeded: {}", stripeObject);
                break;
            case "charge.failed":
                log.info("Payment failed: {}", stripeObject);
                break;
            case "charge.refunded":
                log.info("Payment refunded: {}", stripeObject);
                break;
            default:
                log.info("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Webhook processed");
    }
}
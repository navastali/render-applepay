package com.example.renderapplepay;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.SetupIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.SetupIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class StripeController {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, Object>> createPaymentIntent(@RequestBody Map<String, Object> body) throws StripeException {
        Number amountNum = (Number) body.getOrDefault("amount", 100);
        Long amount = amountNum.longValue();
        String currency = (String) body.getOrDefault("currency", "usd");

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .addPaymentMethodType("card")
                .build();

        PaymentIntent pi = PaymentIntent.create(params);
        Map<String, Object> resp = new HashMap<>();
        resp.put("clientSecret", pi.getClientSecret());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/create-setup-intent")
    public ResponseEntity<Map<String, Object>> createSetupIntent(@RequestBody Map<String, Object> body) throws StripeException {
        String customerId = (String) body.get("customerId"); // optional
        SetupIntentCreateParams.Builder builder = SetupIntentCreateParams.builder();
        if (customerId != null) {
            builder.setCustomer(customerId);
        }
        SetupIntentCreateParams params = builder.build();
        SetupIntent si = SetupIntent.create(params);
        Map<String, Object> resp = new HashMap<>();
        resp.put("clientSecret", si.getClientSecret());
        return ResponseEntity.ok(resp);
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleWebhook(HttpServletRequest request, @RequestHeader("Stripe-Signature") String sigHeader) throws IOException {
        String payload = new String(request.getInputStream().readAllBytes());
        com.stripe.model.Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(400).body("Webhook signature verification failed");
        }

        String type = event.getType();
        switch (type) {
            case "payment_intent.succeeded": {
                PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (pi != null) {
                    System.out.println("PaymentIntent succeeded: " + pi.getId());
                }
                break;
            }
            case "setup_intent.succeeded": {
                SetupIntent si = (SetupIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (si != null) {
                    System.out.println("SetupIntent succeeded: " + si.getId() + " pm: " + si.getPaymentMethod());
                }
                break;
            }
            default:
                System.out.println("Unhandled event type: " + type);
        }

        return ResponseEntity.ok("Received");
    }
}

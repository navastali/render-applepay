import React, { useEffect, useState } from "react";
import { loadStripe } from "@stripe/stripe-js";

const stripePromise = loadStripe(process.env.REACT_APP_STRIPE_PUBLISHABLE_KEY);

export default function ApplePayButton({ amountCents = 1990, currency = "usd", label = "Demo Product" }) {
  const [available, setAvailable] = useState(false);
  const [stripeObj, setStripeObj] = useState(null);

  useEffect(() => {
    let mounted = true;
    (async () => {
      const stripe = await stripePromise;
      if (!mounted) return;
      setStripeObj(stripe);

      const paymentRequest = stripe.paymentRequest({
        country: "US",
        currency: currency,
        total: {
          label,
          amount: amountCents,
        },
        requestPayerName: true,
        requestPayerEmail: true,
      });

      const canMakePayment = await paymentRequest.canMakePayment();
      if (canMakePayment && (canMakePayment.applePay || canMakePayment.paymentRequest)) {
        setAvailable(true);
      }

      paymentRequest.on("paymentmethod", async (ev) => {
        try {
          const createResp = await fetch("/create-payment-intent", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ amount: amountCents, currency }),
          });

          if (!createResp.ok) {
            ev.complete("fail");
            console.error("Failed to create PaymentIntent");
            return;
          }

          const { clientSecret } = await createResp.json();

          const confirmResult = await stripe.confirmCardPayment(
            clientSecret,
            { payment_method: ev.paymentMethod.id },
            { handleActions: false }
          );

          if (confirmResult.error) {
            console.error("confirmCardPayment error:", confirmResult.error);
            ev.complete("fail");
            return;
          }

          ev.complete("success");

          if (confirmResult.paymentIntent && confirmResult.paymentIntent.status === "requires_action") {
            const finalResult = await stripe.confirmCardPayment(clientSecret);
            if (finalResult.error) {
              console.error("3DS error", finalResult.error);
            }
          } else {
            console.log("Payment succeeded:", confirmResult.paymentIntent || confirmResult);
          }
        } catch (err) {
          console.error("Payment flow error:", err);
          try { ev.complete("fail"); } catch (e) {}
        }
      });

      window._stripePaymentRequest = paymentRequest;
    })();

    return () => { mounted = false; };
  }, [amountCents, currency, label]);

  async function onClick() {
    if (!stripeObj || !window._stripePaymentRequest) {
      alert("Apple Pay not available.");
      return;
    }
    try {
      await window._stripePaymentRequest.show();
    } catch (err) {
      console.error("Failed to show payment request:", err);
    }
  }

  if (!available) {
    return <div>Apple Pay not available on this device/browser.</div>;
  }

  return (
    <button
      onClick={onClick}
      style={{
        display: "inline-flex",
        alignItems: "center",
        padding: "12px 20px",
        borderRadius: 8,
        background: "#000",
        color: "#fff",
        border: "none",
        cursor: "pointer",
        fontSize: 16,
      }}
      aria-label="Pay with Apple Pay"
    >
      <span style={{ marginRight: 8 }}></span>
      <span>Pay</span>
    </button>
  );
}

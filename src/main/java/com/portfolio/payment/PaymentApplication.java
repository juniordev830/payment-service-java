package com.portfolio.payment;

public class PaymentApplication {

    public static class PaymentRequest {
        private String transactionId;
        private double amount;
        private String currency;

        public PaymentRequest(String transactionId, double amount, String currency) {
            this.transactionId = transactionId;
            this.amount = amount;
            this.currency = currency;
        }

        public String getTransactionId() { return transactionId; }
        public double getAmount() { return amount; }
        public String getCurrency() { return currency; }
    }

    public static boolean processPayment(PaymentRequest request) {
        System.out.println("Processing payment [" + request.getTransactionId() + "] for " + request.getAmount() + " " + request.getCurrency());
        return request.getAmount() > 0;
    }

    public static void main(String[] args) {
        PaymentRequest request = new PaymentRequest("TXN-987654", 150.75, "USD");
        boolean success = processPayment(request);
        System.out.println("Payment Processed Successfully: " + success);
    }
}

package org.likith.demo;

public class StripeService implements IpaymentService{
    @Override
    public String process() {
        return "PaypalService";
    }
}

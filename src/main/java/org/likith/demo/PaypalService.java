package org.likith.demo;

import org.likith.annotations.LComponent;
import org.likith.annotations.LPrimary;

@LComponent
@LPrimary
public class PaypalService implements IpaymentService {
    @Override
    public String process() {
        return "PaypalService";
    }
}

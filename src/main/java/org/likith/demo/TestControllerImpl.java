package org.likith.demo;

import org.likith.annotations.*;

@LComponent
@Lcontroller
public class TestControllerImpl implements TestController {
    @LAutowired
    private IpaymentService ipaymentService;

    @LLoggable
    @LGetMapping(value = "/get")
    public String getMethod(){
        return ipaymentService.process();
    }


    @LLoggable
    @LPostMapping(value="/post")
    public String postMethod(String requestBody) {
        return "received "+requestBody;
    }
}

package com.decagon.safariwebstore.controller;

import com.decagon.safariwebstore.payload.response.Response;
import com.decagon.safariwebstore.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping(value = "/pay/{id}")
    public String makePayment(HttpServletResponse httpServletResponse, @PathVariable String id) throws Exception {

        Long orderId = Long.valueOf(id);
        String paymentUrl = paymentService.getPaymentAuthorizationUrl(orderId);

        return paymentUrl;
    }


    @GetMapping(value = "/confirm/{order}")
    public ResponseEntity<Response> confirmPayment(@PathVariable String order) throws Exception {
        Long orderId = Long.valueOf(order);
        return paymentService.confirmPayment(orderId);
    }
}
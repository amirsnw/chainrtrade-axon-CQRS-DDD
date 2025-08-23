package com.chaintrade.payment.query;

import com.chaintrade.core.events.PaymentFailedEvent;
import com.chaintrade.core.events.PaymentSucceededEvent;
import com.chaintrade.payment.core.data.PaymentEntity;
import com.chaintrade.payment.core.data.PaymentStatus;
import com.chaintrade.payment.core.event.PaymentRefundedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
public class PaymentProjection {
    private final PaymentRepository paymentRepository;

    public PaymentProjection(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @EventHandler
    public void on(PaymentSucceededEvent event) {
        PaymentEntity payment = new PaymentEntity(
                event.paymentId(),
                event.orderId(),
                event.customerId(),
                event.amount(),
                event.currency(),
                event.paymentMethod(),
                event.transactionId(),
                PaymentStatus.SUCCEEDED
        );
        paymentRepository.save(payment);
    }

    @EventHandler
    public void on(PaymentFailedEvent event) {
        PaymentEntity payment = new PaymentEntity();
        payment.setPaymentId(event.paymentId());
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
    }

    @EventHandler
    public void on(PaymentRefundedEvent event) {
        paymentRepository.findById(event.paymentId()).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        });
    }
} 
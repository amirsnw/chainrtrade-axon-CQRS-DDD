package com.chaintrade.payment.aggregate;

import com.chaintrade.core.commands.InitiatePaymentCommand;
import com.chaintrade.core.events.PaymentFailedEvent;
import com.chaintrade.core.events.PaymentSucceededEvent;
import com.chaintrade.payment.command.RefundPaymentCommand;
import com.chaintrade.payment.core.data.PaymentStatus;
import com.chaintrade.payment.core.event.PaymentRefundedEvent;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.math.BigDecimal;
import java.util.UUID;

@Aggregate(type = "payment-group")
@NoArgsConstructor
public class PaymentAggregate {

    @AggregateIdentifier
    private String paymentId;

    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String transactionId;
    private PaymentStatus status;

    @CommandHandler
    public PaymentAggregate(InitiatePaymentCommand command) {
        // Simulate payment processing
        if (Math.random() > 0.1) { // 90% success rate
            AggregateLifecycle.apply(new PaymentSucceededEvent(
                    command.paymentId(),
                    command.orderId(),
                    command.customerId(),
                    command.amount(),
                    command.currency(),
                    command.paymentMethod(),
                    UUID.randomUUID().toString()
            ));
        } else {
            AggregateLifecycle.apply(new PaymentFailedEvent(
                    command.paymentId(),
                    "Payment processing failed"
            ));
        }
    }

    @CommandHandler
    public void handle(RefundPaymentCommand command) {
        if (status != PaymentStatus.SUCCEEDED) {
            throw new IllegalStateException("Cannot refund a payment that hasn't succeeded");
        }
        AggregateLifecycle.apply(new PaymentRefundedEvent(
                command.paymentId(),
                command.reason(),
                UUID.randomUUID().toString()
        ));
    }

    @EventSourcingHandler
    public void on(PaymentSucceededEvent event) {
        this.paymentId = event.paymentId();
        this.orderId = event.orderId();
        this.customerId = event.customerId();
        this.amount = event.amount();
        this.currency = event.currency();
        this.paymentMethod = event.paymentMethod();
        this.transactionId = event.transactionId();
        this.status = PaymentStatus.SUCCEEDED;
    }

    @EventSourcingHandler
    public void on(PaymentFailedEvent event) {
        this.paymentId = event.paymentId();
        this.status = PaymentStatus.FAILED;
    }

    @EventSourcingHandler
    public void on(PaymentRefundedEvent event) {
        this.status = PaymentStatus.REFUNDED;
    }
} 
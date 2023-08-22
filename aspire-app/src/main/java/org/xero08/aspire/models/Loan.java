package org.xero08.aspire.models;

import com.mongodb.BasicDBObject;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.xero08.aspire.LoanState;

import java.util.List;

@Getter
@Setter
@Builder
public class Loan {
    private String loanId;
    private String customerId;
    private double amount;
    private double amountPaid;
    private int terms;
    private LoanState state;
    // Each loan shall consist of a number of dates on which the payment SHOULD be made
    private List<Payment> paymentsScheduled;
    // When a payment is made, check every time if the full payment has been made
}

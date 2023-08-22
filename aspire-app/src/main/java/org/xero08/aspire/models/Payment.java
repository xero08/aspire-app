package org.xero08.aspire.models;

import lombok.*;
import org.xero08.aspire.LoanState;
import java.util.Date;

@Getter
@Setter
@Builder
public class Payment {
    private Date date;
    private double amount;
    private LoanState paymentState; // Legal values are PENDING and PAID
}

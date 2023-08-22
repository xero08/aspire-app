package org.xero08.aspire.controllers;

import com.mongodb.BasicDBObject;
import org.springframework.web.bind.annotation.*;
import org.xero08.aspire.models.Loan;
import org.xero08.aspire.models.LoanDao;

import java.util.List;

@RestController("/loans")
public class LoanController {
    @GetMapping("/loans/view/{customerId}")
    public List<Loan> viewLoans(@PathVariable String customerId) {
        // Fetch a list of loans from the DB by the customerId
        return LoanDao.getInstance().getLoansByCustomerId(customerId);
    }

    @PostMapping("/loans/create/{customerId}")
    public BasicDBObject createLoan(@PathVariable String customerId, @RequestParam("terms") int terms, @RequestParam("amount") double amount) {
        return LoanDao.getInstance().createLoan(customerId, terms, amount);
    }

    @PostMapping("/loans/makePayment/{loanId}")
    public BasicDBObject makePayment(@PathVariable String loanId, @RequestParam("amount") double amount, @RequestParam("customerId") String customerId) {
        return LoanDao.getInstance().makePayment(loanId, amount, customerId);
    }
}

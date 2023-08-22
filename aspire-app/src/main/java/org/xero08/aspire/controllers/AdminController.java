package org.xero08.aspire.controllers;

import com.mongodb.BasicDBObject;
import org.springframework.web.bind.annotation.*;
import org.xero08.aspire.models.AdminDao;
import org.xero08.aspire.models.LoanDao;

@RestController("/admin")
public class AdminController {
    @PostMapping("/admin/approve/{loanId}")
    public BasicDBObject approveLoan(@PathVariable String loanId, @RequestParam("username") String username, @RequestParam("token") String token) {
        // Validate the admin user
        if (!AdminDao.getInstance().validateAdmin(username, token)) {
            return new BasicDBObject("response", false).append("reason", "Invalid admin credentials");
        }
        if (LoanDao.getInstance().validateLoan(loanId)) {
            return new BasicDBObject("response", false).append("reason", "Invalid loanId");
        }
        // Approve the loan by the loanId
        return new BasicDBObject("response", LoanDao.getInstance().approveLoan(loanId, username));
    }
}

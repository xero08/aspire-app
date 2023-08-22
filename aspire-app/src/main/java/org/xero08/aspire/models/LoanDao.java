package org.xero08.aspire.models;

import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.*;
import org.bson.conversions.Bson;
import org.xero08.aspire.DBConnection;
import org.xero08.aspire.LoanState;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class LoanDao {
    private static final String LOANS = "loans";
    private static final String RESPONSE = "response";
    private static final String AMOUNT = "amount";
    private static final String REASON = "reason";
    private static final String CUSTOMER_ID = "customerId";
    private static final String TERMS = "terms";
    private static final String ID = "_id";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private LoanDao() {
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    private static LoanDao instance;

    // fields

    public static LoanDao getInstance() {
        instance = new LoanDao();
        return instance;
    }

    protected String getDateAsYYYYMMDD() {
        return simpleDateFormat.format(new Date());
    }

    public List<Loan> getLoansByCustomerId(String customerId) {
        List<Loan> output = new ArrayList<>();
        MongoCollection<Document> collection = DBConnection.database.getCollection(LOANS);
        Bson filter = eq(CUSTOMER_ID, customerId);
        try (MongoCursor<Document> cursor = collection.find(filter).iterator()) {
            while (cursor.hasNext()) {
                BsonDocument loanInfo = cursor.next().toBsonDocument();
                List<Payment> paymentsScheduled = new ArrayList<>();
                loanInfo.getArray("paymentsScheduled", new BsonArray()).stream().map(BsonValue::asDocument).forEach(payment -> {
                    try {
                        paymentsScheduled.add(
                                Payment.builder()
                                        .date(simpleDateFormat.parse(payment.getString("date").getValue()))
                                        .amount(payment.getDouble(AMOUNT).getValue())
                                        .build()
                        );
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                });
                Loan loan = Loan.builder()
                        .loanId(loanInfo.getString(ID).getValue())
                        .customerId(customerId)
                        .amount(loanInfo.getDouble(AMOUNT).getValue())
                        .amountPaid(loanInfo.getDouble("amountPaid").getValue())
                        .terms(loanInfo.getInt32(TERMS).getValue())
                        .state(LoanState.valueOf(loanInfo.getString("state", new BsonString(LoanState.PENDING.name())).getValue()))
                        .paymentsScheduled(paymentsScheduled)
                        .build();
                output.add(loan);
            }
        } catch (Exception ignored) {

        }
        return output;
    }

    public BasicDBObject createLoan(String customerId, int terms, double amount) {
        // Create a document in loans
        BasicDBObject loanDetails = new BasicDBObject();
        loanDetails.put(ID, UUID.randomUUID().toString());
        loanDetails.put(CUSTOMER_ID, customerId);
        loanDetails.put(AMOUNT, amount);
        loanDetails.put("amountPaid", 0.0);
        loanDetails.put(TERMS, terms);
        loanDetails.put("createdOn", getDateAsYYYYMMDD());
        // Set the loan as PENDING
        loanDetails.put("state", LoanState.PENDING);
        // Initialize the payment schedule
        List<Payment> paymentSchedule = createPaymentSchedule(terms, amount);
        BasicDBList paymentsList = new BasicDBList();
        for(Payment payment: paymentSchedule) {
            paymentsList.add(new BasicDBObject("date", simpleDateFormat.format(payment.getDate())).append(AMOUNT, payment.getAmount()).append("state", payment.getPaymentState().name()));
        }
        loanDetails.put("paymentsScheduled", paymentsList);
        DBConnection.database.getCollection(LOANS).insertOne(new Document(loanDetails.toMap()));
        return loanDetails;
    }


    public boolean approveLoan(String loanId, String adminUsername) {
        BasicDBObject loanDetails = new BasicDBObject(ID, loanId);
        BasicDBObject updateQuery = new BasicDBObject("$set", new BasicDBObject("state", LoanState.APPROVED).append("approvedOn", getDateAsYYYYMMDD()).append("approvedBy", adminUsername));
        DBConnection.database.getCollection(LOANS).updateOne(loanDetails, updateQuery);
        return true;
    }

    protected List<Payment> createPaymentSchedule(int terms, double amount) {
        List<Double> amounts = new ArrayList<>();
        double amountPerPayment = amount/terms;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        amountPerPayment = Double.parseDouble(decimalFormat.format(amountPerPayment));
        double lastPayment = amountPerPayment;
        if (terms * amountPerPayment < amount) {
            lastPayment = amount - (terms - 1) * amountPerPayment;
        }
        for(int termIndex = 0; termIndex < terms - 1; termIndex++) {
            amounts.add(amountPerPayment);
        }
        amounts.add(lastPayment);

        List<Date> dates = new ArrayList<>();

        Date paymentDate = new Date();
        for(int termIndex = 0; termIndex < terms; termIndex++) {
            dates.add(paymentDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(paymentDate);
            calendar.add(Calendar.DATE, 7);
            paymentDate = calendar.getTime();
        }

        List<Payment> paymentSchedule = new ArrayList<>();
        for(int termIndex = 0; termIndex < terms; termIndex++) {
            paymentSchedule.add(new Payment(dates.get(termIndex), amounts.get(termIndex), LoanState.PENDING));
        }

        return paymentSchedule;
    }

    public BasicDBObject makePayment(String loanId, double amount, String customerId) {
        BasicDBObject output = new BasicDBObject();
        // Record a payment against the loan
        if (validateLoan(loanId)) {
            return new BasicDBObject(RESPONSE, false).append(REASON, "Invalid loanId");
        }
        // If the status of the loan is PAID, then return error saying loan already paid
        BasicDBObject loanDetails = new BasicDBObject(ID, loanId);
        try(MongoCursor<Document> cursor = DBConnection.database.getCollection("loans").find(loanDetails).cursor()) {
            Document document = cursor.next();
            String loanState = document.getString("state");
            if (Objects.equals(loanState, LoanState.PAID.name())) {
                return new BasicDBObject(RESPONSE, false).append(REASON, "Loan already paid up");
            }
            if (Objects.equals(loanState, LoanState.PENDING.name())) {
                return new BasicDBObject(RESPONSE, false).append(REASON, "Loan not approved");
            }
            if (!Objects.equals(customerId, document.getString(CUSTOMER_ID))) {
                return new BasicDBObject(RESPONSE, false).append(REASON, "Loan doesn't belong to customer");
            }
            // Get the sum of amounts paid so far
            double amountPaid = document.getDouble("amountPaid");
            double totalAmount = document.getDouble(AMOUNT);
            double amountRemaining = totalAmount - amountPaid;
            if (amount > amountRemaining) {
                return new BasicDBObject(RESPONSE, false).append(REASON, "Extra payment entered");
            }

            boolean fullyPaid = amountRemaining == amount;
            // Find out which repayment was last made
            int nextPaymentIndex = 0;
            double amountToBePaid = 0.0;
            ArrayList<Document> paymentsScheduled = document.get("paymentsScheduled", new ArrayList<>());
            for (Document payment: paymentsScheduled) {
                if (Objects.equals(payment.getString("state"), LoanState.PENDING.name())) {
                    amountToBePaid = payment.getDouble(AMOUNT);
                    nextPaymentIndex++;
                    break;
                }
            }
            if (amount < amountToBePaid) {
                return new BasicDBObject(RESPONSE, false).append(REASON, "Can't pay less than scheduled amount");
            }
            BasicDBObject updateQueryInner = new BasicDBObject("paymentsScheduled." + nextPaymentIndex + ".state", LoanState.PAID.name());
            updateQueryInner.put("amountPaid", amountPaid + amount);
            if (fullyPaid) {
                updateQueryInner.put("state", LoanState.PAID);
            }
            BasicDBObject updateQuery = new BasicDBObject("$set", updateQueryInner);
            DBConnection.database.getCollection(LOANS).updateOne(loanDetails, updateQuery);
            output.put(RESPONSE, true);
        }
        return output;
    }

    public boolean validateLoan(String loanId) {
        BasicDBObject loanDetails = new BasicDBObject(ID, loanId);
        try(MongoCursor<Document> cursor = DBConnection.database.getCollection(LOANS).find(loanDetails).cursor()) {
            return !cursor.hasNext();
        }
    }
}

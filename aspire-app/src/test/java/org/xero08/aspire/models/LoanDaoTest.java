package org.xero08.aspire.models;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xero08.aspire.DBConnection;
import org.xero08.aspire.LoanState;

import java.util.Arrays;
import java.util.List;

public class LoanDaoTest {
    LoanDao loanDao = LoanDao.getInstance();

    @BeforeEach
    public void setup() {
        ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString("mongodb://localhost:27017")).serverApi(serverApi).build();
        MongoClient client = MongoClients.create(settings);
        MongoDatabase database = client.getDatabase("aspire-test");
        DBConnection.setDatabase(database);
    }

    @AfterEach
    public void teardown() {
        DBConnection.database.drop();
    }

    @Test
    public void testGetLoansByCustomerId() {
        BasicDBObject loanDocument = BasicDBObject.parse("{\n" +
                "    \"_id\" : \"52043972-0487-413a-8cf9-d76e4c88683c\",\n" +
                "    \"customerId\" : \"customer001\",\n" +
                "    \"amount\" : 10009.0,\n" +
                "    \"amountPaid\" : 10009.0,\n" +
                "    \"terms\" : NumberInt(11),\n" +
                "    \"createdOn\" : \"2023-08-22\",\n" +
                "    \"state\" : \"PAID\",\n" +
                "    \"paymentsScheduled\" : [\n" +
                "        {\n" +
                "            \"date\" : \"2023-08-22\",\n" +
                "            \"amount\" : 909.91,\n" +
                "            \"state\" : \"PAID\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"date\" : \"2023-08-29\",\n" +
                "            \"amount\" : 909.91,\n" +
                "            \"state\" : \"PAID\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"date\" : \"2023-09-05\",\n" +
                "            \"amount\" : 909.91,\n" +
                "            \"state\" : \"PENDING\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"date\" : \"2023-09-12\",\n" +
                "            \"amount\" : 909.91,\n" +
                "            \"state\" : \"PENDING\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"date\" : \"2023-09-19\",\n" +
                "            \"amount\" : 909.91,\n" +
                "            \"state\" : \"PENDING\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"date\" : \"2023-09-26\",\n" +
                "            \"amount\" : 909.91,\n" +
                "            \"state\" : \"PENDING\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"date\" : \"2023-10-03\",\n" +
                "            \"amount\" : 909.91,\n" +
                "            \"state\" : \"PENDING\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"date\" : \"2023-10-10\",\n" +
                "            \"amount\" : 909.91,\n" +
                "            \"state\" : \"PENDING\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"date\" : \"2023-10-17\",\n" +
                "            \"amount\" : 909.91,\n" +
                "            \"state\" : \"PENDING\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"date\" : \"2023-10-24\",\n" +
                "            \"amount\" : 909.91,\n" +
                "            \"state\" : \"PENDING\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"date\" : \"2023-10-31\",\n" +
                "            \"amount\" : 909.91,\n" +
                "            \"state\" : \"PENDING\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"approvedBy\" : \"admin001\",\n" +
                "    \"approvedOn\" : \"2023-08-22\"\n" +
                "}\n");
        DBConnection.database.getCollection("loans").insertOne(new Document(loanDocument));
        Assertions.assertEquals(1, loanDao.getLoansByCustomerId("customer001").size());
    }

    @Test
    public void testCreateLoan() {
        loanDao.createLoan("customer001", 3, 10000.0);
        Assertions.assertEquals(3, loanDao.getLoansByCustomerId("customer001").get(0).getTerms());
    }

    @Test
    public void testApproveLoan() {
        loanDao.createLoan("customer001", 3, 10000.0);
        Assertions.assertEquals(LoanState.PENDING, loanDao.getLoansByCustomerId("customer001").get(0).getState());
        loanDao.approveLoan(loanDao.getLoansByCustomerId("customer001").get(0).getLoanId(), "admin001");
        Assertions.assertEquals(LoanState.APPROVED, loanDao.getLoansByCustomerId("customer001").get(0).getState());
    }

    @Test
    public void testMakePayment() {
        loanDao.createLoan("customer001", 3, 10000.0);
        loanDao.approveLoan(loanDao.getLoansByCustomerId("customer001").get(0).getLoanId(), "admin001");
        loanDao.makePayment(loanDao.getLoansByCustomerId("customer001").get(0).getLoanId(), 4000.0, "customer001");
        Assertions.assertEquals(4000.0, loanDao.getLoansByCustomerId("customer001").get(0).getAmountPaid());
    }
}

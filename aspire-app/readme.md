# Aspire-App
This implements the basic functionality of a loan application. The basic features are
1. Allowing a customer to apply for a loan with specified number of terms and loan amount.
2. Allowing an admin to approve a loan, post which payments may be made for the loan.
3. Allowing a customer to make payments for the loan at specific intervals.
4. Allowing a customer to view their existing loans.

The app is built using SpringBoot on Java and connects to a MongoDB database. MongoDB must be setup in advance to run 
this application. Setup MongoDB with no authentication and on the default port (`27017`). Create the bootstrap data for
running the application by create a DB by the name `aspire` and a collection by the name `admins` inside it. Have the 
following document in it:
```
{
    "username": "admin001",
    "token": "gargantua"
}
```

This will create a dummy admin user to allow loans to be approved.

The easiest way to then run the app is to import this as a Maven project into a supported IDE and running the SpringBoot
application.

The Postman collection contains 4 minimal API requests.
1. To create a loan.
2. To view loans for a customer.
3. To approve loans by an admin.
4. To make a payment by a customer.

Certain design choices have been made:
1. Using a NoSQL database - this was done with the intent that a lot of information in this schema is non-relational and
would be repeated were a relational model to be used.
2. Default restrictions to not allow payments for a loan by a different customer.
3. Default restrictions to not allow payments for a loan until it has been approved.
4. Having some form of authorization on the admin actions.


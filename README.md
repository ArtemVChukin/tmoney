[![Build Status](https://travis-ci.com/ArtemVChukin/tmoney.svg?branch=master)](https://travis-ci.com/ArtemVChukin/tmoney)
[![codecov](https://codecov.io/gh/ArtemVChukin/tmoney/branch/master/graph/badge.svg)](https://codecov.io/gh/ArtemVChukin/tmoney)

# Transfer Money
## RESTful API for money transfers between accounts.


Application starts on localhost port 8081

### Available Services

| HTTP METHOD | PATH | USAGE |
| -----------| ------ | ------ |
| POST  | api/accounts | create a new account
| PUT   | api/accounts/{number} | update account by account number
| DELETE| api/accounts/{number} | remove account by account number | 
| GET   | api/accounts | get all accounts | 
| GET   | api/accounts/{number} | get account by account number | 
| GET   | api/accounts?name={name} | get account by name | 
| POST  | api/transactions | create and apply new transaction | 
| GET   | api/transactions | get all transactions | 
| GET   | api/transactions/{id} | get transaction by transaction id |
| GET   | api/transactions?account={account} | get all transactions by account number| 


### Http Status
- 200 OK: The request has succeeded
- 204 OK: The request has succeeded. No data returned
- 400 Bad Request: The request could not be understood by the server 
- 404 Not Found: The requested resource cannot be found
- 422 Unprocessable Entity: operation could not be performed
- 500 Internal Server Error: The server encountered an unexpected condition 

### Sample JSON for Account and Transaction

##### Account (field "number" is not required for post operation):

```sh
{  
   "number":"408178100000000000000000",
   "name":"my account",
   "balance":10.00
} 
```

#### Transaction (field "id" is not required for post operation):
```sh
{  
   "id":1,
   "debit":"408178100000000000000001",
   "credit":"408178100000000000000002",
   "amount":100.00
}
```

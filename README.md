# WEX Transaction Service

REST API built with **Java 25** and **Spring Boot 4** to manage purchase transactions and convert their values using official exchange rates from the **U.S. Treasury Reporting Rates of Exchange API**.

The project follows **Clean Architecture** principles, separating business rules from infrastructure and delivery concerns.

---

## Features

- Create purchase transactions
- Persist transactions locally (H2)
- Convert purchase amounts to another currency
- Use the exchange rate active on or before the transaction date
- Limit exchange rate lookup to a 6-month lookback window
- Full unit test coverage for core business logic

---

## Tech Stack

- Java 25
- Spring Boot 4
- Spring Web / Validation
- Spring Data JPA
- H2 Database
- OpenFeign
- MapStruct
- Lombok
- JUnit 5 + Mockito

---

## Architecture


- **Core** layer contains all business rules and is framework-agnostic
- **Infra** implements persistence and external integrations
- **Entrypoint** handles HTTP requests and validations

---

## API Endpoints

### Create Transaction

**POST** `/purchase/transaction`

```json
{
  "description": "Office supplies",
  "transactionDate": "2025-12-01",
  "amount": 10.555
}
```

### Convert Transaction

**GET** `/purchase/transaction/{id}/convert?country_currency={country_currency}`

```json
{
  "id": 1,
  "description": "Office supplies",
  "transactionDate": "2025-12-01",
  "amount": 10.56,
  "exchangeRate": 1.549,
  "convertedAmount": 6.78
}
```

### Running the application

```bash
./mvnw spring-boot:run
```

### Docker

```bash
docker build -t wex-transaction .
docker run --rm -p 8080:8080 wex-transaction
```
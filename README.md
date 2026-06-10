# Sale Microservice (RetailFlow)

Runs on port **8091**. Talks to Product, Catalog, Invoice, and AuditLog services via Feign.

## 1. Setup

Create the database in MySQL:
```sql
CREATE DATABASE saledb;
```

Open `src/main/resources/application.properties` and update:
- `spring.datasource.password` → your MySQL root password
- The 4 service URLs at the bottom to match your teammates' real ports:
  - `product.service.url`
  - `catalog.service.url`
  - `auditlog.service.url`
  - `invoice.service.url`

## 2. Start order
1. Eureka Server (8761)
2. AuditLog Service (8082)
3. Product Service
4. Catalog Service
5. Sale Service (this) — `mvn spring-boot:run` or run SaleApplication
6. Invoice Service (needed only for COMPLETED-sale invoice creation)

## 3. Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| POST   | /api/sales | Create sale (auto-creates invoice if COMPLETED) |
| PUT    | /api/sales/{id} | Update quantity/status |
| DELETE | /api/sales/{id} | Cancel sale |
| GET    | /api/sales/{id} | Get one sale |
| GET    | /api/sales | Get all sales |
| GET    | /api/sales/customer/{customerId} | Sales by customer |
| GET    | /api/sales/date-range?start=YYYY-MM-DD&end=YYYY-MM-DD | Sales by date range |
| GET    | /api/sales/paginated?page=0&size=5 | Paginated sales |

## 4. Testing in stages

### Stage A — without Invoice service running
Use status PENDING so no invoice is created. This still verifies Product + Catalog calls.

POST http://localhost:8091/api/sales
```json
{ "productId": 1, "customerId": 101, "quantity": 2, "status": "PENDING" }
```
Expect 200 with a sale and `invoiceId: null`.

Prerequisite: Product ID 1 exists and is ACTIVE, and has an ACTIVE catalog
covering today, in your teammates' databases.

### Stage B — with Invoice service running
POST with status COMPLETED (or omit status). Expect a populated `invoiceId`.

## 5. Common errors
- "Product not found" → Product service down or wrong port, or product id missing.
- "Product has no active catalog listing for today" → no ACTIVE catalog row
  whose effectiveDate <= today <= expiryDate for that product.
- "Access denied for user 'root'" → wrong DB password in application.properties.

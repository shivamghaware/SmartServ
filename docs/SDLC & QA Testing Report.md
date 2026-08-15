# SDLC & QA Testing Report
**SmartServ – Enterprise Automobile Service Management Platform**
*Software Development Life Cycle, Fault-Tolerance Strategies & Verification Report*

---

## 1. Software Development Life Cycle (SDLC) Methodology

### 1.1 Applied Methodology: Agile Scrum with Continuous Integration & Delivery (CI/CD)
The development and evolution of the **SmartServ** enterprise platform adheres to the **Agile Scrum framework**, integrated with modern DevOps engineering practices. This ensures iterative delivery, high architectural visibility, rapid feedback cycles, and continuous quality validation.

```
+--------------------------------------------------------------------------------------------------------+
|                                    AGILE SPRINT LIFECYCLE (2-WEEK CADENCE)                             |
+--------------------------------------------------------------------------------------------------------+
|  1. Product Backlog Refinement  --> User stories mapped to IEEE 830 functional requirements (FR-01..24)|
|  2. Sprint Planning             --> Task decomposition, OpenAPI contract definition, velocity sizing  |
|  3. Iterative Development       --> TDD/BDD, Git feature branches, Spring Boot 3 & React 19 modularity |
|  4. Automated Continuous Testing--> Maven compiler, JUnit 5 unit tests, Mockito mocks, ESLint checks   |
|  5. Sprint Review & Demo        --> End-to-end integration demo with live Razorpay sandbox validation  |
|  6. Sprint Retrospective        --> Root Cause Analysis (RCA), performance profiling, refactoring     |
+--------------------------------------------------------------------------------------------------------+
```

---

### 1.2 Environment Topology & Quality Gates

| Environment | Purpose & Infrastructure | Quality Gate & Verification Criteria |
| :--- | :--- | :--- |
| **Local Development** | Developer workstations running Spring Boot DevTools, Vite HMR, and local MySQL instances. | Pre-commit static analysis, ESLint validation, Unit Test execution. |
| **Testing / QA** | Dockerized staging environment (`Dockerfile`) with containerized MySQL and Razorpay sandbox credentials. | Automated integration tests, REST API contract validation, regression suite. |
| **Production Ready** | Containerized cloud deployment behind reverse proxy with strict SSL/TLS, environment secrets, and Spring Actuator metrics. | Performance benchmarking (<200ms latency), security vulnerability scanning, zero high-severity CVEs. |

---

## 2. Edge Cases Handled & Exception Handling Strategy

### 2.1 Centralized Exception Handling Architecture

SmartServ employs a centralized exception handling architecture powered by Spring's `@RestControllerAdvice` in [GlobalExceptionHandler.java](../core-service/src/main/java/com/smartserv/exceptions/GlobalExceptionHandler.java). This decouples error transformation from business logic and guarantees **RFC 7807 Problem Details** compliance across all RESTful endpoints.

```
                  +-------------------------------------------------------------+
                  |                     REST Client / SPA                       |
                  +-------------------------------------------------------------+
                                                 ▲
                                                 │ Standardized JSON Error Response
                                                 │ (HTTP 4xx / 5xx + Timestamp)
                  +-------------------------------------------------------------+
                  |         com.smartserv.exceptions.GlobalExceptionHandler     |
                  |                   (@RestControllerAdvice)                   |
                  +-------------------------------------------------------------+
                                                 ▲
                  +------------------------------+------------------------------+
                  │                              │                              │
     +--------------------------+  +--------------------------+  +--------------------------+
     | Domain-Specific Errors   |  | Security & Auth Errors   |  | Validation & System Errs |
     | - InsufficientStock      |  | - UnauthorizedException  |  | - MethodArgumentNotValid |
     | - DuplicateJobCreation   |  | - InvalidRoleException   |  | - OptimisticLockException|
     | - DuplicateInvoice       |  | - BadCredentialsException|  | - HttpMediaTypeNotSupported|
     +--------------------------+  +--------------------------+  +--------------------------+
```

---

### 2.2 Critical Domain Edge Cases & Code-Level Defenses

The following matrix documents real-world operational edge cases identified and solved in the codebase:

```
+-----------------------------------------------------------------------------------------------------------------------------+
| Feature Area        | Edge Case / Failure Scenario                       | Architectural & Code-Level Defense                       |
+-----------------------------------------------------------------------------------------------------------------------------+
| Appointments        | Customer attempts to schedule a service in the past| InvalidDateException thrown:                             |
|                     | (e.g. yesterday's date).                           | dto.getRequestDate().isBefore(LocalDate.now()) -> HTTP 400|
+-----------------------------------------------------------------------------------------------------------------------------+
| RSA Emergency       | Customer triggers Emergency RSA from remote area   | validateRsaCoordinates() verifies latitude/longitude     |
|                     | where standard date checks would fail.             | format; bypasses past-date check with is_rsa=true.       |
+-----------------------------------------------------------------------------------------------------------------------------+
| RSA Immutability    | Customer attempts to modify an ongoing emergency   | InvalidOperationException thrown: RSA records are        |
|                     | dispatch.                                          | strictly immutable to prevent dispatch corruption.        |
+-----------------------------------------------------------------------------------------------------------------------------+
| Job Card Creation   | Manager attempts to create a duplicate Job Card    | existsByAppointmentId(appointmentId) check throws        |
|                     | for an already active appointment.                 | DuplicateJobCreationException (HTTP 409).                |
+-----------------------------------------------------------------------------------------------------------------------------+
| Job Card Lifecycle  | Mechanic attempts to complete a Job Card without   | InvalidOperationException thrown:                        |
|                     | adding any spare parts or labor items.             | if (jobCard.getItems().isEmpty()) -> Work cannot finish. |
+-----------------------------------------------------------------------------------------------------------------------------+
| Job Card Cancel     | Manager cancels a Job Card that already consumed   | Compensation Loop: System restores all deducted item      |
|                     | 5 units of brake pads from warehouse.              | quantities back to Inventory.stockQuantity in DB.        |
+-----------------------------------------------------------------------------------------------------------------------------+
| Inventory Requisition| Mechanic requests 10 units when only 3 exist in   | InsufficientStockException thrown before item insertion, |
|                     | stock.                                             | preventing negative inventory states.                    |
+-----------------------------------------------------------------------------------------------------------------------------+
| Inventory Soft Delete| Manager tries to add an archived/deleted part to  | isDeleted() check rejects deleted SKUs from being         |
|                     | an active Job Card.                                | assigned to new repairs.                                 |
+-----------------------------------------------------------------------------------------------------------------------------+
| Billing & Invoices  | Manager attempts to generate multiple invoices for | existsByJobCardId(jobCardId) check throws                |
|                     | the same completed Job Card.                       | DuplicateInvoiceException (HTTP 409).                    |
+-----------------------------------------------------------------------------------------------------------------------------+
| Razorpay Gateway    | Gateway experiences network timeout or live API    | Try/Catch fallback generates a deterministic mock order  |
|                     | key is not yet configured in local test env.       | (order_mock_*) allowing local dev testing to continue.   |
+-----------------------------------------------------------------------------------------------------------------------------+
| Payment Fraud Check | Malicious actor attempts to forge signature or     | HMAC-SHA256 recalculated on server; signature mismatch   |
|                     | inject an arbitrary razorpayPaymentId.             | marks invoice as FAILED and logs audit alert.            |
+-----------------------------------------------------------------------------------------------------------------------------+
| Vehicle Deletion    | Customer deletes a vehicle that has historical     | Soft-deletion (is_active=false) preserves database        |
|                     | completed invoices and service records.            | foreign key referential integrity for audit histories.   |
+-----------------------------------------------------------------------------------------------------------------------------+
```

---

## 3. Complete Test Plan Matrix

### 3.1 Unit Testing Suite (Domain Services & Validation)

| Test ID | Module | Component / Class | Scenario / Input Vector | Expected Output / Behavior | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **UT-AUTH-01** | Auth | `JwtUtils` | Mint JWT with valid user (`ID=1, Role=CUSTOMER`) | Valid 3-part Base64 URL-encoded JWT token generated | **PASSED** |
| **UT-AUTH-02** | Auth | `JwtUtils` | Validate token with tampered payload / bad signature | `validateToken(token)` returns `false` | **PASSED** |
| **UT-AUTH-03** | Auth | `UserServiceImpl` | Register user with duplicate email | `ResourceAlreadyExists` exception thrown | **PASSED** |
| **UT-AUTH-04** | Auth | `UserServiceImpl` | Authenticate with incorrect password | `BadCredentialsException` / Auth error thrown | **PASSED** |
| **UT-VEH-01** | Vehicle | `VehicleServiceImpl` | Register vehicle with unique plate number | Vehicle persisted with `isActive = true` | **PASSED** |
| **UT-VEH-02** | Vehicle | `VehicleServiceImpl` | Soft-delete vehicle by ID | Vehicle updated to `isActive = false`, remains in DB | **PASSED** |
| **UT-APPT-01** | Appointment | `AppointmentServiceImpl` | Book appointment with past date (`2020-01-01`) | `InvalidDateException` thrown | **PASSED** |
| **UT-APPT-02** | Appointment | `AppointmentServiceImpl` | Book emergency RSA with coordinates (`18.5204, 73.8567`) | Appointment created with `isRsa = true, status = PENDING` | **PASSED** |
| **UT-APPT-03** | Appointment | `AppointmentServiceImpl` | Modify RSA appointment after creation | `InvalidOperationException` thrown ("RSA immutable") | **PASSED** |
| **UT-JOB-01** | JobCard | `JobCardServiceImpl` | Create Job Card for `PENDING` (unapproved) appointment | `InvalidOperationException` thrown ("Must be approved") | **PASSED** |
| **UT-JOB-02** | JobCard | `JobCardServiceImpl` | Assign user with `ROLE_CUSTOMER` as Mechanic | `InvalidRoleException` thrown | **PASSED** |
| **UT-JOB-03** | JobCard | `JobCardServiceImpl` | Add spare part with requested qty (5) > available stock (2) | `InsufficientStockException` thrown | **PASSED** |
| **UT-JOB-04** | JobCard | `JobCardServiceImpl` | Add spare part with valid stock (Available: 10, Request: 2) | Inventory stock decremented to 8, JobCardItem created | **PASSED** |
| **UT-JOB-05** | JobCard | `JobCardServiceImpl` | Complete Job Card with zero items added | `InvalidOperationException` thrown | **PASSED** |
| **UT-JOB-06** | JobCard | `JobCardServiceImpl` | Cancel Job Card with items already added | Job Card marked `CANCELLED`, item quantities refunded to stock | **PASSED** |
| **UT-INV-01** | Inventory | `InventoryServiceImpl` | Create inventory item with duplicate SKU | `DuplicateSkuException` thrown | **PASSED** |
| **UT-INV-02** | Inventory | `InventoryServiceImpl` | Update stock quantity with negative value | Validation failure (`Min(0)`) | **PASSED** |
| **UT-INVC-01** | Invoices | `InvoiceServiceImpl` | Generate invoice for `IN_PROGRESS` Job Card | `InvalidOperationException` thrown ("Must be COMPLETED") | **PASSED** |
| **UT-INVC-02** | Invoices | `InvoiceServiceImpl` | Calculate tax on items total (Base: 1000, Tax: 18%) | Total amount = 1180.0, Tax amount = 180.0 | **PASSED** |
| **UT-INVC-03** | Invoices | `InvoiceServiceImpl` | Verify Razorpay payment with invalid HMAC signature | Status set to `FAILED`, returns `verified: false` | **PASSED** |
| **UT-INVC-04** | Invoices | `InvoiceServiceImpl` | Verify Razorpay payment with valid HMAC signature | Status set to `PAID`, `paidAt` timestamp recorded | **PASSED** |

---

### 3.2 Integration Testing Suite (End-to-End REST Workflows)

| Test ID | Module / Endpoint | HTTP Flow & Payloads | Expected Response & DB State | Status |
| :--- | :--- | :--- | :--- | :--- |
| **IT-E2E-01** | `POST /api/auth/register` $\rightarrow$ `POST /api/auth/login` | Register customer $\rightarrow$ Login with credentials | HTTP 200 OK + JWT Bearer token issued | **PASSED** |
| **IT-E2E-02** | `GET /api/vehicles` (No Token) | Anonymous HTTP request to secured endpoint | HTTP 401 Unauthorized | **PASSED** |
| **IT-E2E-03** | `GET /api/users` (With Customer JWT) | Customer token accessing Admin endpoint | HTTP 403 Forbidden (RBAC Guard) | **PASSED** |
| **IT-E2E-04** | `POST /api/vehicles` $\rightarrow$ `POST /api/appointments` | Add vehicle $\rightarrow$ Book service slot | HTTP 201 Created; Appointment in DB linked to vehicle | **PASSED** |
| **IT-E2E-05** | `PATCH /api/appointments/{id}/status` $\rightarrow$ `POST /api/job-cards` | Manager approves appointment $\rightarrow$ creates Job Card | Appointment becomes `IN_PROGRESS`, Job Card `CREATED` | **PASSED** |
| **IT-E2E-06** | `PATCH /api/job-cards/{id}/start` $\rightarrow$ `POST /api/job-cards/{id}/items` | Mechanic starts job $\rightarrow$ assigns brake pads (Qty: 2) | Stock reduced in DB; JobCard status `IN_PROGRESS` | **PASSED** |
| **IT-E2E-07** | `PATCH /api/job-cards/{id}/complete` $\rightarrow$ `POST /api/invoices/generate` | Mechanic completes job $\rightarrow$ Manager generates bill | Job Card becomes `BILLED`, Invoice `PENDING` | **PASSED** |
| **IT-E2E-08** | `POST /api/invoices/{id}/payment-order` $\rightarrow$ `POST /api/invoices/{id}/verify-payment` | Initiate Razorpay order $\rightarrow$ Verify signature callback | Invoice status transitions to `PAID`, receipt confirmed | **PASSED** |
| **IT-E2E-09** | `POST /api/job-cards/{id}/cancel` (Compensation) | Cancel job card having 4 consumed spark plugs | Inventory stock automatically restored by +4 in database | **PASSED** |

---

## 4. Test Summary & QA Sign-Off

```
===================================================================================
                              QA TEST EXECUTION SUMMARY
===================================================================================
Total Test Cases Executed : 30
Total Passed              : 30 (100%)
Total Failed              : 0 (0%)
Critical Defects Found    : 0
Regression Status         : Zero regressions detected across core business modules
Database Consistency     : Verified (ACID compliance & Entity Graph optimization)
Security Verification    : Passed (Stateless JWT, RBAC guards, HMAC-SHA256 signature)
===================================================================================
QA Recommendation         : APPROVED FOR PRODUCTION DEPLOYMENT (v1.0.0)
===================================================================================
```

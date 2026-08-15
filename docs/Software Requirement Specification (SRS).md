# Software Requirement Specification (SRS)
## SmartServ – Enterprise Automobile Service Management Platform

---

### Document Control & Metadata
- **Project Title:** SmartServ – Automobile Service Management & Roadside Assistance Platform
- **Document Version:** 1.0.0 (Enterprise Architecture Standard)
- **Standard Reference:** IEEE Std 830-1998 (Recommended Practice for Software Requirements Specifications)
- **Status:** Approved / Baseline Architecture
- **Date:** August 12, 2026

---

## 1. Introduction & Project Scope

### 1.1 Purpose
This document provides a comprehensive and definitive Software Requirement Specification (SRS) for the **SmartServ Automobile Service Management Platform**. It details the functional capabilities, performance criteria, security baselines, external interfaces, and domain constraints governing the design, implementation, verification, and deployment of the system.

### 1.2 Scope of the System
**SmartServ** is an automated, web-based enterprise platform engineered to streamline garage service workflows, manage vehicle lifecycles, optimize spare parts inventory, automate invoicing and cryptographic payment processing, and deliver real-time Emergency Roadside Assistance (RSA).

The system replaces legacy, manual service desk workflows with a secure, cloud-ready, multi-tenant capable architecture featuring:
- Customer digital onboarding, vehicle profile registry, and service booking with automated conflict validation.
- Emergency Roadside Assistance (RSA) incident dispatching with geolocation tracking.
- Workshop manager supervisory dashboard for appointment approvals, mechanic workload distribution, and job card tracking.
- Mechanic task orchestration, repair lifecycle tracking, and atomic spare parts inventory requisition with rollback compensation.
- Automated tax and billing calculation with Razorpay payment gateway integration and server-side HMAC-SHA256 signature verification.
- Comprehensive administrative user governance, role management, and operational analytics reporting.

### 1.3 Target Users & Stakeholder Personas

```
+-------------------------------------------------------------------------------------------------------------------------+
| Actor / Persona   | Description & Organizational Responsibilities                                                       |
+-------------------------------------------------------------------------------------------------------------------------+
| Admin             | System superuser responsible for platform-wide configuration, user provisioning, role assignments,  |
|                   | audit logging, global analytics, and master inventory governance.                                    |
+-------------------------------------------------------------------------------------------------------------------------+
| Manager           | Workshop supervisor managing daily shop operations, reviewing/approving appointment requests,       |
|                   | generating Job Cards, assigning mechanics, issuing invoices, and monitoring repair progress.        |
+-------------------------------------------------------------------------------------------------------------------------+
| Mechanic          | Garage technician accessing assigned Job Cards, updating real-time work status (Start/Complete),    |
|                   | and adding consumed inventory parts with atomic stock deductions.                                   |
+-------------------------------------------------------------------------------------------------------------------------+
| Customer          | Vehicle owner registering personal vehicles, booking scheduled service appointments, requesting     |
|                   | emergency RSA assistance, tracking repair progress live, and paying invoices online via Razorpay.   |
+-------------------------------------------------------------------------------------------------------------------------+
```

---

## 2. Overall System Architecture & Operating Context

### 2.1 System Context
SmartServ operates as a decoupled client-server architecture. The frontend Single Page Application (React 19, Vite, Bootstrap 5.3) communicates over HTTPS via JSON REST endpoints with a Spring Boot 3.5 (Java 21) backend connected to a MySQL 8.x relational database and third-party payment gateway APIs (Razorpay).

```
+-----------------------+          HTTPS/JSON          +-------------------------------+
|  SmartServ Frontend   | <==========================> |   SmartServ REST API Gateway  |
|  (React 19 + Vite)    |     (JWT Authorization)      |   (Spring Boot 3.5 / Java 21) |
+-----------------------+                              +-------------------------------+
                                                                 │              │
                                                JDBC Connection  │              │ HTTPS Webhook/API
                                                                 ▼              ▼
                                                       +---------------+  +------------------+
                                                       | MySQL 8.x DB  |  | Razorpay Gateway |
                                                       +---------------+  +------------------+
```

---

## 3. Functional Requirements (FR)

The functional requirements are itemized, uniquely identified, prioritized using MoSCoW notation (*Must Have, Should Have, Could Have*), and mapped to actor permissions.

### 3.1 Module 1: Authentication & User Management

| Req ID | Requirement Name | Description & Acceptance Criteria | Priority | Actor |
| :--- | :--- | :--- | :--- | :--- |
| **FR-01** | User Registration | The system must allow new customers to register by providing user name, unique email address, valid 10-digit mobile number, and password. Passwords must be hashed using BCrypt prior to database persistence. | Must Have | Customer |
| **FR-02** | User Authentication | The system must authenticate users via email and password, issuing a stateless JSON Web Token (JWT) signed with HMAC-SHA256 containing `userId`, `email`, `role`, and expiration timestamp. | Must Have | All Users |
| **FR-03** | Profile Management | Authenticated users must be able to view and update their profile details (User name, Mobile number). Email cannot be altered once registered. | Must Have | All Users |
| **FR-04** | User Administration | The Admin must be able to view, search, activate/deactivate, and update roles (`ADMIN`, `MANAGER`, `CUSTOMER`, `MECHANIC`) for all system user accounts. | Must Have | Admin |
| **FR-05** | Role-Based Access Control | The API Gateway and UI Router must enforce strict RBAC. Unauthorized attempts to access protected endpoints must yield HTTP `401 Unauthorized` or `403 Forbidden`. | Must Have | System |

---

### 3.2 Module 2: Vehicle Management

| Req ID | Requirement Name | Description & Acceptance Criteria | Priority | Actor |
| :--- | :--- | :--- | :--- | :--- |
| **FR-06** | Vehicle Registration | The system must allow customers to register vehicles by specifying unique Registration/Plate number, Make, Model, Manufacturing Year, and Fuel Type. | Must Have | Customer |
| **FR-07** | Vehicle Directory | Customers must be able to view all active vehicles associated with their account. Managers and Admins can view vehicles across all customer accounts. | Must Have | Customer, Manager, Admin |
| **FR-08** | Vehicle Modification & Soft Deletion | Customers must be able to edit vehicle details or deactivate (soft-delete with `is_active = false`) their vehicle records without breaking historical appointment foreign keys. | Must Have | Customer |

---

### 3.3 Module 3: Appointment Booking & Roadside Assistance (RSA)

| Req ID | Requirement Name | Description & Acceptance Criteria | Priority | Actor |
| :--- | :--- | :--- | :--- | :--- |
| **FR-09** | Service Booking | Customers must be able to book service appointments for their registered active vehicles by specifying a future date, time slot, problem description, and optional photo attachment URL. | Must Have | Customer |
| **FR-10** | Date Validation | The system must reject regular service appointment bookings scheduled in the past with an `InvalidDateException` (HTTP 400). | Must Have | System |
| **FR-11** | Emergency Roadside Assistance (RSA) | The system must allow customers to initiate emergency RSA requests with real-time GPS coordinates (`latitude`, `longitude`) and immediate `is_rsa = true` tagging, bypassing standard future-date restrictions. | Must Have | Customer |
| **FR-12** | Appointment Modification & Cancellation | Customers can update or cancel pending appointments. Once an appointment is `APPROVED` or `IN_PROGRESS`, direct customer modification is restricted. RSA requests are immutable once submitted. | Should Have | Customer |
| **FR-13** | Appointment Workflow Governance | Managers and Admins must be able to review pending appointments, approve them (transitioning state to `APPROVED`), or reject/cancel them with logged reasons. | Must Have | Manager, Admin |

---

### 3.4 Module 4: Job Card Lifecycle & Workshop Management

| Req ID | Requirement Name | Description & Acceptance Criteria | Priority | Actor |
| :--- | :--- | :--- | :--- | :--- |
| **FR-14** | Job Card Creation | Managers must be able to generate a unique Job Card from an `APPROVED` appointment, assigning a designated Mechanic and setting an estimated completion date. Appointment state transitions automatically to `IN_PROGRESS`. | Must Have | Manager |
| **FR-15** | Mechanic Reassignment | Managers can reassign a Job Card to a different qualified Mechanic provided the Job Card is not in `COMPLETED` or `CANCELLED` status. | Should Have | Manager |
| **FR-16** | Service Work Initiation | Mechanics must be able to view assigned Job Cards and transition state to `IN_PROGRESS`, automatically timestamping the `start_time`. | Must Have | Mechanic |
| **FR-17** | Job Card Completion | Mechanics can mark work as `COMPLETED` only if at least one spare part or labor item has been recorded. System records `completion_time` and marks the underlying Appointment as `COMPLETED`. | Must Have | Mechanic |
| **FR-18** | Job Card Cancellation & Compensation | If a Manager cancels a Job Card, a mandatory cancellation reason is captured, and the system automatically rolls back and refunds all consumed inventory stock quantities to the warehouse. | Must Have | Manager |

---

### 3.5 Module 5: Spare Parts & Inventory Management

| Req ID | Requirement Name | Description & Acceptance Criteria | Priority | Actor |
| :--- | :--- | :--- | :--- | :--- |
| **FR-19** | Spare Parts Catalog | Managers and Admins must be able to add, update, search, and view spare parts with SKU, Item Name, Unit Price, Stock Quantity, and Threshold Limits. | Must Have | Manager, Admin |
| **FR-20** | Atomic Part Requisition | When a Mechanic adds a part to a Job Card, the system must verify available stock, atomically decrement `stock_quantity`, and record a `JobCardItem` snapshot with locked unit pricing. If stock < quantity, throw `InsufficientStockException`. | Must Have | Mechanic |
| **FR-21** | Low Stock Alerts | The system must provide visual alerts and reporting when inventory levels fall at or below the designated minimum threshold. | Should Have | Manager, Admin |

---

### 3.6 Module 6: Billing, Invoicing & Razorpay Payments

| Req ID | Requirement Name | Description & Acceptance Criteria | Priority | Actor |
| :--- | :--- | :--- | :--- | :--- |
| **FR-22** | Invoice Generation | System/Manager must generate an official Invoice upon Job Card completion. Base amount is calculated as $\sum (\text{item.totalPrice})$, tax is applied via configurable percentage (default 18% GST), and status is set to `PENDING`. Job Card status becomes `BILLED`. | Must Have | Manager, System |
| **FR-23** | Razorpay Order Creation | When a customer clicks "Pay Now", the system creates a cryptographic payment order with Razorpay in INR (paise conversion: $\text{amount} \times 100$), storing `razorpay_order_id` and setting payment status to `INITIATED`. | Must Have | Customer |
| **FR-24** | Cryptographic Payment Verification | Upon gateway checkout completion, the server recalculates the HMAC-SHA256 digest of `orderId + "|" + paymentId` against the server-side secret key. If matched, status is marked `PAID` and `paid_at` timestamp is committed. If mismatched, marked `FAILED`. | Must Have | System, Customer |

---

## 4. Non-Functional Requirements (NFR)

```
+--------------------------------------------------------------------------------------------------------------------+
| Category          | Metric / Benchmark                               | Specification & Architectural Guarantee     |
+--------------------------------------------------------------------------------------------------------------------+
| Performance       | API Latency < 200ms (95th percentile)            | JPA NamedEntityGraphs to eliminate N+1;     |
|                   | Database Query Execution < 50ms                  | Indexed foreign keys & stateless JWT design |
+--------------------------------------------------------------------------------------------------------------------+
| Security          | Zero plaintext credentials                       | BCrypt (10 rounds); HMAC-SHA256 JWT;        |
|                   | Zero client-side trust for payments              | Server-side Razorpay HMAC signature checks  |
+--------------------------------------------------------------------------------------------------------------------+
| Reliability       | 99.9% Uptime; Atomic Transactions                | Spring @Transactional boundary rollbacks;   |
|                   | Idempotent operations on Job Cards and Invoices  | Unique database constraints (1:1 relations) |
+--------------------------------------------------------------------------------------------------------------------+
| Maintainability   | Clean Architecture (Layered Controller-Svc-Repo) | DTO abstraction layer; Centralized global   |
|                   | Strict standard coding conventions               | exception handler with RFC 7807 formatting  |
+--------------------------------------------------------------------------------------------------------------------+
```

### 4.1 Performance Requirements
- **NFR-01 (Response Times):** All standard CRUD REST API endpoints must respond in under **200 ms** under normal operational load (up to 100 concurrent requests).
- **NFR-02 (Fetch Optimization):** Complex relational fetches (e.g., retrieving a Job Card with its Appointment, Vehicle, Customer, Manager, Mechanic, and Items) must utilize JPA `@NamedEntityGraph` to execute within a single SQL join query, preventing the $N+1$ query defect.
- **NFR-03 (Frontend Bundle Performance):** The React frontend compiled via Vite must achieve a First Contentful Paint (FCP) of $< 1.2$ seconds over standard broadband connections.

### 4.2 Security Requirements
- **NFR-04 (Authentication Integrity):** All sensitive endpoints (excluding `/api/auth/**`) require valid JWT bearer tokens with standard claims verification and signature validation.
- **NFR-05 (Credential Encryption):** Passwords must never be stored or logged in plaintext and must be hashed using the industry-standard BCrypt adaptive hashing function.
- **NFR-06 (Payment Integrity):** Webhook and client payment callbacks must enforce server-side HMAC-SHA256 signature verification to prevent spoofing and unauthorized status updates.
- **NFR-07 (CORS Protection):** Strict Cross-Origin Resource Sharing (CORS) rules must restrict API access exclusively to whitelisted frontend origin URLs (`http://localhost:5173`, production domains).

### 4.3 Reliability & Availability
- **NFR-08 (Transactional Atomicity):** Multi-step operations (e.g., Job Card item deduction, invoice generation, status transitions) must execute within Spring `@Transactional` blocks to ensure automatic rollback on failure.
- **NFR-09 (Fault Tolerance & Fallback):** The Razorpay integration must provide deterministic error handling and development mock order fallbacks to prevent crashes during external gateway network timeouts.
- **NFR-10 (Soft Deletions):** Deletion of primary entities (Vehicles, Inventory Items) must utilize boolean soft-delete flags (`isActive`, `isDeleted`) to preserve relational audit trails for historical job cards and invoices.

### 4.4 Maintainability & Usability
- **NFR-11 (Layered Decoupling):** Codebase must maintain strict separation between Controller, Service, Repository, and Data Model tiers. JPA entities must never be directly exposed to REST clients; all inputs and outputs must pass through validated DTOs.
- **NFR-12 (Responsive Design):** The UI must be fully responsive across mobile (375px+), tablet (768px+), and desktop (1080px+) viewport breakpoints.

---

## 5. System Inputs, Outputs & Data Interfaces

### 5.1 System Inputs

| Interface / Flow | Source | Key Input Fields & Format | Validation Rules |
| :--- | :--- | :--- | :--- |
| **User Registration** | Customer | `userName` (String), `email` (Valid Email), `password` (Min 6 chars), `mobile` (10-digit regex) | Mandatory, Unique Email check |
| **Vehicle Registration** | Customer | `registrationNumber` (Alphanumeric), `make`, `model`, `year` (Int), `fuelType` (Enum) | Mandatory, Plate uniqueness |
| **Appointment Booking** | Customer | `vehicleId` (Long), `requestDate` (ISO Date), `scheduledTime` (String), `description` (String), `isRsa` (Boolean), `rsaCoordinates` (Lat/Lng string) | Date $\ge$ Today (if not RSA), Valid vehicle ownership |
| **Job Card Creation** | Manager | `appointmentId` (Long), `managerId` (Long), `mechanicId` (Long), `estimatedCompletionDate` (ISO Date) | Appointment must be `APPROVED`, No duplicate job card |
| **Spare Part Addition** | Mechanic | `inventoryItemId` (Long), `quantity` (Positive Integer) | Item active, $\text{quantity} \le \text{stockQuantity}$ |
| **Payment Verification** | Razorpay / Client | `razorpayOrderId`, `razorpayPaymentId`, `razorpaySignature`, `paymentMethod` (Enum) | Mandatory non-null cryptographic signature match |

### 5.2 System Outputs

| Interface / Flow | Target | Key Output Fields & Payload | Format / Protocol |
| :--- | :--- | :--- | :--- |
| **Auth Response** | Client App | JWT Bearer Token, `id`, `userName`, `email`, `role`, expiry timestamp | JSON (HTTP 200) |
| **Appointment DTO** | Client App | `appointmentId`, `vehicleDetails`, `status`, `requestDate`, `isRsa`, `rsaCoordinates` | JSON (HTTP 200/201) |
| **Job Card DTO** | Client App | `jobCardId`, `managerName`, `mechanicName`, `status`, `startTime`, `completionTime`, `items[]` | JSON (HTTP 200/201) |
| **Payment Order DTO** | Razorpay Modal | `orderId`, `amount`, `currency` ("INR"), `invoiceNumber`, `customerName`, `customerEmail`, `razorpayKey` | JSON (HTTP 200) |
| **Tax Invoice DTO** | Client / Print | `invoiceNumber`, `baseAmount`, `taxPercentage` (18%), `taxAmount`, `totalAmount`, `paymentStatus`, `paidAt` | JSON (HTTP 200/201) |
| **Error Diagnostics** | Client App | `timestamp`, `status` (HTTP code), `error`, `message`, `path`, field-level validation map | JSON (RFC 7807 Standard) |

---

## 6. Assumptions, Constraints & Dependencies

### 6.1 Assumptions
1. **Timezone Standardization:** The system assumes all operational service appointments and database timestamps are anchored to Indian Standard Time (IST / UTC+05:30).
2. **Taxation Model:** Invoicing assumes a single-tier Goods and Services Tax (GST) model with a default configurable rate of 18%.
3. **Currency & Denomination:** All monetary transactions are processed in Indian Rupees (INR) and converted into smallest currency units (paise) when interfacing with Razorpay.

### 6.2 Constraints
1. **Single Job Card per Appointment:** An appointment can be linked to exactly one active Job Card ($1:1$ invariant).
2. **Immutable RSA Appointments:** Due to emergency dispatch safety protocols, Roadside Assistance requests cannot be updated or postponed once submitted by the customer.
3. **Strict Stock Availability:** Job Card items cannot be added if requested quantity exceeds real-time warehouse stock.

### 6.3 External Dependencies
1. **Razorpay Payment Gateway:** External payment gateway dependency for processing online payments and generating secure checkout orders.
2. **MySQL Database Engine:** Relational database storage supporting InnoDB storage engine with ACID transactions and foreign key enforcement.
3. **Browser Compatibility:** Modern evergreen browsers supporting HTML5 Geolocation API (Chrome, Edge, Firefox, Safari) for RSA location capture.

---

## 7. Requirement Traceability Matrix (RTM Sample)

| Req ID | Requirement Summary | Architectural Component | Entity / Table | Test Verification Method |
| :--- | :--- | :--- | :--- | :--- |
| **FR-01** | User Registration | `AuthController`, `UserService` | `User` (`users`) | Automated Integration Test / Postman |
| **FR-02** | JWT Token Issuance | `JwtUtils`, `JwtAuthFilter` | `User` | Token Decryption & Signature Test |
| **FR-09** | Appointment Booking | `AppointmentController`, `AppointmentService` | `Appointment` (`appointments`) | Boundary Date Validation Test |
| **FR-11** | RSA Emergency Trigger | `AppointmentServiceImpl` | `Appointment` | Geolocation Format & Dispatch Test |
| **FR-14** | Job Card Generation | `JobCardController`, `JobCardService` | `JobCard` (`job_card`) | State Transition & Invariant Check |
| **FR-20** | Inventory Deduction | `JobCardServiceImpl`, `InventoryRepo` | `JobCardItem`, `Inventory` | Stock Concurrency & Unit Price Snapshot Test |
| **FR-24** | Payment Signature Verification | `InvoiceServiceImpl`, `RazorpayClient` | `Invoice` (`invoices`) | HMAC-SHA256 Digest Verification Test |

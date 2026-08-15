# System Architecture & UML Diagrams (Mermaid Code)
**SmartServ – Enterprise Automobile Service Management Platform**
*Comprehensive Visual Specifications & GitHub-Compatible Mermaid Diagrams*

---

## Table of Contents
1. [High-Level System Architecture & Data Flow Diagram (DFD)](#1-high-level-system-architecture--data-flow-diagram-dfd)
2. [Database Entity Relationship (ER) Diagram](#2-database-entity-relationship-er-diagram)
3. [UML Class Diagram & Relationships](#3-uml-class-diagram--relationships)
4. [Primary End-to-End Lifecycle Sequence Diagram](#4-primary-end-to-end-lifecycle-sequence-diagram)
5. [State Machine Diagrams](#5-state-machine-diagrams)

---

## 1. High-Level System Architecture & Data Flow Diagram (DFD)

This diagram visualizes the multi-tier architectural topology, network boundaries, security filter interception, REST controllers, service transactions, and third-party integrations (Razorpay).

```mermaid
flowchart TB
    %% Client Tier
    subgraph ClientTier ["Client Presentation Tier (React 19 + Vite)"]
        direction TB
        CP["Customer Portal<br/>(Bookings, Vehicles, RSA, Invoices)"]
        MP["Manager Console<br/>(Approvals, Job Cards, Assignment, Billing)"]
        MechP["Mechanic View<br/>(Work Queue, Part Requisitions, Progress)"]
        AP["Admin Hub<br/>(User Management, System Analytics)"]
        AxiosClient["Axios HTTP Client + Interceptors<br/>(JWT Token Bearer Injection)"]
        
        CP --> AxiosClient
        MP --> AxiosClient
        MechP --> AxiosClient
        AP --> AxiosClient
    end

    %% Security & Gateway Tier
    subgraph GatewayTier ["Security & Gateway Tier (Spring Security 6)"]
        direction TB
        CORS["CORS Filter & Config<br/>(Origin Whitelisting)"]
        JWTFilter["JwtAuthFilter (OncePerRequestFilter)<br/>Token Validation & Claims Extraction"]
        SecCtx["SecurityContextHolder<br/>(Authentication Token Set)"]
        
        CORS --> JWTFilter
        JWTFilter --> SecCtx
    end

    %% API Controller Tier
    subgraph ControllerTier ["REST Controller Tier"]
        direction TB
        AuthCtrl["AuthController<br/>/api/auth/*"]
        UserCtrl["UserController<br/>/api/users/*"]
        VehCtrl["VehicleController<br/>/api/vehicles/*"]
        ApptCtrl["AppointmentController<br/>/api/appointments/*"]
        JobCtrl["JobCardController<br/>/api/job-cards/*"]
        InvCtrl["InventoryController<br/>/api/inventory/*"]
        InvcCtrl["InvoiceController<br/>/api/invoices/*"]
        ExHandler["GlobalExceptionHandler<br/>(@RestControllerAdvice)"]
    end

    %% Business Service Tier
    subgraph ServiceTier ["Business Logic & Domain Services Tier (@Transactional)"]
        direction TB
        UserSvc["UserServiceImpl<br/>(BCrypt Auth, User Lifecycle)"]
        VehSvc["VehicleServiceImpl<br/>(Vehicle Registry & Soft Deletes)"]
        ApptSvc["AppointmentServiceImpl<br/>(Booking & RSA Coordinate Checks)"]
        JobSvc["JobCardServiceImpl<br/>(Work Workflow & Atomic Stock Allocation)"]
        InvSvc["InventoryServiceImpl<br/>(Catalog & Restocking)"]
        InvcSvc["InvoiceServiceImpl<br/>(Tax Engine & Razorpay Payment Signatures)"]
    end

    %% Persistence & Data Tier
    subgraph DataTier ["Persistence & Database Tier (Spring Data JPA / Hibernate)"]
        direction TB
        Repos["JPA Repositories<br/>(UserRepo, VehicleRepo, ApptRepo, JobCardRepo, InventoryRepo, InvoiceRepo)"]
        MySQL[("MySQL 8.x Relational Database<br/>(InnoDB Engine, Foreign Keys, Audit Timestamps)")]
        Repos --> MySQL
    end

    %% External Systems
    subgraph ExternalServices ["External Payment Gateway"]
        RazorpayAPI["Razorpay Payment Gateway API<br/>(Order Creation & Signature Hash Verification)"]
    end

    %% Connections
    AxiosClient -->|"HTTPS / JSON + Bearer JWT"| CORS
    SecCtx --> ControllerTier

    AuthCtrl --> UserSvc
    UserCtrl --> UserSvc
    VehCtrl --> VehSvc
    ApptCtrl --> ApptSvc
    JobCtrl --> JobSvc
    InvCtrl --> InvSvc
    InvcCtrl --> InvcSvc

    UserSvc --> Repos
    VehSvc --> Repos
    ApptSvc --> Repos
    JobSvc --> Repos
    InvSvc --> Repos
    InvcSvc --> Repos

    InvcSvc <-->|"Order Creation & Verify Request"| RazorpayAPI
```

---

## 2. Database Entity Relationship (ER) Diagram

This diagram displays the relational database schema, data types, constraints, primary keys (PK), foreign keys (FK), and cardinalities.

```mermaid
erDiagram
    USERS ||--o{ USERS : "supervises (1:N)"
    USERS ||--o{ VEHICLES : "owns (1:N)"
    USERS ||--o{ JOB_CARD : "manages as manager (1:N)"
    USERS ||--o{ JOB_CARD : "assigned as mechanic (1:N)"
    
    VEHICLES ||--o{ APPOINTMENTS : "services (1:N)"
    
    APPOINTMENTS ||--o| JOB_CARD : "initiates (1:1)"
    
    JOB_CARD ||--|{ JOB_CARD_ITEM : "contains (1:N)"
    JOB_CARD ||--o| INVOICE : "billed via (1:1)"
    
    INVENTORY ||--o{ JOB_CARD_ITEM : "supplies (1:N)"

    USERS {
        bigint user_id PK
        varchar user_name "NOT NULL"
        varchar email UK "NOT NULL"
        varchar password "NOT NULL (BCrypt)"
        varchar mobile
        double salary
        enum user_role "ADMIN | MANAGER | CUSTOMER | MECHANIC"
        boolean is_active "DEFAULT TRUE"
        bigint manager_id FK "REFERENCES users(user_id)"
        datetime created_on
        datetime updated_on
    }

    VEHICLES {
        bigint vehicle_id PK
        bigint customer_id FK "REFERENCES users(user_id) NOT NULL"
        varchar license_plate UK "NOT NULL"
        varchar brand
        varchar model
        varchar color
        boolean is_active "DEFAULT TRUE"
        datetime created_on
        datetime updated_on
    }

    APPOINTMENTS {
        bigint appointment_id PK
        bigint vehicle_id FK "REFERENCES vehicles(vehicle_id) NOT NULL"
        date request_date "NOT NULL"
        time scheduled_time
        text problem_description
        varchar customer_photo_url
        varchar rejection_reason
        boolean is_rsa "DEFAULT FALSE"
        varchar rsa_coordinates "LAT,LNG"
        enum status "PENDING | APPROVED | REJECTED | IN_PROGRESS | COMPLETED | CANCELLED"
        datetime created_on
        datetime updated_on
    }

    JOB_CARD {
        bigint job_card_id PK
        bigint appointment_id FK,UK "REFERENCES appointments(appointment_id)"
        bigint manager_id FK "REFERENCES users(user_id) NOT NULL"
        bigint mechanic_id FK "REFERENCES users(user_id)"
        datetime start_time
        datetime completion_time
        date estimated_completion_date
        text cancellation_reason
        enum job_card_status "CREATED | IN_PROGRESS | COMPLETED | CANCELLED"
        datetime created_on
        datetime updated_on
    }

    INVENTORY {
        bigint product_id PK
        varchar sku_code UK "NOT NULL"
        varchar item_name "NOT NULL"
        double current_price "NOT NULL"
        int stock_quantity "NOT NULL"
        int version "DEFAULT 0"
        boolean is_deleted "DEFAULT FALSE"
        datetime created_on
        datetime updated_on
    }

    JOB_CARD_ITEM {
        bigint item_id PK
        bigint job_card_id FK "REFERENCES job_card(job_card_id) NOT NULL"
        bigint product_id FK "REFERENCES inventory(product_id) NOT NULL"
        int quantity "NOT NULL"
        double snapshot_price "NOT NULL"
        varchar snapshot_item_name "NOT NULL"
        double total_price "NOT NULL"
        datetime created_on
        datetime updated_on
    }

    INVOICE {
        bigint invoice_id PK
        bigint job_card_id FK,UK "REFERENCES job_card(job_card_id) NOT NULL"
        varchar invoice_number UK "NOT NULL"
        double base_amount "NOT NULL"
        double tax_percentage "DEFAULT 18.0"
        double tax_amount "NOT NULL"
        double total_amount "NOT NULL"
        varchar razorpay_order_id
        varchar razorpay_payment_id
        varchar razorpay_signature
        enum payment_method "CASH | CREDIT_CARD | DEBIT_CARD | UPI | NET_BANKING"
        enum payment_status "PENDING | INITIATED | PAID | FAILED | REFUNDED"
        datetime paid_at
        datetime created_on
        datetime updated_on
    }
```

---

## 3. UML Class Diagram & Relationships

This diagram depicts the Object-Oriented structure of domain entities, inheritance from `BaseEntity`, service interfaces, and service implementations.

```mermaid
classDiagram
    %% Inheritance Hierarchy
    class BaseEntity {
        <<abstract>>
        -Long id
        -LocalDateTime createdOn
        -LocalDateTime lastUpdated
        +getId() Long
        +setId(Long id) void
        +getCreatedOn() LocalDateTime
        +getLastUpdated() LocalDateTime
    }

    class User {
        -String userName
        -String email
        -String password
        -Role userRole
        -String mobile
        -Double salary
        -boolean isActive
        -User manager
        +getUserName() String
        +getEmail() String
        +getPassword() String
        +getUserRole() Role
        +setUserRole(Role role) void
    }

    class Vehicle {
        -User customer
        -String licensePlate
        -String brand
        -String model
        -String color
        -boolean isActive
        +getCustomer() User
        +getLicensePlate() String
        +isActive() boolean
        +setActive(boolean active) void
    }

    class Appointment {
        -Vehicle vehicleDetails
        -LocalDate requestDate
        -LocalTime scheduledTime
        -String problemDescription
        -boolean rsa
        -String rsaCoordinates
        -Status status
        -String customerPhotoUrl
        -String rejectionReason
        +isRsa() boolean
        +getStatus() Status
        +setStatus(Status status) void
    }

    class JobCard {
        -Appointment appointment
        -User manager
        -User mechanic
        -LocalDateTime startTime
        -LocalDateTime completionTime
        -LocalDate estimatedCompletionDate
        -String cancellationReason
        -JobCardStatus jobCardStatus
        -List~JobCardItem~ items
        +addItem(JobCardItem item) void
        +getJobCardStatus() JobCardStatus
        +setJobCardStatus(JobCardStatus status) void
    }

    class JobCardItem {
        -JobCard jobCard
        -Inventory inventoryItem
        -int quantity
        -Double snapshotPrice
        -String snapshotItemName
        -Double totalPrice
        +getQuantity() int
        +getSnapshotPrice() Double
        +getTotalPrice() Double
    }

    class Inventory {
        -String itemName
        -String skuCode
        -Double currentPrice
        -Integer stockQuantity
        -boolean deleted
        -Integer version
        +getStockQuantity() Integer
        +setStockQuantity(Integer qty) void
        +isDeleted() boolean
    }

    class Invoice {
        -JobCard jobCard
        -String invoiceNumber
        -Double baseAmount
        -Double taxPercentage
        -Double taxAmount
        -Double totalAmount
        -PaymentStatus paymentStatus
        -String razorpayOrderId
        -String razorpayPaymentId
        -String razorpaySignature
        -PaymentMethod paymentMethod
        -LocalDateTime paidAt
        +getTotalAmount() Double
        +getPaymentStatus() PaymentStatus
        +setPaymentStatus(PaymentStatus status) void
    }

    BaseEntity <|-- User
    BaseEntity <|-- Vehicle
    BaseEntity <|-- Appointment
    BaseEntity <|-- JobCard
    BaseEntity <|-- JobCardItem
    BaseEntity <|-- Inventory
    BaseEntity <|-- Invoice

    %% Associations
    User "1" <-- "0..*" User : manager
    User "1" <-- "0..*" Vehicle : customer
    Vehicle "1" <-- "0..*" Appointment : vehicleDetails
    Appointment "1" <-- "0..1" JobCard : appointment
    User "1" <-- "0..*" JobCard : manager
    User "1" <-- "0..*" JobCard : mechanic
    JobCard "1" *-- "0..*" JobCardItem : items
    Inventory "1" <-- "0..*" JobCardItem : inventoryItem
    JobCard "1" <-- "0..1" Invoice : jobCard

    %% Service Contracts
    class JobCardService {
        <<interface>>
        +createJobCard(CreateJobCardDto dto) JobCardResponseDto
        +updateMechanic(Long id, AssignMechanicDto dto) JobCardResponseDto
        +startWork(Long id) JobCardResponseDto
        +completeWork(Long id) JobCardResponseDto
        +cancelJobCard(Long id, String reason) JobCardResponseDto
        +addItemToJobCard(Long id, AddItemToJobCardDto dto) JobCardResponseDto
    }

    class InvoiceService {
        <<interface>>
        +generateInvoice(Long jobCardId) InvoiceResponseDto
        +createPaymentDto(Long invoiceId) CreatePaymentOrderResponseDto
        +verifyPayment(Long invoiceId, VerifyPaymentRequestDto request) PaymentVerificationResponseDto
    }

    class JobCardServiceImpl {
        -JobCardRepository jobCardRepo
        -AppointmentRepository appointmentRepo
        -UserRepository userRepo
        -InventoryRepository inventoryRepo
        -JobCardItemRepository jobCardItemRepo
    }

    class InvoiceServiceImpl {
        -InvoiceRepository invoiceRepo
        -JobCardRepository jobCardRepo
        -RazorpayClient razorpayClient
        -calculateRazorpaySignature(String orderId, String paymentId) String
    }

    JobCardService <|.. JobCardServiceImpl : implements
    InvoiceService <|.. InvoiceServiceImpl : implements
```

---

## 4. Primary End-to-End Lifecycle Sequence Diagram

This sequence diagram depicts the complete business transaction flow: **Customer Registration $\rightarrow$ Booking $\rightarrow$ Manager Approval $\rightarrow$ Mechanic Repair with Stock Deduction $\rightarrow$ Invoice Generation $\rightarrow$ Razorpay Payment Verification**.

```mermaid
sequenceDiagram
    autonumber
    actor Cust as Customer
    actor Mgr as Manager
    actor Mech as Mechanic
    participant UI as React Frontend
    participant Gateway as Security Filter
    participant ApptCtrl as Appointment Module
    participant JobCtrl as JobCard Module
    participant InvCtrl as Invoice Module
    participant Razorpay as Razorpay API
    participant DB as MySQL Database

    %% Step 1: Appointment Booking
    Note over Cust,DB: Phase 1: Service Appointment Booking
    Cust->>UI: Submits Service Booking (Vehicle ID, Date, Issue)
    UI->>Gateway: POST /api/appointments (Bearer JWT)
    Gateway->>ApptCtrl: Authenticated Request
    ApptCtrl->>DB: INSERT INTO appointments (status='PENDING', is_rsa=false)
    DB-->>ApptCtrl: Created Appointment (ID: #101)
    ApptCtrl-->>UI: 201 Created

    %% Step 2: Manager Approval & Job Card Generation
    Note over Mgr,DB: Phase 2: Manager Approval & Job Card Dispatch
    Mgr->>UI: Approves Appointment & Assigns Mechanic
    UI->>Gateway: PATCH /api/appointments/101/status?status=APPROVED
    Gateway->>ApptCtrl: Set status = APPROVED
    ApptCtrl->>DB: UPDATE appointments SET status='APPROVED'
    
    Mgr->>UI: Creates Job Card (#JC-501)
    UI->>Gateway: POST /api/job-cards { appointmentId: 101, mechanicId: 20 }
    Gateway->>JobCtrl: Create Job Card
    JobCtrl->>DB: INSERT INTO job_card (status='CREATED', appointment_id=101)
    JobCtrl->>DB: UPDATE appointments SET status='IN_PROGRESS'
    JobCtrl-->>UI: 201 Created

    %% Step 3: Mechanic Service & Inventory Deduction
    Note over Mech,DB: Phase 3: Mechanic Service Execution & Part Usage
    Mech->>UI: Clicks "Start Work"
    UI->>Gateway: PATCH /api/job-cards/501/start
    Gateway->>JobCtrl: Set status = IN_PROGRESS
    JobCtrl->>DB: UPDATE job_card SET status='IN_PROGRESS', start_time=NOW()

    Mech->>UI: Adds Part (Brake Pads, Qty: 2)
    UI->>Gateway: POST /api/job-cards/501/items { inventoryItemId: 5, quantity: 2 }
    Gateway->>JobCtrl: Add Item to Job Card
    JobCtrl->>DB: SELECT stock_quantity FROM inventory WHERE id=5
    DB-->>JobCtrl: Available: 20
    JobCtrl->>DB: UPDATE inventory SET stock_quantity = 18 WHERE id=5
    JobCtrl->>DB: INSERT INTO job_card_items (job_card_id=501, qty=2, unit_price=1200)
    JobCtrl-->>UI: 200 OK (Item Added)

    Mech->>UI: Clicks "Complete Work"
    UI->>Gateway: PATCH /api/job-cards/501/complete
    Gateway->>JobCtrl: Complete Job Card
    JobCtrl->>DB: UPDATE job_card SET status='COMPLETED', completion_time=NOW()
    JobCtrl->>DB: UPDATE appointments SET status='COMPLETED'
    JobCtrl-->>UI: 200 OK

    %% Step 4: Invoice Generation & Razorpay Payment
    Note over Cust,Razorpay: Phase 4: Invoice Generation & Cryptographic Payment
    Mgr->>UI: Generates Invoice
    UI->>Gateway: POST /api/invoices/generate?jobCardId=501
    Gateway->>InvCtrl: Generate Invoice
    InvCtrl->>DB: Calculate Total (Items + 18% Tax)
    InvCtrl->>DB: INSERT INTO invoices (invoice_number='INV-2026-001', payment_status='PENDING')
    InvCtrl->>DB: UPDATE job_card SET job_card_status='BILLED'
    InvCtrl-->>UI: Invoice Response DTO

    Cust->>UI: Clicks "Pay Now"
    UI->>Gateway: POST /api/invoices/1/payment-order
    Gateway->>InvCtrl: Create Payment Order
    InvCtrl->>Razorpay: orders.create(amount=283200, currency="INR")
    Razorpay-->>InvCtrl: Order Response { id: "order_Kxyz999" }
    InvCtrl->>DB: UPDATE invoices SET razorpay_order_id='order_Kxyz999', payment_status='INITIATED'
    InvCtrl-->>UI: Return Order Details & Razorpay Key

    UI->>Razorpay: Open Razorpay Checkout Modal
    Cust->>Razorpay: Completes Payment Authorization
    Razorpay-->>UI: Return { order_id, payment_id, signature }

    UI->>Gateway: POST /api/invoices/1/verify-payment { orderId, paymentId, signature }
    Gateway->>InvCtrl: Verify HMAC Signature
    InvCtrl->>InvCtrl: Compute HMAC-SHA256(order_id + "|" + payment_id, secret)
    alt Signature Valid
        InvCtrl->>DB: UPDATE invoices SET payment_status='PAID', razorpay_payment_id='pay_123', paid_at=NOW()
        InvCtrl-->>UI: 200 OK { verified: true, message: "Payment successful" }
        UI-->>Cust: Displays Paid Receipt
    else Signature Invalid
        InvCtrl->>DB: UPDATE invoices SET payment_status='FAILED'
        InvCtrl-->>UI: 400 Bad Request { verified: false }
    end
```

---

## 5. State Machine Diagrams

### 5.1 Appointment State Lifecycle
```mermaid
stateDiagram-v2
    [*] --> PENDING : Customer books appointment / RSA
    PENDING --> APPROVED : Manager reviews and approves
    PENDING --> CANCELLED : Customer or Manager cancels
    APPROVED --> IN_PROGRESS : Job Card created
    APPROVED --> CANCELLED : Manager cancels
    IN_PROGRESS --> COMPLETED : Mechanic finishes work
    IN_PROGRESS --> CANCELLED : Job Card cancelled with reason
    COMPLETED --> [*]
    CANCELLED --> [*]
```

### 5.2 Job Card State Lifecycle
```mermaid
stateDiagram-v2
    [*] --> CREATED : Generated from APPROVED appointment
    CREATED --> IN_PROGRESS : Mechanic starts work
    CREATED --> CANCELLED : Manager cancels (Restores stock)
    IN_PROGRESS --> COMPLETED : Work done & parts added
    IN_PROGRESS --> CANCELLED : Manager cancels (Restores stock)
    COMPLETED --> BILLED : Invoice generated
    BILLED --> [*]
    CANCELLED --> [*]
```

### 5.3 Invoice Payment State Lifecycle
```mermaid
stateDiagram-v2
    [*] --> PENDING : Invoice generated for Job Card
    PENDING --> INITIATED : Customer clicks "Pay Now" (Order created)
    INITIATED --> PAID : Razorpay HMAC-SHA256 signature verified
    INITIATED --> FAILED : Payment rejected or signature mismatch
    FAILED --> INITIATED : Customer retries payment
    PAID --> [*]
```

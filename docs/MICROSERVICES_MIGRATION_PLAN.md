# Microservices Migration Plan (Spring Cloud, Eureka, Feign, Gateway, Admin Server)

This document outlines the system architecture, database design (ER diagram), API sequence flows, module structures, and implementation steps required to split the **Inventory Service** from the SmartServ backend and transition to a distributed microservices model.

---

## 1. System Architecture Diagram

```mermaid
graph TD
    Client[Client Browser / Mobile] -->|1. REST Calls: Port 8080| Gateway[API Gateway - Port 8080]
    
    %% Gateway routes
    Gateway -->|2a. Route: /api/inventory/**| InventoryService[Inventory Service - Port 8082]
    Gateway -->|2b. Route: /api/auth/**, /api/appointments/**, /api/jobcard/**| CoreService[Core Backend Service - Port 8081]
    
    %% Inter-service calls
    CoreService -->|3. Feign Client Calls| InventoryService
    
    %% Registration
    Eureka[Eureka Service Registry - Port 8761] <-->|Register & Heartbeat| Gateway
    Eureka <-->|Register & Heartbeat| CoreService
    Eureka <-->|Register & Heartbeat| InventoryService
    Eureka <-->|Register & Heartbeat| AdminServer[Admin Server - Port 8083]
    
    %% Monitoring
    AdminServer -->|Poll Actuator Metrics| Gateway
    AdminServer -->|Poll Actuator Metrics| CoreService
    AdminServer -->|Poll Actuator Metrics| InventoryService
    
    %% Databases
    CoreService -->|Read/Write| DBCore[(MySQL DB: smartserv)]
    InventoryService -->|Read/Write| DBInventory[(MySQL DB: smartserv_inventory)]
```

---

## 2. Entity-Relationship (ER) Diagram & Database Separation

### Monolithic Database Design (Before)
Direct physical association and foreign key link.

```mermaid
erDiagram
    INVENTORY {
        Long product_id PK
        String item_name
        String sku_code
        Double current_price
        Integer stock_quantity
        Boolean is_deleted
        Integer version
    }
    JOBCARD_ITEM {
        Long item_id PK
        Long job_card_id FK
        Long product_id FK
        Integer quantity
        Double snapshot_price
        String snapshot_item_name
        Double total_price
    }
    JOBCARD_ITEM }o--|| INVENTORY : "foreign key link (ManyToOne)"
```

### Decoupled Microservice Databases (After)

To decouple the services, we split the schema into two distinct databases. In the Core Database, `JOBCARD_ITEM` references the inventory product ID as a standard long column (`product_id`) without physical foreign keys or Jpa joins.

#### Database 1: Core Service Database (`smartserv`)
```mermaid
erDiagram
    USER ||--o{ VEHICLE : owns
    USER ||--o{ JOBCARD : manages
    USER ||--o{ JOBCARD : works_on
    VEHICLE ||--o{ APPOINTMENT : scheduled_for
    APPOINTMENT ||--|| JOBCARD : creates
    JOBCARD ||--o{ JOBCARD_ITEM : contains
    JOBCARD ||--o| INVOICE : bills
    
    USER {
        Long id PK
        String username
        String email
        String role
    }
    VEHICLE {
        Long id PK
        String license_plate
        Long customer_id FK
    }
    APPOINTMENT {
        Long id PK
        Long vehicle_id FK
    }
    JOBCARD {
        Long id PK
        Long appointment_id FK
        Long manager_id FK
        Long mechanic_id FK
        String status
    }
    JOBCARD_ITEM {
        Long item_id PK
        Long job_card_id FK
        Long product_id "logical reference only (no FK)"
        Integer quantity
        Double snapshot_price
        String snapshot_item_name
        Double total_price
    }
    INVOICE {
        Long id PK
        Long job_card_id FK
    }
```

#### Database 2: Inventory Service Database (`smartserv_inventory`)
```mermaid
erDiagram
    INVENTORY {
        Long product_id PK
        String item_name
        String sku_code
        Double current_price
        Integer stock_quantity
        Boolean is_deleted
        Integer version
    }
```

---

## 3. Sequence Flow Diagram: Adding Item to Job Card

This sequence diagram depicts how stock validation, lookup, and deduction occur across the service boundary.

```mermaid
sequenceDiagram
    autonumber
    actor User as Client Application
    participant GW as API Gateway (8080)
    participant Core as Core Service (8081)
    participant Inv as Inventory Service (8082)
    
    User->>GW: POST /api/jobcard/{id}/items (JSON Body with inventoryItemId & quantity)
    Note over User,GW: JWT Cookie or Authorization Header attached
    GW->>Core: Route request to /api/jobcard/{id}/items
    
    activate Core
    Core->>Core: Fetch JobCard from database & validate status
    
    Note over Core,Inv: Feign call invokes inter-service lookup
    Core->>Inv: GET /api/inventory/{inventoryItemId}
    activate Inv
    Inv->>Inv: Retrieve Inventory from DB
    Inv-->>Core: Return InventoryResponseDto (itemName, stockQuantity, currentPrice, isDeleted)
    deactivate Inv
    
    Core->>Core: Validate product exists, is not deleted, and stock >= quantity
    
    Note over Core,Inv: Feign call deducts stock transactionally
    Core->>Inv: PUT /api/inventory/{inventoryItemId}/deduct-stock?quantity={quantity}
    activate Inv
    Note over Inv: Optimistic locking (version check) updates stock quantity in DB
    Inv-->>Core: HTTP 200 OK (Success)
    deactivate Inv
    
    Core->>Core: Create JobCardItem with Snapshot properties & associate with JobCard
    Core->>Core: Save JobCard to Core Database
    
    Core-->>GW: Return JobCardResponseDto
    deactivate Core
    GW-->>User: Return HTTP 200 OK
```

---

## 4. User Review Required

> [!IMPORTANT]
> **Database Decoupling & Foreign Keys**
> Separating the Inventory Service requires removing direct database relationships (like Hibernate `@ManyToOne` foreign key mappings) between `JobCardItem` (in the Core database) and `Inventory` (in the Inventory database).
> - We will replace `Inventory inventoryItem` in `JobCardItem` with `Long inventoryItemId`.
> - Queries joining `JobCardItem` and `Inventory` will be replaced by API composition, utilizing a Feign Client to query the Inventory Service by ID or list of IDs.
> - A data migration is required to split the tables and preserve references.

> [!WARNING]
> **Inter-service Security Propagation**
> Since all APIs are secured by JWT, inter-service Feign calls from the Core Service to the Inventory Service must pass along the user's JWT token or cookie. We will implement a `RequestInterceptor` in the Core Service to automatically forward the client's Authorization header and/or cookies.
```

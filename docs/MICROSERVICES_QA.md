# SmartServ Microservices Architecture - Q&A Guide

This guide explains our new microservices architecture in simple, easy-to-understand English.

---

### Q1: What is a "Monolith" and what is a "Microservice"?
* **Monolith (Before)**: Imagine a giant, single building where the reception, inventory room, appointment desk, and billing counter are all in the same room. If the inventory room gets crowded, the whole building slows down. If the inventory room catches fire, the entire building shuts down.
* **Microservices (Now)**: Imagine splitting that single building into separate, specialized shops on the same street. One shop is only for Inventory, another is only for Appointents/Billing, and another is the Reception desk at the street entrance. If the Inventory shop is busy, it does not affect the Appointments shop. If the Inventory shop has a problem, the Appointments shop can still take orders.

---

### Q2: What are the 5 parts (services) in our new system?
We split our system into 5 separate applications (sub-modules):

1. **Discovery Server (Eureka)** runs on Port `8761`. It acts as a phone book directory.
2. **API Gateway** runs on Port `8080`. It acts as the front receptionist/gatekeeper.
3. **Admin Server** runs on Port `8083`. It acts as the security guard monitoring health logs.
4. **Core Service** runs on Port `8081`. It handles appointments, users, vehicles, job cards, and payments.
5. **Inventory Service** runs on Port `8082`. It handles items, stock quantities, and prices.

---

### Q3: Why do we need the Eureka Discovery Server (Port 8761)?
In a microservices system, services boot up on different computers or ports. They need to find each other.
* **How it works**: When the Inventory Service starts, it calls Eureka and says: *"Hello, I am the Inventory Service, and you can find me at localhost:8082."* 
* When the Core Service wants to check stock, it asks Eureka: *"Where is the Inventory Service?"* Eureka looks at its phone book and returns the address.
* This means we do not need to hardcode IP addresses or ports in our code.

---

### Q4: What is the API Gateway (Port 8080) and why is it the only entry point?
The client (React Frontend) should not have to remember 5 different addresses. It only talks to one receptionist: the **API Gateway** on Port `8080`.
* **How it works**:
  * If a request is for `/api/inventory/items`, the Gateway forwards it to the **Inventory Service** (8082).
  * If a request is for `/api/jobcard` or `/api/auth`, the Gateway forwards it to the **Core Service** (8081).
* The Gateway also handles **CORS configurations** so that the React frontend on Port `5173` is allowed to make requests without browser security blocking it.

---

### Q5: How do the Core Service and Inventory Service talk to each other?
They talk using **OpenFeign**. OpenFeign is a tool that allows one Java Spring Boot application to call another application's REST APIs like it was a local function.
* **Example**: In our Core Service, we write an interface called [InventoryClient](../core-service/src/main/java/com/smartserv/client/InventoryClient.java). 
* When a user adds an item to a Job Card, the Core Service calls `inventoryClient.deductStock(itemId, quantity)`. OpenFeign automatically translates this call into an HTTP `PUT` request to `http://inventory-service/api/inventory/{id}/deduct-stock` behind the scenes.

---

### Q6: How does Security work now that we have split the services?
Our system uses **JWT (JSON Web Tokens)** for security.
1. The user logs in at `/api/auth/login`. The **Core Service** validates the password and returns a JWT token.
2. For any subsequent request, the browser sends this JWT token (in a cookie or an Authorization header).
3. The **API Gateway** forwards the request (with the JWT token) to the destination service.
4. Both the **Core Service** and **Inventory Service** have a security filter ([JwtAuthFilter](../inventory-service/src/main/java/com/smartserv/inventory/security/JwtAuthFilter.java)) that checks if the token is valid before letting the request proceed.

> [!WARNING]
> **Inter-Service Security**: When the Core Service calls the Inventory Service via Feign, the request is a new call. We implemented a **Feign Interceptor** ([FeignClientInterceptor](../core-service/src/main/java/com/smartserv/config/FeignClientInterceptor.java)) that copies the user's JWT token from the original browser request and pastes it onto the outgoing Feign request. This makes sure the Inventory Service knows who is requesting the stock change.

---

### Q7: If we deleted the database foreign keys, how do we link Job Cards and Inventory?
In the monolith database, a row in the `job_card_item` table was physically tied to a row in the `inventory` table using a Foreign Key database constraint. 
In microservices, databases are split into separate SQL instances (`smartserv` and `smartserv_inventory`). They cannot physically talk to each other to check constraints.
* **Our Solution**: We use **Logical References**.
* In the `job_card_item` table, we store the ID of the product as a regular number (`product_id`) without a database-level link constraint.
* When saving a new job card item, we query the Inventory Service over the network using Feign to make sure that the ID exists, and then we save that ID number.
* When displaying the job card, we read the saved snapshot details (`snapshot_item_name`, `snapshot_price`) directly from the `job_card_item` table without needing to call the Inventory Service.

---

### Q8: What is the "Snapshot" mechanism and why is it helpful?
When an inventory item is added to a job card, we copy its name and price at that exact second and save them inside the `job_card_item` record as `snapshot_item_name` and `snapshot_price`.
* **Why?** 
  1. If a mechanic changes the price of an oil filter in the Inventory panel tomorrow, past customers should still see the price they were billed.
  2. Because the name and price are saved directly in the core database's `job_card_item` table, when a manager views a list of Job Cards, the Core Service doesn't need to ask the Inventory Service for item details. It reads them locally, making the system super fast!

---

### Q9: How do we prevent two users from updating the same stock at the same time?
If two mechanics try to check out the last spark plug at the exact same millisecond, they might both see that `stock = 1` and buy it, resulting in a negative stock.
* **Our Solution**: We use **Optimistic Locking** (`@Version` field in the database).
* Every time a row is updated in the `inventory` table, Hibernate increments its version number (e.g., from `1` to `2`).
* If Mechanic A reads version `1` and tries to write, the database updates it to version `2`.
* If Mechanic B tries to update the same row at the same time using version `1`, the database sees that the version is already `2` and throws an error (`OptimisticLockException`). The system then blocks the second purchase and shows an error: *"Stock was modified by another user. Please refresh."*
* **Learn More**: For a detailed, step-by-step walkthrough of this race condition, see [Optimistic Locking & @Version Annotation Explained](OPTIMISTIC_LOCKING_EXPLAINED.md).

---

### Q10: What is the Spring Boot Admin Server (Port 8083)?
It is a dashboard app. All our microservices send their vitals (CPU usage, memory usage, health status, and logs) to the Admin Server.
* You can open `http://localhost:8083` in your browser to see a beautiful graphical interface showing which services are healthy and running, without digging through terminal logs.

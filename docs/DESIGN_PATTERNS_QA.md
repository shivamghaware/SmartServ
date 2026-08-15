# Design Patterns Interview Q&A Guide (SmartServ)

This document contains a list of interview questions and answers about the design patterns used in **SmartServ**. The answers are written in **simple, clear English** with code references, key definitions, and mental models to make them easy to learn and memorize.

---

## 1. Easy Questions (Fundamentals & Basics)

### Q1: What is the Layered (N-Tier) Architecture, and how is it used in this project?
*   **Simple Answer**: It is a pattern that divides the application code into separate "folders" or layers. Each layer has one job. Information flows in one direction: from the user down to the database.
*   **How SmartServ uses it**: 
    1.  [UserController](../core-service/src/main/java/com/smartserv/controller/UserController.java) (Controller Layer) receives HTTP requests.
    2.  [InvoiceServiceImpl](../core-service/src/main/java/com/smartserv/service/InvoiceServiceImpl.java) (Service Layer) handles calculations and business rules.
    3.  [JobCardRepository](../core-service/src/main/java/com/smartserv/repository/JobCardRepository.java) (Repository Layer) fetches data from the database.
*   **Analogy**: Like a restaurant. The waiter (Controller) takes your order, the kitchen chef (Service) cooks the food, and the pantry assistant (Repository) gets the ingredients from the fridge (Database).
*   **Why use it?**: If you want to change the database, you only change the Repository layer. The rest of the code stays safe.

---

### Q2: What is a Singleton Pattern, and how does Spring Boot use it by default?
*   **Simple Answer**: It means having **only one copy (instance)** of a class in memory for the whole application.
*   **How SmartServ uses it**: In Spring Boot, all classes marked with `@RestController`, `@Service`, or `@Repository` are Singletons by default. For example, there is only one instance of `InvoiceServiceImpl` running.
*   **Analogy**: Like a single wall clock in an office. Everyone looks at the same clock to see the time. We do not buy a clock for every single person.
*   **Why use it?**: It saves computer memory. We do not need to keep creating new objects.

---

### Q3: What is the Builder Pattern, and why is it useful for DTOs?
*   **Simple Answer**: It is a pattern that helps create complex objects step-by-step using a clean, readable chain of method calls instead of using constructors with many parameters.
*   **How SmartServ uses it**: We use Lombok's `@Builder` annotation on DTOs like [InvoiceResponseDto](../core-service/src/main/java/com/smartserv/dto/invoice/InvoiceResponseDto.java). It allows us to write:
    ```java
    CreatePaymentOrderResponseDto.builder()
            .orderId(orderId)
            .amount(amount)
            .build();
    ```
*   **Analogy**: Like ordering a custom pizza. You start with the base, then call `.addCheese()`, `.addTomato()`, and finally `.bake()`.
*   **Why use it?**: It prevents mistakes. Standard constructors with 10 parameters are hard to read and easy to break if you put arguments in the wrong order.

---

## 2. Medium Questions (Structural & Intermediate Concepts)

### Q4: What is Dependency Injection (DI) and how is it done in SmartServ?
*   **Simple Answer**: Classes do not create their own helper objects (dependencies). Instead, the framework (Spring) creates them and passes (injects) them into the class constructor.
*   **How SmartServ uses it**: We use Lombok's `@RequiredArgsConstructor` to generate a constructor for our `final` helper fields. Look at [InvoiceServiceImpl](../core-service/src/main/java/com/smartserv/service/InvoiceServiceImpl.java#L46-L53):
    ```java
    @Service
    @RequiredArgsConstructor
    public class InvoiceServiceImpl implements InvoiceService {
        private final InvoiceRepository invoiceRepo; // Spring automatically finds and injects this
    }
    ```
*   **Analogy**: If a mechanic needs a wrench, they do not build a wrench factory inside the garage. The garage owner (Spring) hands them the wrench when they start their shift.
*   **Why use it?**: It makes unit testing easy. You can pass mock/fake repositories to test the service business logic.

---

### Q5: How does the Proxy Pattern make `@Transactional` work in Spring Boot?
*   **Simple Answer**: A Proxy is a "wrapper" or "middleman" object. Spring wraps our service class inside a dynamic Proxy class.
*   **How SmartServ uses it**: In [InvoiceServiceImpl](../core-service/src/main/java/com/smartserv/service/InvoiceServiceImpl.java#L45-L49), we have `@Transactional` at class level.
*   **What happens under the hood**:
    1.  The client calls `generateInvoice()`.
    2.  The call hits the **Spring Proxy** first.
    3.  The Proxy starts a database transaction (`BEGIN TRANSACTION`).
    4.  The Proxy calls our actual `generateInvoice()` code.
    5.  If our code finishes successfully, the Proxy commits the changes (`COMMIT`). If an error is thrown, it rolls back (`ROLLBACK`).
*   **Analogy**: Like a security guard at a bank door. The guard opens the door (starts transaction), lets you do your work, and locks the door after you leave (commits transaction).

---

### Q6: What is the Interceptor / Filter Pattern, and where is it used for Security?
*   **Simple Answer**: It is a pattern that intercepts incoming HTTP requests *before* they reach the controllers to perform checkups (like checking security tags).
*   **How SmartServ uses it**:
    *   **Backend**: [JwtAuthFilter](../core-service/src/main/java/com/smartserv/security/JwtAuthFilter.java) intercepts every request. It extracts the JWT cookie/token, checks if it is valid, and logs the user details into Spring Security.
    *   **Frontend**: [axiosConfig.js](../SmartServFrontEnd/src/api/axiosConfig.js#L23-L39) uses response interceptors to watch for `401 Unauthorized` errors and automatically logout the user.
*   **Analogy**: Like a security check at the airport. You cannot walk onto the airplane (Controller) until the guard inspects your ticket and ID (Filter).

---

### Q7: What is the Adapter Pattern, and how is it used with DTOs and Frontend normalizing?
*   **Simple Answer**: It converts data from one format into another format that the client expects.
*   **How SmartServ uses it**:
    *   On the backend, `ModelMapper` converts database `@Entity` objects to DTOs before sending them to the API.
    *   On the frontend, in [invoiceService.js](../SmartServFrontEnd/src/services/invoiceService.js#L3-L16), `normalizeInvoice` adapts raw JSON data:
        ```javascript
        const normalizeInvoice = (inv) => {
          return {
            ...inv,
            totalAmount: inv.totalAmount || (Number(inv.baseAmount) + Number(inv.taxAmount))
          };
        };
        ```
*   **Analogy**: Like a travel plug adapter. If you travel to another country, you need an adapter to plug your phone charger into the wall outlet.

---

## 3. Advanced Questions (Architectural & Performance Patterns)

### Q8: What is the Repository Pattern, and how does JPA solve the N+1 Select query problem?
*   **Simple Answer**: The Repository pattern encapsulates database queries into simple method interfaces. The N+1 select query problem happens when Hibernate runs one query to load parent records, and then runs N extra queries to fetch children for each parent.
*   **How SmartServ solves it**:
    1.  **Fetch Joins**: Inside [JobCardRepository](../core-service/src/main/java/com/smartserv/repository/JobCardRepository.java#L18-L25), we write custom `LEFT JOIN FETCH` queries to load parents and children in a single database roundtrip.
    2.  **Entity Graphs**: In [JobCard.java](../core-service/src/main/java/com/smartserv/entity/JobCard.java#L28-L50), we define `@NamedEntityGraph` listing what relationships must be fetched together.
*   **Why use it?**: Without this, if you load 100 job cards, Hibernate might hit the database 501 times, making the website extremely slow.

---

### Q9: How is the Observer (Publish-Subscribe) Pattern implemented between Axios and React Context?
*   **Simple Answer**: It is a pattern where one part of the system broadcasts an event, and other parts listen and react to it without knowing about each other.
*   **How SmartServ uses it**:
    1.  If the Axios interceptor in [axiosConfig.js](../SmartServFrontEnd/src/api/axiosConfig.js#L31) detects a `401 Unauthorized` error, it publishes an event:
        ```javascript
        window.dispatchEvent(new Event('auth:unauthorized'));
        ```
    2.  The [AuthContext.jsx](../SmartServFrontEnd/src/context/AuthContext.jsx#L47-L52) component subscribes to this event:
        ```javascript
        window.addEventListener('auth:unauthorized', handleUnauthorized);
        ```
*   **Why use it?**: It cleanly separates the Axios HTTP client logic from the React UI components. Axios does not need to import React hooks directly to trigger a logout.

---

### Q10: What is the React Provider Pattern, and how does it prevent "Prop Drilling"?
*   **Simple Answer**: The Provider pattern shares state globally from the top of the React component tree down to any nested component underneath, without passing props manually through middle components (which is called "Prop Drilling").
*   **How SmartServ uses it**: We use [AuthContext.jsx](../SmartServFrontEnd/src/context/AuthContext.jsx#L108-L113) to wrap our application components:
    ```javascript
    return (
      <AuthContext.Provider value={value}>
        {children}
      </AuthContext.Provider>
    );
    ```
    Any component under it can use the custom hook `useAuth()` to get the logged-in user data.
*   **Analogy**: Like radio broadcasting. The radio station (Provider) broadcasts music. Anyone with a radio receiver (hook) can listen to it. You do not need to string a wire from the station to every house.

---

### Q11: What is the Strategy Pattern, and how is it used in Spring Security configurations?
*   **Simple Answer**: It defines a set of algorithms or rules as interfaces. The application can swap them at runtime without modifying the class using them.
*   **How SmartServ uses it**: Spring Security's password encoding relies on the `PasswordEncoder` strategy interface. In [SecurityConfig.java](../core-service/src/main/java/com/smartserv/config/SecurityConfig.java#L53-L56), we choose the `BCryptPasswordEncoder` strategy:
    ```java
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    ```
*   **Why use it?**: If tomorrow security regulations require swapping BCrypt to a different algorithm (like Argon2), we only update this single `@Bean` definition. The rest of the authentication code remains completely unchanged.

---

## Additional References

For the full detailed breakdown of each design pattern's implementation, class relations, and diagrams, see the main documentation:
*   [SmartServ Design Patterns & Architecture Reference](DESIGN_PATTERNS.md)


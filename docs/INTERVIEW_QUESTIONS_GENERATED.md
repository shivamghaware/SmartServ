# Technical Interview Question Bank
**Candidate Assessment Guide for Enterprise Full-Stack & Health-Informatics Roles**

This document serves as a structured, comprehensive interview question bank designed to evaluate candidates against the specified **Essential** and **Desirable** skill requirements. Each section is categorized by technical domain and contains target competencies, open-ended questions, ideal answers, and red flags to watch for during the evaluation.

---

## Table of Contents
1. [Part A: Essential Skills Evaluation](#part-a-essential-skills-evaluation)
   - [1. Java Programming & Data Structures](#1-java-programming--data-structures)
   - [2. Web Technologies (Spring Boot, J2EE, Web Services, Struts)](#2-web-technologies-spring-boot-j2ee-web-services-struts)
   - [3. Relational & Non-Relational Databases](#3-relational--non-relational-databases)
   - [4. UI/UX Technologies (HTML, CSS, JS, Ajax, Node.js)](#4-uiux-technologies-html-css-js-ajax-nodejs)
   - [5. Testing Tools & Methodologies](#5-testing-tools--methodologies)
   - [6. SDLC / PDLC Lifecycle Management](#6-sdlc--pdlc-lifecycle-management)
   - [7. Problem-Solving, Troubleshooting & Collaboration](#7-problem-solving-troubleshooting--collaboration)
2. [Part B: Desirable Skills Evaluation](#part-b-desirable-skills-evaluation)
   - [1. Artificial Intelligence Integration](#1-artificial-intelligence-integration)
   - [2. Device Interfacing & Real-Time Data Ingestion](#2-device-interfacing--real-time-data-ingestion)
   - [3. Image Processing & Heavy Asset Handling](#3-image-processing--heavy-asset-handling)
   - [4. Mobile Application Development (Android / iOS)](#4-mobile-application-development-android--ios)
   - [5. Health Informatics Systems & Standards (DICOM, HL7, FHIR, SNOMED-CT)](#5-health-informatics-systems--standards-dicom-hl7-fhir-snomed-ct)
   - [6. DevOps, Containerization & Automation](#6-devops-containerization--automation)
3. [Candidate Evaluation & Grading Rubric](#candidate-evaluation--grading-rubric)

---

## Part A: Essential Skills Evaluation

### 1. Java Programming & Data Structures

#### **Q1.1: HashMap Internal Mechanics & Hash Collision Resolution**
*   **Question:** Can you explain how a `HashMap` works internally in Java? What happens when two different keys generate the same hash code, and how has this behavior evolved since Java 8?
*   **Target Competency:** Deep understanding of Java memory management, complexity analysis ($O(1)$ vs $O(N)$ vs $O(\log N)$), and standard libraries.
*   **Expected Answer:**
    *   `HashMap` works on the principle of hashing, using an array of Node/Entry buckets.
    *   When a collision occurs (two keys have the same hash code but are not equal), they are stored in a linked list at that bucket index.
    *   In **Java 8+**, if the number of elements in a bucket exceeds a threshold (called `TREEIFY_THRESHOLD` which is 8) and the overall map capacity is at least 64, the linked list is converted into a **Balanced Red-Black Tree**. This improves the worst-case lookup performance from $O(N)$ to $O(\log N)$.
    *   If size decreases under a threshold (called `UNTREEIFY_THRESHOLD` which is 6) during removals, it converts back to a linked list.
*   **Red Flags:** Candidate does not know about Red-Black Trees/Java 8 changes; confuses hash code with `equals()` method; fails to mention $O(\log N)$ complexity for tree structures.

#### **Q1.2: Concurrency & Multithreading (Runnable vs. Callable vs. CompletableFuture)**
*   **Question:** What are the differences between `Runnable`, `Callable`, and `CompletableFuture` in Java? In what scenarios would you use `CompletableFuture` over standard `Future`?
*   **Target Competency:** Concurrent programming, asynchronous execution, and non-blocking architecture.
*   **Expected Answer:**
    *   `Runnable` defines a task that runs asynchronously but cannot return a result and cannot throw checked exceptions.
    *   `Callable` returns a generic result `V` and can throw checked exceptions, but fetching the result via `Future.get()` is a blocking operation.
    *   `CompletableFuture` (introduced in Java 8) implements both `Future` and `CompletionStage`. It allows for non-blocking asynchronous pipeline chaining (using `thenApply`, `thenAccept`, `thenCompose`), exception handling, combining multiple futures (`allOf`, `anyOf`), and manual completion.
*   **Red Flags:** Unable to explain why `Future.get()` is problematic in high-throughput applications; lacks awareness of execution threads (`ForkJoinPool.commonPool()`).

---

### 2. Web Technologies (Spring Boot, J2EE, Web Services, Struts)

#### **Q2.1: Request Lifecycle - Filters vs. Interceptors vs. ControllerAdvice**
*   **Question:** In a Spring Boot application, if you need to perform actions like logging, validation, authentication, and exception mapping, how do you decide whether to use a Servlet `Filter`, a Spring `HandlerInterceptor`, or a `@ControllerAdvice`?
*   **Target Competency:** Web architecture, framework design patterns, and cross-cutting concerns.
*   **Expected Answer:**
    *   **Servlet Filter:** Part of the Servlet container (outside Spring Context). Best for low-level request/response manipulation, CORS, gzip compression, and global security/JWT parsing before hitting Spring controllers.
    *   **HandlerInterceptor:** Part of the Spring MVC framework. Accesses the handler object and `ModelAndView`. Best for application-specific logic like request timing, authorization checks, or theme/locale changes.
    *   **@ControllerAdvice:** A specialization of `@Component` that allows declaring global `@ExceptionHandler` and `@ModelAttribute` interceptors. Best for mapping exceptions to clean API response objects (DTOs) with correct HTTP status codes.
*   **Red Flags:** Confusing Filters with Interceptors; not knowing that Filters run before Spring MVC picks up the request; lack of familiarity with global exception handling patterns.

#### **Q2.2: Legacy Migration (J2EE/Struts to Spring Boot)**
*   **Question:** Imagine we have a legacy web application built using Struts 2 and J2EE (EJB, Servlets, JSP). What are the main architectural differences you must consider when migrating it to a modern Spring Boot REST API? How do you map Struts components to Spring MVC?
*   **Target Competency:** System modernization, framework mapping, dependency injection, and REST design.
*   **Expected Answer:**
    *   **State Management:** Struts Action classes are often stateful (storing data in ActionForms). Spring MVC Controllers are singleton, stateless beans by default.
    *   **Mapping Components:**
        *   Struts `Action` classes $\rightarrow$ Spring Boot `@RestController` / `@Controller`.
        *   `ActionForm` variables $\rightarrow$ Clean Request/Response DTOs validated with `@Valid` annotations.
        *   `struts.xml` mappings $\rightarrow$ Class and method level `@RequestMapping` / `@GetMapping` annotations.
    *   **Business Logic:** EJB (Enterprise JavaBeans) services should be refactored into Spring `@Service` beans, utilizing Spring's declarative `@Transactional` management instead of JTA.
    *   **View Layer:** Replace JSP with a React/Vite/HTML single-page application communicating via REST APIs.
*   **Red Flags:** Lacks understanding of statelessness in REST APIs; cannot map Action/ActionForm to Controllers/DTOs; doesn't mention how to handle validation or transactions.

---

### 3. Relational & Non-Relational Databases

#### **Q3.1: Transaction Isolation Levels & Anomalies**
*   **Question:** What are the four standard ANSI SQL transaction isolation levels? Explain the database anomalies (e.g., Dirty Reads, Non-repeatable Reads, Phantom Reads) that each level prevents.
*   **Target Competency:** Database transactions, consistency modeling, and concurrency control.
*   **Expected Answer:**
    1.  **Read Uncommitted:** Prevents nothing. Allows **Dirty Reads** (reading uncommitted data from another transaction).
    2.  **Read Committed:** Prevents Dirty Reads. Allows **Non-repeatable Reads** (rereading the same row yields different values because another transaction modified and committed it).
    3.  **Repeatable Read:** Prevents Dirty and Non-repeatable Reads. Allows **Phantom Reads** (queries retrieve new rows inserted by another concurrent transaction).
    4.  **Serializable:** Prevents all anomalies. Executes transactions as if they were running sequentially, usually by acquiring range locks.
*   **Red Flags:** Cannot name the four levels; confuses non-repeatable reads with phantom reads; is unaware of the performance penalties associated with higher isolation levels like Serializable.

#### **Q3.2: SQL vs. NoSQL Modeling for Enterprise Applications**
*   **Question:** For a service tracking automobile repair job cards and customer information, when would you choose a Relational Database (like MySQL/Postgres) over a Document Database (like MongoDB)? How does the CAP theorem guide this decision?
*   **Target Competency:** Data modeling, storage trade-offs, and distributed systems.
*   **Expected Answer:**
    *   **Choose Relational (SQL):** When transactional integrity (ACID), complex multi-table joins, and strict data consistency are critical (e.g., billing, invoicing, inventory deductions).
    *   **Choose Document (NoSQL/MongoDB):** When schema flexibility is required (e.g., storing varied device telemetry or unstructured notes), rapid horizontal scaling is needed, and deep nesting of dynamic attributes is common.
    *   **CAP Theorem:** States a distributed system can guarantee at most two out of three: Consistency, Availability, Partition Tolerance. RDBMS prioritize **Consistency (C)** over availability during partition events, whereas many NoSQL systems allow configuration for **Availability and Partition Tolerance (AP)** with eventual consistency.
*   **Red Flags:** Believes NoSQL is always faster or SQL is outdated; cannot explain ACID vs BASE; is unfamiliar with join performance differences.

---

### 4. UI/UX Technologies (HTML, CSS, JS, Ajax, Node.js)

#### **Q4.1: JavaScript Event Loop & Node.js Concurrency**
*   **Question:** JavaScript is single-threaded, yet Node.js is widely used for high-concurrency, I/O-intensive network applications. How does the JavaScript Event Loop handle asynchronous operations, and what is the difference between Microtasks and Macrotasks?
*   **Target Competency:** Client/Server engine execution, non-blocking asynchronous behaviors.
*   **Expected Answer:**
    *   Node.js uses a single-threaded event loop but offloads blockable I/O tasks to the OS kernel or thread pools (like Libuv) to execute concurrently.
    *   When asynchronous operations complete, callback tasks are placed into task queues.
    *   **Microtasks** (e.g., `Promise.then`, `process.nextTick`) have higher priority and are executed completely at the end of the current operation, before the event loop moves to the next phase.
    *   **Macrotasks** (e.g., `setTimeout`, `setInterval`, network I/O callbacks) are executed in subsequent loop iterations.
*   **Red Flags:** Believes JavaScript runs multiple threads for user code; cannot explain why a `setTimeout(..., 0)` doesn't run immediately; does not know what Libuv or Promises are.

#### **Q4.2: Frontend Performance & Modern Styling**
*   **Question:** How do you optimize web page load times and rendering performance when building a user interface? How do you prevent blocking UI paint cycles with CSS and JavaScript?
*   **Target Competency:** Browser rendering path, bundle size reduction, UX principles.
*   **Expected Answer:**
    *   **CSS Optimizations:** Place critical CSS inline or in the `<head>` to prevent Flash of Unstyled Content (FOUC). Avoid complex CSS selectors. Use CSS variables instead of heavy JS calculations.
    *   **JS Optimizations:** Load script bundles asynchronously (`async` or `defer`), use code splitting/lazy loading for routes (e.g., dynamic imports in React/Vite), and keep bundle sizes small using tree-shaking.
    *   **Critical Rendering Path:** Minimize DOM depth, avoid layout thrashing, and batch DOM manipulations (or leverage virtual DOM reconciliation).
    *   **Asynchronous Queries:** Use AJAX (Axios/Fetch) with proper load indicators (skeletons) to fetch data asynchronously without blocking initial page loading.
*   **Red Flags:** Lacks structural approach to UI optimization (only mentions "compressing images"); doesn't understand the difference between `async` and `defer`; unfamiliar with lazy loading concepts.

---

### 5. Testing Tools & Methodologies

#### **Q5.1: Unit vs. Integration Testing with Spring Boot**
*   **Question:** What is the difference between `@SpringBootTest` and `@WebMvcTest`? How do you use `Mockito` to test a service method that connects to external web services?
*   **Target Competency:** Software quality assurance, mock frameworks, testing boundaries.
*   **Expected Answer:**
    *   `@SpringBootTest` loads the **full application context**, starting all bean configurations. It is ideal for end-to-end integration tests but runs slower.
    *   `@WebMvcTest` is a slice test that **only bootstrap controllers, security filters, and routing controllers**. You must mock downstream dependencies (`@MockBean`). It runs extremely fast.
    *   **Mockito Usage:** Use `@Mock` for helper beans, `@InjectMocks` on the target class, and verify behaviors using `Mockito.when(...).thenReturn(...)` or `Mockito.verify(...)` to isolate logic from external services.
*   **Red Flags:** Runs full `@SpringBootTest` for simple class unit tests; does not know how to stub external REST APIs; does not understand `@Mock` vs `@Spy`.

---

### 6. SDLC / PDLC Lifecycle Management

#### **Q6.1: Product Development Lifecycle (PDLC) vs. Software Development Lifecycle (SDLC)**
*   **Question:** How does the Product Development Lifecycle (PDLC) differ from the Software Development Lifecycle (SDLC)? As a developer, how do you ensure that your code deliverables align with product requirements during sprint planning?
*   **Target Competency:** Release management, cross-functional alignment, lifecycle execution.
*   **Expected Answer:**
    *   **SDLC** focuses on the technical phases of writing, compiling, testing, and deploying software.
    *   **PDLC** is a broader framework covering the entire life of the product from market research, design, validation, sales alignment, product launch, customer feedback, and retirement.
    *   **Sprint Alignment:** Write detailed user stories with clear Acceptance Criteria (Gherkin format: Given/When/Then). Collaborate with Product Managers to refine requirements, create wireframes/mockups, and conduct peer code reviews to confirm requirements match the implementation.
*   **Red Flags:** Views programming as isolated from business goals; cannot explain Agile ceremonies; does not know what acceptance criteria are.

---

### 7. Problem-Solving, Troubleshooting & Collaboration

#### **Q7.1: Diagnosing Production OutOfMemoryError (OOM) & Slow Response Times**
*   **Question:** You receive an alert stating the production server is returning `HTTP 503 Service Unavailable` due to a `java.lang.OutOfMemoryError: Java heap space`. Simultaneously, database queries are hanging. How do you troubleshoot and solve this?
*   **Target Competency:** Diagnostic capacity under pressure, logging, APM tools.
*   **Expected Answer:**
    1.  **Immediate Remediation:** Gather thread/heap dumps if automated, restart instance (or route traffic to healthy replica) to restore availability.
    2.  **Diagnostics:** Analyze heap dumps using tools like Eclipse Memory Analyzer (MAT) to identify memory leaks or object accumulation.
    3.  **Thread Dumps:** Run `jstack` or check APM logs to look for deadlocked threads or threads blocked on network/DB sockets.
    4.  **Database Diagnostics:** Look at slow query logs to detect missing indexes or N+1 query patterns. Examine connection pool utilization (e.g., HikariCP exhaustion).
*   **Red Flags:** Instantly proposes allocating more memory without analyzing the leak root cause; has never used stack trace analyzer or heap dump tools; does not mention query optimization or DB pool checks.

---

## Part B: Desirable Skills Evaluation

### 1. Artificial Intelligence Integration

#### **Q1.1: Enterprise LLM Integration & Prompt Engineering**
*   **Question:** If asked to build an AI feature that automatically summarizes complex medical transcripts or mechanic diagnosis logs, how would you design the backend API integration with an LLM? How do you prevent "hallucinations" and secure sensitive data?
*   **Target Competency:** Cognitive computing, model deployment architectures, data compliance.
*   **Expected Answer:**
    *   **API Design:** Use a Spring Boot service to call LLM APIs (e.g., Vertex AI, OpenAI) using structured JSON templates. Use asynchronous requests (`CompletableFuture` or WebSockets) for streaming tokens back to the UI.
    *   **Hallucination Prevention:** Use **RAG (Retrieval-Augmented Generation)** to ground prompts in specific reference documents. Constrain response schemas using system prompts or JSON mode.
    *   **Security:** Mask Personal Identifiable Information (PII) before sending payloads to third-party LLMs. Store data in a secure vector database with Role-Based Access Control (RBAC).
*   **Red Flags:** Proposes training a model from scratch for a simple summarization task; lacks security considerations for API keys or PII data.

---

### 2. Device Interfacing & Real-Time Data Ingestion

#### **Q2.1: Handling Serial/IoT Stream Ingestion**
*   **Question:** How would you design a Spring Boot application to communicate with a physical laboratory device or automobile OBD-II scanner that outputs raw binary streams over RS-232/USB/TCP? How do you stream this telemetry to a browser in real-time?
*   **Target Competency:** Hardware integration, sockets, real-time push frameworks.
*   **Expected Answer:**
    *   **Interfacing:** Use library bindings (e.g., `jSerialComm` or Netty for TCP/UDP) to open a non-blocking stream.
    *   **Threading:** Process the input stream on a dedicated background thread pool using a consumer pattern to avoid blocking the main server threads.
    *   **Data Parsing:** Parse binary packets into Java DTOs, validating checksums (e.g., CRC16).
    *   **Streaming to UI:** Push parsed metrics to the frontend using **WebSockets** or **Server-Sent Events (SSE)**.
*   **Red Flags:** Suggests polling the database from the UI to show real-time metrics; runs long-running stream reads directly inside Servlet web threads.

---

### 3. Image Processing & Heavy Asset Handling

#### **Q3.1: Memory-Safe Image Manipulation**
*   **Question:** You need to build a service that processes, crops, and watermarks user-uploaded high-resolution images. How do you handle this in Java without exhausting JVM memory during periods of high concurrency?
*   **Target Competency:** Stream-based processing, garbage collector optimization.
*   **Expected Answer:**
    *   Avoid reading the entire image array into memory at once as a `BufferedImage` if using huge raw formats.
    *   Use streaming libraries or command-line wrappers (e.g., ImageMagick) running outside the JVM memory space.
    *   If using Java libraries (e.g., `javax.imageio`), stream output directly to target file storage (e.g., S3) rather than storing byte arrays in memory.
    *   Constrain queue inputs using ThreadPool executors to limit concurrent image processing tasks.
*   **Red Flags:** Suggests storing massive raw byte arrays in Spring session context or JVM heap; ignores threading constraints for image manipulation.

---

### 4. Mobile Application Development (Android / iOS)

#### **Q4.1: Offline-First Synchronization Architecture**
*   **Question:** What are the key architecture patterns you would apply on an Android or iOS application to ensure it can function offline (e.g., a technician working in a basement with no network connectivity) and then synchronize local changes back to a Spring Boot backend?
*   **Target Competency:** Mobile storage, conflict resolution, caching.
*   **Expected Answer:**
    *   **Local Storage:** Use local databases like SQLite via Room (Android) or CoreData/SwiftData (iOS).
    *   **Sync Logic:** Save actions in a transaction queue with sync status flags (`PENDING`, `SYNCED`, `FAILED`).
    *   **Network Monitoring:** Use WorkManager (Android) or BackgroundTasks (iOS) to automatically trigger sync jobs when network connectivity changes.
    *   **Conflict Resolution:** Implement timestamps, vector clocks, or let the server evaluate version tokens (optimistic locking) to resolve conflicts.
*   **Red Flags:** Suggests blocking the UI when network is lost; does not address concurrency or primary key conflicts during synchronization.

---

### 5. Health Informatics Systems & Standards (DICOM, HL7, FHIR, SNOMED-CT)

#### **Q5.1: Medical Image Archiving & DICOM Parsing**
*   **Question:** Explain the structure of a DICOM file. How would you extract metadata (such as patient demographics or modality) and display the actual medical image slice within a web application?
*   **Target Competency:** Medical imaging standards, pixel mapping.
*   **Expected Answer:**
    *   **Structure:** A DICOM file contains a preamble, header metadata (group and element tags, e.g., `0010,0010` for Patient Name), and a pixel data block.
    *   **Extraction:** Use libraries like **dcm4che** in Java or **pydicom** in Python to parse metadata tags.
    *   **Web Rendering:** Convert high-bitrate gray-scale pixel data to standard formats (JPEG/PNG/WebP) using windowing level/width transformations, or serve DICOM directly using JS viewers (e.g., Cornerstone.js).
*   **Red Flags:** Thinks DICOM is a standard image file like JPEG; does not know that metadata and pixel data reside in the same file.

#### **Q5.2: Legacy HL7 v2 to FHIR & Terminology Mapping (SNOMED-CT)**
*   **Question:** How does a traditional HL7 v2 ER7 (pipe-delimited) message differ from an HL7 FHIR resource? What challenges occur when mapping diagnoses inside these messages to SNOMED-CT codes?
*   **Target Competency:** Healthcare interoperability, ontology standardization, compliance.
*   **Expected Answer:**
    *   **HL7 v2:** Trigger-based, pipe-delimited segment format (e.g., MSH, PID, PV1, OBX) difficult to parse without custom parsers.
    *   **FHIR:** RESTful, resource-oriented framework leveraging JSON/XML (e.g., `Patient`, `Observation`, `Condition` resources).
    *   **SNOMED-CT Mapping Challenges:** Free-text descriptions in legacy messages require NLP or semantic lookup to map to precise SNOMED-CT concepts. You must handle complex hierarchal parent-child relationships and post-coordination (combining multiple concepts).
*   **Red Flags:** Lacks understanding of FHIR's HTTP REST foundation; does not know what HL7 stands for; has no concept of code systems/ontologies in SNOMED-CT.

---

### 6. DevOps, Containerization & Automation

#### **Q6.1: CI/CD Pipelines & Container Orchestration**
*   **Question:** Walk me through how you containerize a Spring Boot application. What Kubernetes objects (e.g., Pod, Deployment, Service, Ingress) are required to expose this service securely?
*   **Target Competency:** Deployment automation, environment parity, infrastructure management.
*   **Expected Answer:**
    *   **Containerization:** Use multi-stage Dockerfiles to build the JAR (using Maven/Gradle) and package it in a minimal JRE base image (e.g., distroless or alpine).
    *   **Kubernetes Objects:**
        *   **Pod:** Smallest execution unit containing the application container.
        *   **Deployment:** Manages replica sets, rollout strategies (rolling updates), and self-healing.
        *   **Service:** Provides a stable IP address and performs internal load balancing across Pod replicas.
        *   **Ingress:** Manages external HTTP routing rules, SSL termination, and routes traffic into the cluster Services.
*   **Red Flags:** Hardcodes environment properties in Docker images; is unfamiliar with multi-stage builds; confuses Kubernetes Services with Ingress.

---

## Candidate Evaluation & Grading Rubric

When interviewing candidates, grade their responses using the following matrix:

| Score | Rating | Criteria |
| :--- | :--- | :--- |
| **5** | **Expert** | Deep technical expertise. Explains background trade-offs, shows structural design skills, mentions concrete libraries and production edge cases. |
| **4** | **Strong** | Solid answers. Explains core mechanics accurately and provides working examples. Understands best practices. |
| **3** | **Competent** | Understands the concepts but answers are superficial. Needs guidance on complex optimization or trade-offs. |
| **2** | **Weak** | Fails to explain core mechanics (e.g., cannot explain how hashing collisions work or how REST differs from SOAP). |
| **1** | **Unsatisfactory** | No knowledge of the technology or framework. Lacks basic problem-solving logic. |

### Technical Alignment Index
*   **Essential Skills Cutoff:** Candidate must average **$\ge$ 3.5** across Part A sections.
*   **Desirable Skills Weight:** Focus on candidates scoring **$\ge$ 4.0** on healthcare or hardware integration segments if hiring specifically for medical/interfacing domains.

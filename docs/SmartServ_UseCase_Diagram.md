# 3.2 Use Case Diagram

## SmartServ – Automobile Service Management System

![Fig. Use Case Diagram for SmartServ System](C:/Users/shiva/.gemini/antigravity-ide/brain/0eddd896-1c14-4599-950a-fb8519cfdebf/smartserv_usecase_final_1786364627637.png)

---

### Use Case Description

The above use case diagram illustrates the interaction between four primary actors
(**Admin**, **Manager**, **Customer**, and **Mechanic**) and the SmartServ Automobile
Service Management System. The system boundary encloses all 17 functional use cases
that the system provides. Each actor is connected to their respective use cases
through association lines as verified from the application source code.

---

### Use Case – Actor Mapping (Code-Verified)

| # | Use Case | Admin | Manager | Customer | Mechanic |
|---|----------|:-----:|:-------:|:--------:|:--------:|
| UC1 | Register Account | | | ✔ | |
| UC2 | Login & Logout from System | ✔ | ✔ | ✔ | ✔ |
| UC3 | Update My Profile | ✔ | ✔ | ✔ | ✔ |
| UC4 | Manage Users & Full Application | ✔ | | | |
| UC5 | Manage Inventory | ✔ | ✔ | | |
| UC6 | Manage Job Cards | ✔ | ✔ | | |
| UC7 | Assign Mechanic to Job Card | ✔ | ✔ | | |
| UC8 | Generate Invoice | ✔ | ✔ | | |
| UC9 | Approve / Reject Appointments | ✔ | ✔ | | |
| UC10 | Book Service Appointment | | | ✔ | |
| UC11 | Register Vehicle | | | ✔ | |
| UC12 | Make Payment | | | ✔ | |
| UC13 | View Invoices | ✔ | ✔ | ✔ | |
| UC14 | Request RSA Service | | | ✔ | |
| UC15 | View Dashboard | | ✔ | | ✔ |
| UC16 | View Assigned Job Cards | | | | ✔ |
| UC17 | Update Job Card Status | | | | ✔ |

---

### Actor Descriptions

| Actor | Role Description |
|-------|-----------------|
| **Admin** | Has full access to the system. Manages users, inventory, job cards, invoices, and appointments. |
| **Manager** | Oversees service operations. Manages job cards, assigns mechanics, generates invoices, approves/rejects appointments, and views the manager dashboard. |
| **Customer** | Registers an account, adds vehicles, books service appointments, requests RSA, makes payments, and views invoices. |
| **Mechanic** | Views assigned job cards, updates job card status (start/complete work), and accesses the mechanic dashboard. |

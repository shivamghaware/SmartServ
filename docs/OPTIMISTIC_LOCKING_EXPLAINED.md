# Optimistic Locking & @Version Annotation Explained

This document provides a simple, step-by-step explanation of how **Optimistic Locking** and the `@Version` annotation prevent database update conflicts in a multi-user environment.

---

## 1. What is the `@Version` Annotation?

In Spring Boot, `@Version` is an annotation from JPA/Hibernate. You place it on a field in your entity class (like we did in [Inventory.java](../inventory-service/src/main/java/com/smartserv/inventory/entity/Inventory.java)):

```java
@Version
private Integer version;
```

When you add this annotation:
* **Automatic Management**: Hibernate automatically manages this field. You do not need to read, write, or increment it manually.
* **Initialization**: Every time a new row is inserted into the database, Hibernate sets its version to `0`.
* **Auto-Increment**: Every time that row is updated in the database, Hibernate automatically increases the version number by 1 (e.g., from `0` to `1`, `1` to `2`, and so on).

---

## 2. Step-by-Step Example: The Spark Plug Race

Imagine we have only **1 Spark Plug** left in stock, and its database row looks like this:

| product_id | item_name | stock_quantity | version |
| :--- | :--- | :---: | :---: |
| **12** | Spark Plug | **1** | **5** |

Now, two mechanics (Mechanic A and Mechanic B) try to check out this spark plug at the exact same millisecond. Here is exactly what happens:

### Step 1: Both read the data
Both application servers query the database for product ID `12`.
* **Mechanic A's server reads**: Stock = 1, Version = **5**
* **Mechanic B's server reads**: Stock = 1, Version = **5**

### Step 2: Both calculate new stock locally
Both servers calculate what the new stock should be in memory:
* **Mechanic A**: `1 - 1 = 0` spark plugs.
* **Mechanic B**: `1 - 1 = 0` spark plugs.

### Step 3: Mechanic A saves first
Mechanic A's server completes the save operation a fraction of a millisecond earlier. Behind the scenes, Hibernate sends this SQL update command to the database:

```sql
UPDATE inventory 
SET stock_quantity = 0, version = 6 
WHERE product_id = 12 AND version = 5;
```

* **Result**: The database checks if there is a row with `product_id = 12` and `version = 5`. **Yes, there is.**
* The database updates the stock to `0`, increases the version to `6`, and returns **"1 row updated"**. Mechanic A successfully buys the spark plug.

The database table now looks like this:

| product_id | item_name | stock_quantity | version |
| :--- | :--- | :---: | :---: |
| **12** | Spark Plug | **0** | **6** |

### Step 4: Mechanic B tries to save second
A split second later, Mechanic B's server tries to save its update. Hibernate sends this SQL update command:

```sql
UPDATE inventory 
SET stock_quantity = 0, version = 6 
WHERE product_id = 12 AND version = 5; -- Mechanic B's server still thinks the version is 5!
```

* **Result**: The database looks for a row with `product_id = 12` and `version = 5`.
* **It cannot find one!** (The version of product `12` is now `6`).
* The database updates **0 rows** and returns **"0 rows updated"** to Hibernate.

### Step 5: Hibernate triggers the Exception
When Hibernate sees that the update command affected `0` rows instead of the expected `1` row, it knows that someone else modified the data while Mechanic B was reading it.

* Hibernate halts the operation, rolls back the transaction, and throws an `OptimisticLockException`.
* Our service catches this exception and returns a clean error message to Mechanic B: *"Stock was modified by another user. Please refresh and try again."*

---

## 3. Why is it called "Optimistic"?

* > [!TIP]
  > **Optimistic Locking**
  > We are *optimistic* that conflicts will rarely happen. We do not block other users from reading or editing the database table. We only check for a conflict at the last microsecond when saving. This keeps the application super fast and responsive.

* > [!NOTE]
  > **Pessimistic Locking (The Alternative)**
  > We are *pessimistic* and assume conflicts will happen constantly. When Mechanic A reads the row, the database locks it. Mechanic B is forced to wait (their screen freezes or spins) until Mechanic A finishes. This is secure but makes the app very slow under high load.

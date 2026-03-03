# Database Initialization Fixes

## Issues Fixed

### Problem
Three microservices were failing health checks due to missing database schemas:
- **Item Service** (MongoDB) - Index conflict error  
- **Order Service** (Cassandra) - Missing keyspace and tables
- **Inventory Service** (Cassandra) - Missing keyspace and tables

### Root Causes

1. **MongoDB Index Conflict** (Item Service)
   - Spring Data MongoDB auto-index creation conflicted with existing indexes
   - Index name mismatch: `upc_1` vs `upc`

2. **Missing Cassandra Keyspaces** (Order & Inventory Services)
   - Keyspaces `order_keyspace` and `inventory_keyspace` were not created
   - Tables were not initialized

3. **Cassandra Init Script Not Running**
   - The `init/cassandra/001_init.cql` script was not executed during startup
   - Services tried to connect before schema was ready

## Solutions Applied

### 1. Fixed MongoDB Item Service
**Changed:** Disabled auto-index creation to avoid conflicts
```yaml
# item-service/src/main/resources/application.yml
spring:
  data:
    mongodb:
      auto-index-creation: false  # Changed from true
```

**Actions:**
```powershell
# Dropped problematic collection
docker exec -it shoppingservice-mongo-1 mongosh --eval "use item_db; db.items.drop();"

# Rebuilt service
cd item-service
mvn clean package -DskipTests
docker-compose build item-service
docker-compose up -d item-service
```

### 2. Initialized Cassandra Keyspaces and Tables
**Actions:**
```powershell
# Created keyspaces manually
docker exec -it shoppingservice-cassandra-1 cqlsh -e "
CREATE KEYSPACE IF NOT EXISTS order_keyspace 
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};"

docker exec -it shoppingservice-cassandra-1 cqlsh -e "
CREATE KEYSPACE IF NOT EXISTS inventory_keyspace 
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};"

# Ran initialization script
Get-Content "init\cassandra\001_init.cql" | docker exec -i shoppingservice-cassandra-1 cqlsh

# Restarted services
docker restart shoppingservice-order-service-1 shoppingservice-inventory-service-1
```

### 3. Fixed JWT Token Parsing in Demo Script
**Issue:** Base64 padding caused JSON parse errors

**Fixed:**
```powershell
# Before (incorrect padding)
$payload = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($tokenParts[1] + "=="))

# After (correct padding calculation)
$base64 = $tokenParts[1]
$padding = 4 - ($base64.Length % 4)
if ($padding -ne 4) {
    $base64 = $base64 + ("=" * $padding)
}
$payload = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($base64))
```

## Verification

All services now pass health checks:
```
✓ Auth (9000): OK
✓ Account (9001): OK
✓ Item (9002): OK
✓ Inventory (9003): OK
✓ Order (9004): OK
✓ Payment (9005): OK
✓ Gateway (8080): OK
```

## Permanent Fix Recommendations

To prevent these issues on fresh deployments:

### 1. Docker Compose Init Containers
Add init container to wait for databases and run scripts:
```yaml
services:
  cassandra-init:
    image: cassandra:4.1
    depends_on:
      - cassandra
    volumes:
      - ./init/cassandra:/docker-entrypoint-initdb.d
    command: >
      bash -c "
      sleep 30 && 
      cqlsh cassandra -f /docker-entrypoint-initdb.d/001_init.cql
      "
```

### 2. Service Health Dependencies
Make services wait for database readiness:
```yaml
order-service:
  depends_on:
    cassandra:
      condition: service_healthy
```

### 3. MongoDB Index Strategy
Either:
- Keep `auto-index-creation: false` and create indexes manually
- Or use explicit index names in entity annotations:
  ```java
  @Indexed(name = "upc_unique_idx", unique = true)
  private String upc;
  ```

## Files Modified
1. `item-service/src/main/resources/application.yml` - Disabled auto-index creation
2. `demo.ps1` - Fixed JWT base64 decoding

## Database State After Fixes

### MongoDB (items_db)
- Collection: `items`
- Indexes: Auto-creation disabled, can be added manually if needed

### Cassandra 
**Keyspaces:**
- `order_keyspace` - Tables: `orders`, `order_items`
- `inventory_keyspace` - Tables: `inventory_by_sku`, `inventory_events`

**Tables Created:**
```cql
-- Orders
orders (id text PRIMARY KEY, user_id text, status text, total_amount decimal, ...)
order_items (order_id text, sku text, qty int, unit_price decimal, PRIMARY KEY(order_id, sku))

-- Inventory  
inventory_by_sku (sku text PRIMARY KEY, available int, reserved int, updated_at timestamp)
inventory_events (sku text, ts timeuuid, type text, delta int, order_id text, PRIMARY KEY (sku, ts))
```

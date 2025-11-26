# Backend Setup Instructions

## Complete Java Spring Boot Backend for Decentralized Supply Chain Tracking

This document provides complete setup instructions for running the Java Spring Boot backend with PostgreSQL and blockchain integration.

## Architecture Overview

```
Backend Stack:
- Java 17+
- Spring Boot 3.2.1
- Spring Security + JWT Authentication
- Spring Data JPA
- PostgreSQL Database
- Flyway DB Migrations
- Web3j for Blockchain Integration
- Maven Build System

Frontend Stack:
- React 18 + TypeScript
- Axios for API calls
- JWT Token Authentication
- Responsive UI with Tailwind CSS
```

## Prerequisites

1. **Java Development Kit (JDK) 17 or higher**
   ```bash
   java -version
   ```

2. **Maven 3.6+**
   ```bash
   mvn -version
   ```

3. **PostgreSQL 14+**
   ```bash
   psql --version
   ```

4. **Node.js 18+ and npm** (for frontend)
   ```bash
   node -version
   npm -version
   ```

5. **Optional: Docker** (for containerized PostgreSQL)

## Step 1: Database Setup

### Option A: Local PostgreSQL Installation

1. Install PostgreSQL from https://www.postgresql.org/download/

2. Create database and user:
```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database
CREATE DATABASE supply_chain_db;

-- Create user (optional, or use default postgres user)
CREATE USER supply_chain_user WITH PASSWORD 'supply_chain_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE supply_chain_db TO supply_chain_user;

-- Exit
\q
```

### Option B: Docker PostgreSQL

```bash
docker run --name supply-chain-postgres \
  -e POSTGRES_DB=supply_chain_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15
```

## Step 2: Backend Configuration

1. Navigate to backend directory:
```bash
cd backend
```

2. Update `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/supply_chain_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# JWT Secret (CHANGE THIS IN PRODUCTION!)
jwt.secret=YourSuperSecretKeyForJWTTokenGenerationChangeThisInProduction123456789012345678901234567890

# JWT Expiration (24 hours in milliseconds)
jwt.expiration=86400000

# Blockchain Configuration
blockchain.enabled=true
blockchain.simulation=true

# CORS (add your frontend URL)
cors.allowed.origins=http://localhost:5173,http://localhost:3000
```

## Step 3: Build and Run Backend

### Using Maven:

```bash
# Navigate to backend directory
cd backend

# Clean and build
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### Alternative: Run JAR directly:

```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/blockchain-supply-chain-1.0.0.jar
```

## Step 4: Verify Backend is Running

1. Check health endpoint:
```bash
curl http://localhost:8080/api/auth/login
```

2. You should see Spring Boot running logs:
```
Started SupplyChainApplication in X.XXX seconds
```

3. Database tables should be automatically created by Flyway migrations

## Step 5: Frontend Setup

1. Navigate to project root (where package.json is located):
```bash
cd ..  # If you're in backend directory
```

2. Install dependencies:
```bash
npm install
```

3. Create `.env` file in project root:
```env
VITE_API_BASE_URL=http://localhost:8080/api
```

4. Start frontend development server:
```bash
npm run dev
```

Frontend will start on `http://localhost:5173`

## Step 6: Test the Application

### 1. Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "supplier@example.com",
    "password": "password123",
    "role": "SUPPLIER"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "supplier@example.com",
    "password": "password123"
  }'
```

Save the returned JWT token.

### 3. Create a Product (requires JWT token)

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Organic Cotton",
    "description": "Premium organic cotton",
    "category": "Raw Material",
    "sku": "ORG-COT-001"
  }'
```

### 4. Create a Batch

```bash
curl -X POST http://localhost:8080/api/batches \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "productId": 1,
    "batchNumber": "BATCH-001",
    "quantity": 100,
    "currentLocation": "Warehouse A"
  }'
```

## API Endpoints Summary

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### Products (Authenticated)
- `POST /api/products` - Create product (SUPPLIER role)
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID

### Batches (Authenticated)
- `POST /api/batches` - Create batch (SUPPLIER role)
- `GET /api/batches` - Get all batches
- `GET /api/batches/{id}` - Get batch by ID
- `PUT /api/batches/{id}/status` - Update batch status (MANUFACTURER, DISTRIBUTOR, RETAILER roles)
- `GET /api/batches/{id}/history` - Get batch event history

### Consumer (Public)
- `GET /api/consumer/verify?batchId={id}` - Verify batch and view complete history

## User Roles and Permissions

1. **ADMIN** - Manage users and view all data
2. **SUPPLIER** - Create products and batches
3. **MANUFACTURER** - Process batches, update status
4. **DISTRIBUTOR** - Update shipment status and location
5. **RETAILER** - Mark products as available for sale
6. **CONSUMER** - Verify product authenticity and view history

## Blockchain Integration

### Current State: Simulation Mode

The backend is configured with `blockchain.simulation=true`, which means:
- Blockchain transactions are simulated
- Random transaction hashes are generated
- All events are stored in PostgreSQL
- No actual Ethereum network calls are made

### Enabling Real Blockchain Integration

To connect to real Ethereum network:

1. **Deploy Smart Contract:**
```bash
cd blockchain
npm install
npx hardhat compile
npx hardhat run scripts/deploy.js --network sepolia
```

2. **Update application.properties:**
```properties
blockchain.enabled=true
blockchain.simulation=false
blockchain.rpc.url=https://sepolia.infura.io/v3/YOUR_INFURA_KEY
blockchain.contract.address=0xYourDeployedContractAddress
blockchain.private.key=your-private-key-here
```

3. **Generate Java Contract Wrapper:**
```bash
web3j generate solidity \
  -b blockchain/artifacts/contracts/SupplyChain.sol/SupplyChain.json \
  -o backend/src/main/java \
  -p com.supplychain.contracts
```

4. **Update BlockchainService.java** to use generated contract wrapper instead of simulation

## Database Schema

Flyway automatically creates these tables:
- `users` - User accounts with roles
- `products` - Product catalog
- `batches` - Batch/shipment tracking
- `supply_chain_events` - All supply chain events with blockchain hashes
- `payments` - Payment release records

## Troubleshooting

### Backend won't start:
- Check PostgreSQL is running: `pg_isready`
- Verify database connection in application.properties
- Check port 8080 is not in use: `lsof -i :8080`

### Database connection errors:
- Ensure PostgreSQL is running
- Verify credentials in application.properties
- Check firewall settings

### Frontend can't connect to backend:
- Verify backend is running on port 8080
- Check CORS configuration in application.properties
- Verify VITE_API_BASE_URL in .env file

### JWT token errors:
- Token might be expired (24 hour expiration)
- Login again to get new token
- Check jwt.secret is set in application.properties

## Production Deployment

### Backend:
1. Change jwt.secret to a strong random value
2. Use environment variables for sensitive data
3. Set blockchain.simulation=false for real blockchain
4. Enable HTTPS
5. Configure proper CORS origins
6. Use production-grade PostgreSQL setup

### Database:
1. Use managed PostgreSQL service (AWS RDS, Google Cloud SQL, etc.)
2. Enable SSL connections
3. Regular backups
4. Monitoring and alerting

### Blockchain:
1. Deploy smart contract to mainnet
2. Use proper key management (AWS KMS, HashiCorp Vault)
3. Set appropriate gas limits
4. Monitor transaction costs

## Complete Workflow Example

1. **Supplier** registers and creates a product
2. **Supplier** creates a batch with CREATED status
3. **Manufacturer** receives batch, updates to IN_TRANSIT, then DELIVERED
4. **System** automatically releases payment when status becomes DELIVERED
5. **Distributor** updates location and ownership
6. **Retailer** marks as available for sale (SOLD status)
7. **Consumer** scans batch ID and views complete verified history

Each status change:
- Creates a supply chain event in database
- Records transaction on blockchain (simulated or real)
- Stores transaction hash and block number
- Maintains immutable audit trail

## Support

For issues or questions:
1. Check application logs in `logs/` directory
2. Review PostgreSQL logs
3. Test API endpoints with Postman or curl
4. Verify JWT tokens are valid and not expired

## License

MIT License - See LICENSE file for details

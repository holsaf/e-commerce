# 🛒 Virtual Shop - E-commerce Backend API

A modern, secure e-commerce backend built with Spring Boot, featuring JWT authentication, role-based access control, and comprehensive API documentation.

## 🚀 Quick Start

### Prerequisites
- **Docker & Docker Compose**
- **MySQL 8.0+** running locally on port 3306
- **Java 17+** and **Gradle 8.5+** (for local development)

### 🐳 Run with Docker (Recommended)

1. **Clone and navigate to the project:**
   ```bash
   git clone https://github.com/holsaf/e-commerce.git
   cd backend
   ```

2. **Start your local MySQL database:**
   ```bash
   # Ensure MySQL is running on localhost:3306
   # Database: <your_db_scheme>
   # User: <user_db>, Password: <password_db>
   ```

3. **Run the application:**
   ```bash
   docker-compose up --build
   ```

4. **Access the application:**
   - **API Base URL:** http://localhost:8080
   - **Swagger UI:** http://localhost:8080/swagger-ui/index.html
   - **Health Check:** http://localhost:8080/actuator/health

### 💻 Run Locally (Alternative)

1. **Set up the database:**
   ```sql
   CREATE DATABASE virtual_shop;
   ```

2. **Run the application:**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

## 📚 API Documentation

### Interactive Documentation
- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Key Endpoints

#### 🔐 Authentication
| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| `POST` | `/api/auth/register` | Register new customer | Public |
| `POST` | `/api/auth/login` | User login | Public |
| `POST` | `/api/auth/admin/register` | Register admin user | Public |

#### 👥 Users
| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| `GET` | `/api/users` | List all users | Admin |
| `GET` | `/api/users/{id}` | Get user by ID | Admin |
| `GET` | `/api/users/profile` | Get current user profile | Authenticated |
| `PUT` | `/api/users/profile` | Update user profile | Authenticated |

#### 🛍️ Products
| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| `GET` | `/api/products` | List products (with pagination/search) | Public |
| `GET` | `/api/products/{id}` | Get product details | Public |
| `POST` | `/api/products` | Create new product | Admin |
| `PUT` | `/api/products/{id}` | Update product | Admin |
| `DELETE` | `/api/products/{id}` | Delete product | Admin |

#### 📦 Orders
| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| `POST` | `/api/orders` | Create new order | Authenticated |
| `GET` | `/api/orders` | Get user's orders | Authenticated |
| `GET` | `/api/orders/{id}` | Get order details | Authenticated |
| `GET` | `/api/orders/admin/all` | Get all orders | Admin |

### 🔑 Authentication

The API uses **JWT Bearer Token** authentication:

1. **Login to get token:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email": "user@example.com", "password": "password"}'
   ```

2. **Use token in requests:**
   ```bash
   curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/orders
   ```

### 👤 User Roles
- **CUSTOMER**: Can browse products, manage profile, create orders
- **ADMIN**: Full access to users, products, and orders management

## 🏗️ Architecture Overview

### Technology Stack
- **Framework:** Spring Boot 3.3.5
- **Language:** Java 17
- **Security:** Spring Security with JWT
- **Database:** MySQL 8.0 with JPA/Hibernate
- **Documentation:** OpenAPI 3 (Swagger)
- **Build Tool:** Gradle 8.5
- **Containerization:** Docker with multi-stage builds

### Architecture Decisions

#### 🛡️ **Security Design**
- **JWT Stateless Authentication:** Scalable, suitable for microservices and cloud deployment
- **Role-Based Access Control (RBAC):** Clean separation between customer and admin capabilities
- **Password Encryption:** BCrypt for secure password storage

#### 🗄️ **Database Design**
- **Single Table Inheritance:** `User` entity with `Customer`/`Admin` subclasses for role-based differentiation
- **JPA/Hibernate:** Simplified data access with automatic schema generation
- **Connection Pooling:** HikariCP for optimal database performance

#### 🏭 **Layered Architecture**
```
┌─────────────────────────────────────┐
│          REST Controllers           │  ← API Layer
├─────────────────────────────────────┤
│            Services                 │  ← Business Logic
├─────────────────────────────────────┤
│          Repositories               │  ← Data Access
├─────────────────────────────────────┤
│            Entities                 │  ← Domain Models
└─────────────────────────────────────┘
```

#### 🐳 **Containerization Strategy**
- **Multi-stage Docker Build:** Separates build environment from runtime for smaller images
- **Non-root User:** Security best practice for container execution
- **Health Checks:** Built-in monitoring for container orchestration
- **Development Profile:** Docker connects to host MySQL for local development

#### 🔧 **Configuration Management**
- **Spring Profiles:** Separate configurations for `dev`, `prod`
- **Environment Variables:** Secure configuration for sensitive data
- **Property Externalization:** Easy deployment across environments

### 🌐 Deployment Architecture

#### Development Environment
```
┌─────────────────────────────────────┐
│     Docker Container (app)          │
│  ┌─────────────────────────────┐    │
│  │   Spring Boot Application   │    │
│  │        (port 8080)          │    │
│  └─────────────────────────────┘    │
└─────────────────────────────────────┘
                ↓ host.docker.internal
┌─────────────────────────────────────┐
│       Host Machine (MySQL)         │
│     localhost:3306/virtual_shop     │
└─────────────────────────────────────┘
```

#### Production Environment (AWS Fargate)
```
┌─────────────────────────────────────┐
│          AWS Fargate Task           │
│  ┌─────────────────────────────┐    │
│  │   Spring Boot Container     │    │
│  │      (production profile)   │    │
│  └─────────────────────────────┘    │
└─────────────────────────────────────┘
                ↓ VPC
┌─────────────────────────────────────┐
│            AWS RDS MySQL            │
│        (managed database)          │
└─────────────────────────────────────┘
```

## 🧪 Testing

### Run Tests
```bash
# Unit and Integration Tests
./gradlew test

# Test Coverage Report
./gradlew jacocoTestReport
# Report: build/reports/jacoco/test/html/index.html

# Performance Tests (Gatling)
./gradlew gatlingRun
```

### Test Database
- Integration tests use **H2 in-memory database**
- Configured in `src/test/resources/application-test.properties`
- Automatic test data cleanup between tests

## 📊 Monitoring & Observability

- **Health Checks:** `/actuator/health`
- **Application Info:** `/actuator/info` 
- **Metrics:** `/actuator/metrics`
- **Database Status:** Included in health checks

## 🔧 Development

### Project Structure
```
src/
├── main/java/com/ecommerce/backend/
│   ├── config/          # Security, Swagger configuration
│   ├── controller/      # REST endpoints
│   ├── dto/            # Request/Response DTOs
│   ├── entity/         # JPA entities
│   ├── repository/     # Data access layer
│   ├── service/        # Business logic
│   └── exception/      # Global exception handling
├── main/resources/
│   ├── application.properties          # Base configuration
│   ├── application-dev.properties      # Development settings
│   └── application-prod.properties     # Production settings
└── test/               # Unit and integration tests
```

### Code Quality
- **JaCoCo Coverage:** Minimum 85% test coverage
- **MapStruct:** Automatic DTO mapping
- **Lombok:** Reduced boilerplate code
- **Global Exception Handling:** Consistent error responses

## 🚢 Production Deployment

For AWS Fargate deployment, see: [`aws-fargate-deployment-guide.md`](aws-fargate-deployment-guide.md)


## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


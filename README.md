# ZetuMall Backend (Spring Boot)

Enterprise-grade backend API for ZetuMall e-commerce platform built with Spring Boot 3.2+ and Java 17.

## 🏗️ Architecture

- **Framework**: Spring Boot 3.2.1
- **Build Tool**: Gradle 8+
- **Database**: Supabase PostgreSQL (via JPA)
- **Authentication**: Supabase JWT
- **Language**: Java 17

## 📁 Project Structure

```
zetumall-backend/
├── src/main/java/com/zetumall/
│   ├── ZetumallApplication.java      # Main application
│   ├── config/                        # Configuration classes
│   │   ├── SecurityConfig.java
│   │   ├── CorsConfig.java
│   │   └── SupabaseProperties.java
│   ├── security/                      # Authentication & security
│   │   ├── JwtTokenValidator.java
│   │   ├── SupabaseAuthenticationFilter.java
│   │   └── SupabaseAuthenticatedUser.java
│   ├── shared/                        # Shared utilities
│   │   ├── BaseEntity.java
│   │   └── ApiResponse.java
│   └── health/                        # Health check endpoints
│       └── HealthController.java
└── src/main/resources/
    └── application.yml                # Application configuration
```

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Gradle 8+ (or use included wrapper)
- Access to Supabase PostgreSQL database

### Installation

1. **Clone and navigate to the project**:
   ```bash
   cd zetumall-backend
   ```

2. **Copy environment file**:
   ```bash
   copy .env.example .env
   ```

3. **Configure environment variables** in `.env`:
   ```env
   DATABASE_URL=your-supabase-database-url
   SUPABASE_URL=your-supabase-project-url
   SUPABASE_ANON_KEY=your-anon-key
   SUPABASE_JWT_SECRET=your-jwt-secret
   ```

4. **Build the project**:
   ```bash
   ./gradlew build
   ```

5. **Run the application**:
   ```bash
   ./gradlew bootRun
   ```

   The API will be available at `http://localhost:8080`

### Testing the Setup

Visit the health endpoint:
```bash
curl http://localhost:8080/api/health
```

Expected response:
```json
{
  "success": true,
  "message": "Health check successful",
  "data": {
    "status": "UP",
    "service": "zetumall-core-api",
    "version": "1.0.0",
    "database": "connected",
    "timestamp": 1704835200000
  }
}
```

## 🔐 Authentication

All API endpoints (except `/api/health` and `/api/public/**`) require Supabase JWT authentication.

### Making Authenticated Requests

Include the Supabase access token in the `Authorization` header:

```bash
curl -H "Authorization: Bearer YOUR_SUPABASE_JWT_TOKEN" \
     http://localhost:8080/api/stores
```

### How It Works

1. User logs in via Supabase Auth (handled by Next.js frontend)
2. Supabase returns a JWT token
3. Frontend includes token in API requests
4. Spring Boot validates token and extracts user information
5. Request is processed with authenticated user context

## 🗄️ Database

Uses existing Supabase PostgreSQL database with Prisma schema.

- **Connection Pooling**: Enabled via HikariCP
- **Schema**: `public` (default)
- **Multi-Schema Support**: Configured for `auth` and `public` schemas
- **Migrations**: Manual (using existing schema, no auto-generation)

## 📝 Development

### Adding a New Entity

1. Create entity class extending `BaseEntity`:
   ```java
   @Entity
   @Table(name = "stores", schema = "public")
   public class Store extends BaseEntity {
       // Add fields matching Prisma schema
   }
   ```

2. Create repository interface:
   ```java
   public interface StoreRepository extends JpaRepository<Store, String> {
       // Add custom queries
   }
   ```

3. Create service layer:
   ```java
   @Service
   public class StoreService {
       // Business logic
   }
   ```

4. Create controller:
   ```java
   @RestController
   @RequestMapping("/api/stores")
   public class StoreController {
       // API endpoints
   }
   ```

### Running Tests

```bash
./gradlew test
```

### Building for Production

```bash
./gradlew build -x test
java -jar build/libs/zetumall-backend-1.0.0.jar
```

## 🌐 Deployment

### Railway

1. Create new project on Railway
2. Connect your GitHub repository
3. Add environment variables
4. Deploy automatically on push

### Docker

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```bash
./gradlew build
docker build -t zetumall-backend .
docker run -p 8080:8080 --env-file .env zetumall-backend
```

## 📚 Next Steps

- [ ] Implement User entity and repository
- [ ] Implement Store management APIs
- [ ] Implement Product management APIs
- [ ] Implement Order & Escrow APIs
- [ ] Add Swagger/OpenAPI documentation
- [ ] Add unit and integration tests
- [ ] Set up CI/CD pipeline

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Write/update tests
4. Submit a pull request

## 📄 License

MIT License

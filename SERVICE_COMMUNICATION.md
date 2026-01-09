# Service Communication - Spring Boot ↔ Python AI

Testing guide for service-to-service communication between the Spring Boot core API and Python AI microservice.

## Architecture

```
Next.js Frontend
    ↓ (HTTP Request)
Spring Boot API (Port 8080)
    ↓ (HTTP Request)
Python AI Service (Port 8000)
    ↓ (Gemini API)
Google AI
```

## Prerequisites

1. **Both services running**:
   ```bash
   # Terminal 1: Spring Boot
   cd zetumall-backend
   gradlew.bat bootRun
   
   # Terminal 2: Python AI
   cd zetumall-ai-service
   venv\Scripts\activate
   uvicorn main:app --reload --port 8000
   ```

2. **Environment configured**:
   - Spring Boot `.env`: `AI_SERVICE_URL=http://localhost:8000`
   - Python `.env`: `GEMINI_API_KEY=your-key`

## Testing Service Communication

### 1. Direct Python AI Service Test

Test Python service directly:

```bash
curl -X POST http://localhost:8000/api/ai/description \
  -H "Authorization: Bearer YOUR_SUPABASE_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MacBook Pro 16",
    "category": "Computers",
    "features": ["M3 chip", "16GB RAM", "512GB SSD"]
  }'
```

Expected response:
```json
{
  "success": true,
  "description": "The MacBook Pro 16 is..."
}
```

### 2. Spring Boot Proxy Test

Test via Spring Boot (which calls Python):

```bash
curl -X POST http://localhost:8080/api/ai/product-description \
  -H "Authorization: Bearer YOUR_SUPABASE_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MacBook Pro 16",
    "category": "Computers",
    "features": ["M3 chip", "16GB RAM"]
  }'
```

Expected response:
```json
{
  "success": true,
  "message": "Description generated successfully",
  "data": {
    "description": "The MacBook Pro 16..."
  }
}
```

### 3. Store Description Generation

```bash
curl -X POST http://localhost:8080/api/ai/store-description \
  -H "Authorization: Bearer YOUR_SUPABASE_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tech Haven",
    "category": "Electronics",
    "tagline": "Your Premium Tech Store"
  }'
```

### 4. Product Tags Generation

```bash
curl -X POST http://localhost:8080/api/ai/tags \
  -H "Authorization: Bearer YOUR_SUPABASE_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15 Pro",
    "description": "Latest Apple smartphone with titanium design",
    "category": "Smartphones"
  }'
```

Expected response:
```json
{
  "success": true,
  "message": "Tags generated successfully",
  "data": {
    "tags": ["iPhone", "Apple", "Smartphone", "Titanium", "5G", ...]
  }
}
```

### 5. SEO Metadata Generation

```bash
curl -X POST http://localhost:8080/api/ai/seo \
  -H "Authorization: Bearer YOUR_SUPABASE_JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Gaming Mouse",
    "description": "High-precision wireless gaming mouse",
    "category": "Gaming Accessories"
  }'
```

## Integration in Product Service

The Product Service can now use AI features:

```java
@Service
public class ProductService {
    private final AiServiceClient aiServiceClient;
    
    public Product createProductWithAI(ProductCreateRequest request, String token) {
        // Generate AI description if not provided
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            String aiDescription = aiServiceClient.generateProductDescription(
                request.getName(),
                request.getCategory(),
                List.of(),
                token
            );
            request.setDescription(aiDescription);
        }
        
        // Create product normally
        return createProduct(request, userId);
    }
}
```

## Common Issues

### Issue: Connection Refused to AI Service

**Symptoms**:
```
Failed to generate product description via AI service: Connection refused
```

**Solution**:
1. Ensure Python AI service is running: `http://localhost:8000/health`
2. Check `AI_SERVICE_URL` in Spring Boot `.env`
3. Verify Python service port (default 8000)

### Issue: 401 Unauthorized from AI Service

**Symptoms**:
```
401 Unauthorized when calling Python AI
```

**Solution**:
1. Ensure JWT token is being passed from Spring Boot to Python
2. Verify `SUPABASE_JWT_SECRET` matches in both services
3. Check token expiration

### Issue: AI Service Timeout

**Symptoms**:
```
java.net.SocketTimeoutException: Read timed out
```

**Solution**:
1. Gemini API may be slow - increase timeout in `application.yml`:
   ```yaml
   ai-service:
     timeout: 60000  # 60 seconds
   ```
2. Check Gemini API quota/limits
3. Verify `GEMINI_API_KEY` is valid

## Service Health Checks

### Check Spring Boot
```bash
curl http://localhost:8080/api/health
```

### Check Python AI Service
```bash
curl http://localhost:8000/health
```

Both should return `200 OK` with health status.

## Monitoring Logs

### Spring Boot Logs
Watch for:
```
INFO c.z.ai.AiServiceClient : Calling AI service for product description
ERROR c.z.ai.AiServiceClient : Failed to generate product description via AI service
```

### Python Logs

Watch for:
```
INFO: 127.0.0.1:8080 - "POST /api/ai/description HTTP/1.1" 200 OK
WARNING: AI generation failed: API quota exceeded
```

## Production Considerations

1. **API Gateway**: Use Nginx or Kong to route `/api/ai/*` to Python service
2. **Load Balancing**: Deploy multiple Python AI instances
3. **Caching**: Cache AI-generated content to reduce API calls
4. **Fallbacks**: Handle AI service downtime gracefully
5. **Rate Limiting**: Protect Gemini API quota

## Next Steps

- [ ] Integrate AI description generation into Store creation
- [ ] Add AI tags when creating products
- [ ] Generate SEO metadata automatically
- [ ] Implement quality scoring for listings
- [ ] Add caching layer for AI responses

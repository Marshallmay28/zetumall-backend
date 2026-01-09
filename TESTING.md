# Testing the Store APIs

Quick guide to test the Store management endpoints.

## Prerequisites

1. **Start the Spring Boot application**:
   ```bash
   cd zetumall-backend
   gradlew.bat bootRun
   ```

2. **Get a Supabase JWT token**:
   - Log in to your Next.js frontend
   - Open browser DevTools → Application → Local Storage
   - Find the Supabase session (`sb-<project>-auth-token`)
   - Copy the `access_token` value

## API Endpoints

### 1. Health Check (No Auth Required)
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
    "database": "connected"
  }
}
```

### 2. Create Store
```bash
curl -X POST http://localhost:8080/api/stores \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_SUPABASE_JWT_TOKEN" \
  -d '{
    "name": "Test Store",
    "username": "teststore123",
    "description": "A test store for development",
    "email": "store@example.com",
    "contact": "+254712345678",
    "address": "Nairobi, Kenya",
    "category": "Electronics",
    "logo": "https://example.com/logo.png",
    "mpesaNumber": "254712345678"
  }'
```

Expected response:
```json
{
  "success": true,
  "message": "Store created successfully!",
  "data": {
    "id": "c1704835200abc123",
    "userId": "user-id",
    "name": "Test Store",
    "username": "teststore123",
    "status": "APPROVED",
    "isActive": true,
    ...
  }
}
```

### 3. Get My Store
```bash
curl -X GET http://localhost:8080/api/stores/me \
  -H "Authorization: Bearer YOUR_SUPABASE_JWT_TOKEN"
```

### 4. Get Store by ID
```bash
curl -X GET http://localhost:8080/api/stores/STORE_ID \
  -H "Authorization: Bearer YOUR_SUPABASE_JWT_TOKEN"
```

### 5. Update Store
```bash
curl -X PUT http://localhost:8080/api/stores/STORE_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_SUPABASE_JWT_TOKEN" \
  -d '{
    "name": "Updated Store Name",
    "description": "Updated description",
    "contact": "+254700000000"
  }'
```

### 6. List All Stores
```bash
# All stores
curl http://localhost:8080/api/stores

# Filter by status
curl "http://localhost:8080/api/stores?status=APPROVED"
```

## Using Thunder Client / Postman

1. **Import the collection** (create in Postman/Thunder Client):
   
   ```json
   {
     "name": "ZetuMall Core API",
     "baseUrl": "http://localhost:8080",
     "auth": {
       "type": "bearer",
       "token": "YOUR_SUPABASE_JWT_TOKEN"
     }
   }
   ```

2. **Add requests**:
   - GET `/api/health`
   - POST `/api/stores`
   - GET `/api/stores/me`
   - GET `/api/stores/:id`
   - PUT `/api/stores/:id`
   - GET `/api/stores`

## Testing Scenarios

### ✅ Successful Store Creation
1. Get valid Supabase JWT token
2. Send POST request with required fields
3. Verify response has `success: true`
4. Check store is created in database

### ❌ Authentication Error
1. Send request WITHOUT Authorization header
2. Expected: `401 Unauthorized`

### ❌ Duplicate Username
1. Create a store with username "teststore"
2. Try creating another store with same username
3. Expected: Error message "Username already taken"

### ❌ Admin Cannot Create Store
1. Log in as admin user
2. Try creating a store
3. Expected: Error message "Admins cannot create stores"

## Common Issues

### Issue: 401 Unauthorized
**Cause**: Invalid or missing JWT token  
**Solution**: 
- Ensure token is from Supabase Auth
- Check token hasn't expired
- Verify `SUPABASE_JWT_SECRET` in `.env` matches Supabase settings

### Issue: Database Connection Failed
**Cause**: DATABASE_URL incorrect  
**Solution**: 
- Check `.env` file
- Verify Supabase database is accessible
- Test with health endpoint first


### Issue: User Not Found
**Cause**: User doesn't exist in database yet  
**Solution**: 
- Ensure user is synced from Supabase Auth to database
- May need to create user record first (will implement sync later)

## Next Steps

After successful testing:
- [ ] Implement Product entities and APIs
- [ ] Add file upload for logos/banners (Supabase Storage)
- [ ] Integrate with Python AI service for store descriptions
- [ ] Add validation and error handling improvements

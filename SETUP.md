# ZetuMall Backend Setup Guide

## Prerequisites
- Java 17 or higher (check: `java -version`)
- Gradle is included in wrapper, no separate installation needed
- Access to Supabase database credentials

## Quick Setup

### Step 1: Configure Environment

1. Copy the environment template:
   ```bash
   copy .env.example .env
   ```

2. Fill in your Supabase credentials in `.env`:
   ```env
   DATABASE_URL=postgresql://postgres.YOUR_PROJECT:YOUR_PASSWORD@aws-1-eu-central-1.pooler.supabase.com:6543/postgres?pgbouncer=true
   
   SUPABASE_URL=https://YOUR_PROJECT.supabase.co
   SUPABASE_ANON_KEY=your-anon-key-here
   SUPABASE_JWT_SECRET=your-jwt-secret-here
   
   # Other settings can use defaults
   ```

### Step 2: Build the Project

Windows:
```bash
gradlew.bat build
```

Linux/Mac:
```bash
./gradlew build
```

### Step 3: Run the Application

Windows:
```bash
gradlew.bat bootRun
```

Linux/Mac:
```bash
./gradlew bootRun
```

The API will start on `http://localhost:8080`

### Step 4: Test the Setup

Open another terminal and test the health endpoint:

```bash
curl http://localhost:8080/api/health
```

You should see:
```json
{
  "success": true,
  "message": "Health check successful",
  "data": {
    "status": "UP",
    "service": "zetumall-core-api",
    "database": "connected"
  }
}
```

## Common Issues

### Issue: Database Connection Failed

**Solution**: Check your `DATABASE_URL` in `.env`:
- Ensure you're using the **connection pooling URL** (port 6543)
- Verify your password is correct
- Check if your Supabase project is active

### Issue: Build Failed

**Solution**: 
- Ensure Java 17+ is installed: `java -version`
- Clear Gradle cache: `gradlew clean`
- Try building again: `gradlew build --refresh-dependencies`

### Issue: Port 8080 Already in Use

**Solution**: Change the port in `.env`:
```env
SERVER_PORT=8081
```

## Development Workflow

1. Make code changes
2. Spring Boot DevTools will auto-restart the application
3. Test your changes
4. Commit and push

## Next Steps

After successful setup:
1. ✅ Test database connectivity
2. ⏭️ Create User entity
3. ⏭️ Create Store entity  
4. ⏭️ Implement Store APIs
5. ⏭️ Test with Postman/Thunder Client

## Getting Supabase Credentials

### DATABASE_URL
1. Go to Supabase Dashboard → Settings → Database
2. Copy "Connection string" → "Connection pooling"
3. Replace `[YOUR-PASSWORD]` with your actual password

### SUPABASE_URL
1. Go to Supabase Dashboard → Settings → API
2. Copy "Project URL"

### SUPABASE_ANON_KEY
1. Go to Supabase Dashboard → Settings → API
2. Copy "anon public" key

### SUPABASE_JWT_SECRET
1. Go to Supabase Dashboard → Settings → API
2. Scroll to "JWT Settings"
3. Copy "JWT Secret"

## Support

If you encounter any issues, check:
1. Logs in the terminal where you ran `gradlew bootRun`
2. The `application.yml` configuration
3. Your `.env` file for correct credentials

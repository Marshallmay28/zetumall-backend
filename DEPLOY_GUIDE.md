# 🚀 ZetuMall Deployment Guide (Railway + GitHub)

Your ZetuMall backend is split into two services for easier deployment and scaling:
1.  **Spring Boot Backend** (`zetumall-backend`) - The core e-commerce API.
2.  **Python AI Service** (`zetumall-ai-service`) - The AI features (Gemini).

## Step 1: Create GitHub Repositories
Since you don't have the GitHub CLI installed, you need to create two empty repositories manually on GitHub.

1.  Go to [github.com/new](https://github.com/new).
2.  Create a repository named **`zetumall-backend`**.
    *   Public or Private: Your choice.
    *   **Do not** initialize with README, .gitignore, or license (we have them).
3.  Create another repository named **`zetumall-ai-service`**.
    *   Same settings (Empty repo).

## Step 2: Push Code to GitHub
I have created a script to do this for you!

1.  Open **PowerShell** in the `ZetuMall` directory.
2.  Run the helper script: 
    ```powershell
    .\SETUP_GITHUB.ps1
    ```
3.  Paste the URL for `zetumall-backend` when asked.
4.  Paste the URL for `zetumall-ai-service` when asked.

The script will link your local code to GitHub and push everything.

## Step 3: Deploy to Railway

### 1. Spring Boot Backend
1.  Go to [Railway Dashboard](https://railway.app/dashboard).
2.  Click **"New Project"** -> **"Deploy from GitHub repo"**.
3.  Select **`zetumall-backend`**.
4.  Add Variables:
    *   `DATABASE_URL`: (Your Supabase connection string)
    *   `SUPABASE_URL`: (Your Supabase URL)
    *   `SUPABASE_ANON_KEY`: (Your Supabase Key)
    *   `MPESA_CONSUMER_KEY`, `MPESA_CONSUMER_SECRET`, etc.
5.  Click **Deploy**.

### 2. Python AI Service
1.  In the *same* Railway project, click **"New"** -> **"GitHub Repo"**.
2.  Select **`zetumall-ai-service`**.
3.  Add Variables:
    *   `GEMINI_API_KEY`: (Your Google Gemini Key)
    *   `SUPABASE_URL`, `SUPABASE_ANON_KEY`.
4.  Click **Deploy**.

### 3. Connect Them
1.  Get the **Public Domain** of the deployed Python service (e.g., `ai-service.up.railway.app`).
2.  Go to the **Spring Boot** service settings in Railway.
3.  Add/Update Variable: `AI_SERVICE_URL` = `https://ai-service.up.railway.app`
4.  Redeploy Spring Boot.

## Step 4: Update Frontend (Vercel/Netlify)
1.  Get the **Public Domain** of the Spring Boot service (e.g., `backend.up.railway.app`).
2.  Update your local `.env.local`:
    ```
    NEXT_PUBLIC_CORE_API_URL=https://backend.up.railway.app/api
    NEXT_PUBLIC_AI_API_URL=https://ai-service.up.railway.app/api/ai
    ```
3.  Deploy your Next.js frontend.

✅ **Done! Your hybrid microservices architecture is live.**

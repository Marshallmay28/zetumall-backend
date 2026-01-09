const http = require('http');

function makeRequest(path, options = {}) {
    return new Promise((resolve, reject) => {
        const req = http.request({
            hostname: 'localhost',
            port: 8080,
            path: path,
            method: 'GET',
            ...options
        }, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => resolve({ statusCode: res.statusCode, data: data }));
        });

        req.on('error', reject);
        req.end();
    });
}

async function verify() {
    console.log("🔍 Verifying Phase 2: Core Setup...");

    // 1. Test Database Connection via Health Endpoint
    try {
        console.log("👉 Testing Database Connection (Health Check)...");
        const health = await makeRequest('/api/health');
        if (health.statusCode === 200) {
            console.log("✅ Backend is UP");
            try {
                const json = JSON.parse(health.data);
                if (json.data && json.data.database === 'connected') {
                    console.log("✅ Database is CONNECTED");
                } else {
                    console.error("❌ Database status unknown:", json);
                }
            } catch (e) {
                console.warn("⚠️ Could not parse health response json");
            }
        } else {
            console.error(`❌ Health check failed with status ${health.statusCode}`);
        }
    } catch (e) {
        console.error("❌ Connection refused. Is the Spring Boot backend running on port 8080?");
        process.exit(1);
    }

    // 2. Test Authentication Security
    try {
        console.log("\n👉 Testing Authentication Security...");
        // Request a protected endpoint without token
        const protectedRes = await makeRequest('/api/stores/me');
        if (protectedRes.statusCode === 401 || protectedRes.statusCode === 403) {
            console.log("✅ Authentication Security is ACTIVE (Protected endpoint rejected unauthorized request)");
        } else {
            console.error(`❌ Authentication check failed. Expected 401/403, got ${protectedRes.statusCode}`);
        }
    } catch (e) {
        console.error("❌ Auth check request failed:", e.message);
    }

    console.log("\n🎉 Phase 2 Verification Complete.");
}

verify();

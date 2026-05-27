# Deploying to Render with Aiven MySQL

## 1. Create the Aiven MySQL database

1. Create an Aiven for MySQL service.
2. Open the service connection details.
3. Use these values for the Render backend environment:

```text
DB_URL=jdbc:mysql://<AIVEN_HOST>:<AIVEN_PORT>/<AIVEN_DATABASE>?sslMode=REQUIRED
DB_USERNAME=<AIVEN_USERNAME>
DB_PASSWORD=<AIVEN_PASSWORD>
```

For Aiven, the default database username is often `avnadmin`. Use the exact values from your Aiven dashboard.

## 2. Deploy the Spring Boot backend on Render

Create a new Render Web Service from this GitHub repository.

```text
Runtime: Docker
Dockerfile Path: ./Dockerfile
```

Add these Render environment variables:

```text
DB_URL=jdbc:mysql://<AIVEN_HOST>:<AIVEN_PORT>/<AIVEN_DATABASE>?sslMode=REQUIRED
DB_USERNAME=<AIVEN_USERNAME>
DB_PASSWORD=<AIVEN_PASSWORD>
JWT_SECRET=<long-random-secret>
Dev_gmail=<your-gmail-address>
Dev_password=<your-gmail-app-password>
CORS_ALLOWED_ORIGINS=http://localhost:5174,http://localhost:5173
OLLAMA_BASE_URL=<optional-external-ollama-url>
OLLAMA_MODEL=llama3.2
```

After the backend deploys, copy its Render URL, for example:

```text
https://hotel-management-system-api.onrender.com
```

## 3. Deploy the React frontend on Render

Create a new Render Static Site from the same GitHub repository.

```text
Build Command: npm install && npm run build
Publish Directory: dist
```

If the deployed page is blank and the page source contains this line, Render is serving the repository root instead of the production build:

```html
<script type="module" src="/src/frontend/main.jsx"></script>
```

Fix it by updating the frontend Static Site settings:

```text
Build Command: npm install && npm run build
Publish Directory: dist
```

Then click `Manual Deploy` -> `Clear build cache & deploy`.

Add this environment variable before building:

```text
VITE_API_BASE_URL=https://<YOUR_BACKEND_SERVICE>.onrender.com
```

After the frontend deploys, copy its Render URL, for example:

```text
https://hotel-management-system.onrender.com
```

## 4. Update backend CORS

Go back to the backend Web Service on Render and update:

```text
CORS_ALLOWED_ORIGINS=https://<YOUR_FRONTEND_SITE>.onrender.com,http://localhost:5174,http://localhost:5173
```

Redeploy the backend after changing this value.

## 5. Verify

1. Open the frontend Render URL.
2. Register or log in.
3. Check the browser Network tab if login fails.
4. Confirm the backend logs show a successful Aiven MySQL connection.

Note: `localhost` Ollama will not work from Render. Use an externally hosted Ollama server or keep the AI section limited to database-backed answers.

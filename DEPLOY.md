# Deploying E-Gatepass — Render (backend) + Vercel (frontend)

Deploy the backend first, then the frontend.

---

## Step 1 — Push to GitHub

Both `backend/` and `frontend/` need to be in a GitHub repo.
If they're not already:

```bash
git init
git add .
git commit -m "initial commit"
gh repo create gatepass --public --push   # or create manually on github.com
```

---

## Step 2 — Deploy the Backend on Render

### Option A — Using render.yaml (recommended)

1. Go to [render.com](https://render.com) → **New** → **Blueprint**
2. Connect your GitHub repo
3. Render detects `backend/render.yaml` and creates:
   - A **Web Service** (Spring Boot via Docker)
   - A **PostgreSQL** database
4. Click **Apply** — done, Render handles the rest

### Option B — Manual (if Blueprint doesn't work)

**Create the database first:**
1. Render Dashboard → **New** → **PostgreSQL**
2. Name: `gatepass-db` | Region: `Singapore` | Plan: `Free`
3. Save the **Internal Database URL** shown on the info page

**Create the web service:**
1. **New** → **Web Service** → connect your repo
2. Set **Root Directory** to `backend`
3. **Runtime**: Docker
4. **Dockerfile Path**: `./Dockerfile`
5. Add these **Environment Variables**:

| Key | Value |
|-----|-------|
| `DATABASE_URL` | *(paste the Internal Database URL from above)* |
| `DB_USERNAME` | *(from Postgres info page)* |
| `DB_PASSWORD` | *(from Postgres info page)* |
| `JWT_SECRET` | *(click "Generate" in Render — or any 32+ char string)* |
| `DDL_AUTO` | `update` |
| `PORT` | `8080` |
| `CORS_ALLOWED_ORIGINS` | `https://your-app.vercel.app` *(fill in after Step 3)* |

6. Click **Create Web Service**

### After the first successful deploy

Lock the schema so Hibernate can't accidentally alter your tables:

1. Render Dashboard → your web service → **Environment**
2. Change `DDL_AUTO` from `update` → `validate`
3. Click **Save Changes** (triggers a redeploy)

---

## Step 3 — Deploy the Frontend on Vercel

1. Go to [vercel.com](https://vercel.com) → **Add New Project**
2. Import your GitHub repo
3. Set **Root Directory** to `frontend`
4. Framework will be auto-detected as **Vite**
5. Add this **Environment Variable**:

| Key | Value |
|-----|-------|
| `VITE_API_URL` | `https://gatepass-backend.onrender.com/api` *(your Render service URL)* |

6. Click **Deploy**

Your app is live at the Vercel URL (e.g. `https://gatepass.vercel.app`).

---

## Step 4 — Wire up CORS

Now that you have the Vercel URL, go back to Render:

1. Your web service → **Environment**
2. Update `CORS_ALLOWED_ORIGINS` → your real Vercel URL, e.g. `https://gatepass.vercel.app`
3. Save (triggers redeploy)

---

## Step 5 — Verify

1. Open your Vercel URL
2. Log in with `admin@college.edu` / `Admin@123`
3. Demo data is seeded automatically on first boot

---

## Local development

Backend — needs a local Postgres (or use Docker):
```bash
docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=secret -e POSTGRES_DB=gatepass postgres:15-alpine

cd backend
export DATABASE_URL=jdbc:postgresql://localhost:5432/gatepass
export DB_USERNAME=postgres
export DB_PASSWORD=secret
export JWT_SECRET=local-dev-secret-32-chars-minimum
mvn spring-boot:run
```

Frontend:
```bash
cd frontend
echo "VITE_API_URL=http://localhost:8080/api" > .env.local
npm install
npm run dev
```

---

## Free tier notes

| Service | Free tier limits |
|---------|-----------------|
| Render Web Service | Spins down after 15 min inactivity; first request after sleep takes ~30s |
| Render PostgreSQL | 1GB storage, 90-day expiry on free plan — upgrade to $7/mo to keep it |
| Vercel | 100GB bandwidth/month, unlimited deploys — more than enough |

To prevent Render spin-down, set up an uptime monitor (e.g. [UptimeRobot](https://uptimerobot.com) pinging `/api/auth/health` every 10 minutes — free).

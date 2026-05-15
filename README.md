# 🛡️ E-Gatepass — Hostel Access Management System

A full-stack web application for managing student hostel gate passes with role-based access control, QR code verification, and a multi-stage approval workflow.

---

## ✨ Features

- **Multi-role system** — Student, Coordinator, Warden, Security, Admin
- **Approval workflow** — Student applies → Coordinator reviews → Warden approves → QR generated
- **QR code scanning** — Security scans students out and back in at the gate
- **Overdue tracking** — Automatically marks students overdue if not returned on time
- **Bulk approvals** — Coordinators and Wardens can approve multiple requests at once
- **Admin panel** — Full user management and system stats
- **JWT authentication** — Stateless, secure, role-protected routes
- **Responsive UI** — Works on desktop and mobile

---

## 🧑‍💼 Roles & Access

| Role | What they can do |
|------|-----------------|
| **Student** | Apply for a gatepass, view history, cancel pending requests, download QR code |
| **Coordinator** | Review and approve/reject pending student requests, bulk approve |
| **Warden** | Final approval/rejection, generates QR code on approval, view all gatepasses and stats |
| **Security** | Scan QR codes or enter gatepass numbers to log exit and return, view who is currently outside |
| **Admin** | Manage all users (create, enable/disable), view system-wide stats |

---

## 🗂️ Gatepass Lifecycle

```
Student applies
      │
      ▼
  PENDING ──────────────────────────────► CANCELLED (by student)
      │
      ▼
Coordinator reviews
      ├── Rejected ──► COORDINATOR_REJECTED
      └── Approved ──► COORDINATOR_APPROVED
                              │
                              ▼
                       Warden reviews
                              ├── Rejected ──► WARDEN_REJECTED
                              └── Approved ──► WARDEN_APPROVED + QR generated
                                                      │
                                                      ▼
                                              Security scans exit
                                                      │
                                                      ▼
                                                   ACTIVE
                                                      │
                                          ┌───────────┴───────────┐
                                          │                       │
                                    On time return          Past return time
                                          │                       │
                                          ▼                       ▼
                                      COMPLETED               OVERDUE
                                                                  │
                                                        Security scans return
                                                                  │
                                                                  ▼
                                                              COMPLETED
```

---

## 🛠️ Tech Stack

### Backend
- **Java 17** + **Spring Boot 3.2**
- **Spring Security** with JWT (JJWT 0.11)
- **Spring Data JPA** + **Hibernate**
- **PostgreSQL** (production) / H2 (local dev)
- **ZXing** for QR code generation
- **Lombok** for boilerplate reduction
- **Maven** build

### Frontend
- **React 19** + **Vite**
- **React Router v7** for client-side routing
- **Axios** with request/response interceptors
- **Lucide React** for icons
- **html5-qrcode** for camera-based QR scanning
- **CSS custom properties** for theming

---

## 🚀 Getting Started (Local)

### Prerequisites
- Java 17+
- Node.js 20+
- Docker (optional, for running Postgres locally)

### 1. Start PostgreSQL

```bash
docker run -d \
  -p 5432:5432 \
  -e POSTGRES_DB=gatepass \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=secret \
  postgres:15-alpine
```

Or point to any existing PostgreSQL instance.

### 2. Run the Backend

```bash
cd backend

export DATABASE_URL=jdbc:postgresql://localhost:5432/gatepass
export DB_USERNAME=postgres
export DB_PASSWORD=secret
export JWT_SECRET=local-dev-secret-key-at-least-32-characters

mvn spring-boot:run
```

The API starts at `http://localhost:8080/api`.  
On first run, demo users are seeded automatically.

### 3. Run the Frontend

```bash
cd frontend

echo "VITE_API_URL=http://localhost:8080/api" > .env.local

npm install
npm run dev
```

The app opens at `http://localhost:5173`.

---

## 🔑 Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@college.edu | Admin@123 |
| Warden | warden@college.edu | Warden@123 |
| Coordinator | coordinator@college.edu | Coord@123 |
| Security | security@college.edu | Security@123 |
| Student | student@college.edu | Student@123 |

---

## 🌐 Deployment

Deployed on **Render** (backend + PostgreSQL) and **Vercel** (frontend).

See [`DEPLOY.md`](./DEPLOY.md) for the full step-by-step guide.

### Quick overview

| Service | Platform | URL pattern |
|---------|----------|-------------|
| Spring Boot API | Render Web Service | `https://gatepass-backend.onrender.com` |
| PostgreSQL | Render Managed DB | internal to Render |
| React App | Vercel | `https://gatepass.vercel.app` |

### Key environment variables

**Backend (set in Render dashboard):**

| Variable | Description |
|----------|-------------|
| `DATABASE_URL` | PostgreSQL JDBC connection string |
| `DB_USERNAME` | Database user |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Secret key for signing JWTs (32+ chars) |
| `CORS_ALLOWED_ORIGINS` | Your Vercel frontend URL |
| `DDL_AUTO` | `update` on first deploy, then `validate` |

**Frontend (set in Vercel dashboard):**

| Variable | Description |
|----------|-------------|
| `VITE_API_URL` | Full URL to the backend API, e.g. `https://gatepass-backend.onrender.com/api` |

---

## 📁 Project Structure

```
project/
├── backend/
│   ├── src/main/java/com/gatepass/
│   │   ├── config/          # SecurityConfig, DataSeeder
│   │   ├── controller/      # Auth, Student, Coordinator, Warden, Security, Admin
│   │   ├── dto/             # AuthDTO, GatepassDTO
│   │   ├── entity/          # User, Gatepass, AuditLog
│   │   ├── enums/           # Role, GatepassStatus
│   │   ├── exception/       # GlobalExceptionHandler
│   │   ├── repository/      # JPA repositories
│   │   ├── security/        # JwtUtil, JwtAuthFilter, CustomUserDetailsService
│   │   ├── service/         # AuthService, GatepassService, UserService
│   │   └── util/            # QRCodeUtil
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── Dockerfile
│   └── render.yaml
│
└── frontend/
    ├── src/
    │   ├── context/         # AuthContext
    │   ├── pages/
    │   │   ├── admin/       # Admin Dashboard
    │   │   ├── coordinator/ # Coordinator Dashboard
    │   │   ├── security/    # QR Scanner
    │   │   ├── student/     # Student Dashboard
    │   │   └── warden/      # Warden Dashboard
    │   ├── services/        # api.js (Axios instance)
    │   ├── App.jsx          # Routes + role-based redirect
    │   └── main.jsx
    ├── vercel.json
    └── vite.config.js
```

---

## 🔒 Security Notes

- Passwords are hashed with **BCrypt** (cost factor 12)
- JWTs expire after **24 hours** by default
- All protected routes require a valid JWT — the role is embedded in the token
- The `/auth/me` and `/admin/users` endpoints never return the password field
- CORS is restricted to the configured frontend origin only

---

## 📄 License

MIT

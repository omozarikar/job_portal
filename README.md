# Job Portal Backend — Spring Boot Portfolio Project

A full-featured job portal REST API built with Spring Boot 3, Spring Security, JWT, and MySQL.  
Demonstrates: authentication, role-based access, file handling, complex DB relationships, email notifications, and Docker.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JWT (jjwt) |
| Database | MySQL 8 (JPA / Hibernate) |
| File Upload | Spring Multipart (local storage) |
| Email | Spring Mail (SMTP) |
| Documentation | Springdoc OpenAPI (Swagger UI) |
| Build | Maven |
| Deployment | Docker + Docker Compose |

---

## Roles

| Role | Permissions |
|---|---|
| `ROLE_CANDIDATE` | Register, search jobs, apply, upload resumes, track applications |
| `ROLE_EMPLOYER` | Post/edit/delete jobs, view applicants, update application status |
| `ROLE_ADMIN` | Full access: manage all users, jobs, view dashboard stats |

---

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8 (or Docker)

### Option 1 — Docker Compose (recommended)
```bash
docker-compose up --build
```
App at: http://localhost:8080  
Swagger UI: http://localhost:8080/swagger-ui.html

### Option 2 — Run directly
```bash
# Start MySQL, then:
mvn spring-boot:run
```

---

## API Endpoints Summary

### Auth  `/api/auth`
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/register` | Public | Register (candidate or employer) |
| POST | `/login` | Public | Login, returns JWT |
| GET | `/me` | Authenticated | Get own profile |
| PUT | `/profile` | Authenticated | Update profile |

### Jobs  `/api/jobs`
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/` | Public | List all active jobs |
| GET | `/search` | Public | Search with filters (keyword, location, type) |
| GET | `/{id}` | Public | Get job details |
| POST | `/` | Employer | Create job posting |
| PUT | `/{id}` | Employer | Edit job posting |
| DELETE | `/{id}` | Employer | Delete job posting |
| GET | `/my-jobs` | Employer | List own posted jobs |
| PATCH | `/{id}/toggle-status` | Employer | Activate/deactivate job |

### Applications  `/api/applications`
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/` | Candidate | Apply for a job |
| GET | `/my-applications` | Candidate | View own applications |
| DELETE | `/{id}/withdraw` | Candidate | Withdraw application |
| GET | `/job/{jobId}` | Employer | View applicants for a job |
| GET | `/employer` | Employer | All applications across own jobs |
| PATCH | `/{id}/status` | Employer | Update status (SHORTLISTED, REJECTED, etc.) |

### Resumes  `/api/resumes`
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/upload` | Authenticated | Upload resume (PDF/DOC, max 5MB) |
| GET | `/` | Authenticated | List own resumes |
| DELETE | `/{id}` | Authenticated | Delete a resume |
| PATCH | `/{id}/set-primary` | Authenticated | Set primary resume |
| GET | `/{id}/download` | Authenticated | Download a resume |

### Admin  `/api/admin`
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/dashboard` | Admin | Platform stats |
| GET | `/users` | Admin | List all users |
| PATCH | `/users/{id}/toggle-status` | Admin | Enable/disable user |
| DELETE | `/users/{id}` | Admin | Delete user |
| GET | `/jobs` | Admin | All jobs (including inactive) |
| DELETE | `/jobs/{id}` | Admin | Force delete any job |

---

## Sample Request — Register as Candidate

```json
POST /api/auth/register
{
  "fullName": "Rahul Sharma",
  "email": "rahul@example.com",
  "password": "Password@123",
  "role": "ROLE_CANDIDATE",
  "phone": "9876543210",
  "location": "Mumbai, India",
  "skills": "Java, Spring Boot, MySQL",
  "experience": "1 year",
  "education": "B.Tech CSE 2024"
}
```

## Sample Request — Post a Job (Employer)

```json
POST /api/jobs
Authorization: Bearer <employer-jwt-token>
{
  "title": "Java Backend Developer",
  "description": "Looking for a Java developer with Spring Boot experience...",
  "company": "TechCorp India",
  "location": "Bengaluru, India",
  "jobType": "FULL_TIME",
  "salaryMin": 400000,
  "salaryMax": 700000,
  "experienceRequired": "0-2 years",
  "skillsRequired": "Java, Spring Boot, MySQL, REST APIs",
  "applicationDeadline": "2024-12-31"
}
```

---

## Project Highlights (Explain in Interviews)

1. **JWT Authentication** — Stateless auth with `OncePerRequestFilter`; token validated on every request
2. **Role-based Access Control** — Method + URL-level security using `@EnableMethodSecurity` and `HttpSecurity` rules
3. **File Handling** — Multipart upload with validation (type, size), UUID-based filenames, per-user directories
4. **Async Email** — `@Async` + `@EnableAsync` so notifications don't block the HTTP response
5. **Global Exception Handling** — `@RestControllerAdvice` with typed exception classes returns consistent `ApiResponse<T>`
6. **JPA Auditing** — `@CreatedDate` / `@LastModifiedDate` auto-populated via `@EnableJpaAuditing`
7. **Docker** — Multi-stage Dockerfile + `docker-compose.yml` for one-command setup
8. **Swagger UI** — Full API documentation auto-generated at `/swagger-ui.html`

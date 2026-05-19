# Smart Disaster Response MIS

A role-based management information system for coordinating disaster response operations across reporting, resources, teams, hospitals, finance, approvals, and audits.

## Highlights
- **Role-based dashboards:** Admin, Operator, Field Officer, Warehouse Manager, Finance Officer
- **Secure API:** JWT auth + Spring Security RBAC
- **Operational workflows:** incident reporting, team deployment, resource allocation & dispatch
- **Health coordination:** hospital capacity, admissions, and discharge tracking
- **Financial control:** donations, expenses, approvals, and summaries
- **Governance:** approvals pipeline and audit logging with export
- **Analytics:** incident stats, resource utilization, and response-time insights

## Tech Stack
- **Backend:** Spring Boot, Spring Security, JPA (Hibernate), JJWT, SQL Server
- **Frontend:** React, React Router, Axios, Chart.js, Tailwind CSS
- **Docs:** OpenAPI/Swagger UI

## Core Modules
- **Auth & Users:** login/logout, admin-managed users
- **Emergency Reporting:** submit incidents, status updates, filters
- **Resource Management:** inventory, allocation requests, dispatch, restock
- **Rescue Teams:** assignment requests, status updates
- **Hospitals:** capacity, admissions, discharge flow
- **Finance:** donations/expenses, approvals, summaries
- **Approvals:** requests, review, history
- **Audit & Monitoring:** audit logs, exports, performance benchmark

## Quick Start
See `RUNNING.md` for full setup (database, configuration, backend, frontend).

## API Docs
Swagger UI is available at `http://localhost:8080/swagger-ui.html` (when the backend is running).

---
If you want a longer technical overview, see `project_description.md`.

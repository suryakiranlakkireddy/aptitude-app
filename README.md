# Aptitude App

Aptitude preparation app for **Government job exams** (SSC, Banking, Railways, State PSC style) and **IT/Software job placements**.

## Architecture

```
Flutter App (mobile)
      |
      | API requests
      v
Spring Boot (backend)
      |
      |-- User data
      |-- Quiz data
      |-- Progress
      v
PostgreSQL (database)

Supabase Storage
      |-- Profile images
      |-- PDFs / Notes
      |-- Thumbnails
      |-- Other files
```

## Modules / Scope

- **Roles**: Admin (content management via web admin panel) and User (student)
- **Content**: Quizzes, timed mock tests, notes/PDFs, analytics dashboards
- **Topics covered**: Quantitative Aptitude, Logical Reasoning, Verbal Ability, Data Interpretation, General Awareness/Current Affairs, Basic Computer Knowledge, CS fundamentals (OS/DBMS/OOP) for IT placements
- **Auth**: Email/password
- **Payments**: Course access priced at Rs 59, valid for **4 months**; requires repayment/renewal after expiry
- **Progress tracking**: Per-student isolated progress - topic-wise completion, attempt history, scores, accuracy, streaks, weak-area detection, performance trends over time

## Repository Structure

```
aptitude-app/
|-- backend/        # Spring Boot API (Java)
|-- frontend/        # Flutter mobile app
|-- admin-panel/    # Web admin panel for managing quizzes/content
|-- docs/           # Architecture docs, DB schema, API contracts
```

## Status

Project scaffolding stage. Backend, frontend, and admin panel implementations to follow.

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

## Repository Structure

```
aptitude-app/
|-- backend/        # Spring Boot API (Java 17, JPA, JWT auth)
|-- frontend/       # Flutter mobile app
|-- admin-panel/    # Plain HTML/JS web admin panel (no build step)
|-- docs/           # Requirements + DB schema
```

## Features implemented

- Email/password auth (JWT) with USER and ADMIN roles
- Topics, quizzes/mock tests, questions with options
- Quiz attempt submission with automatic scoring
- Per-student, isolated progress tracking (topic-wise accuracy, attempts, streaks)
- Subscription model: Rs 59 for 4 months, auto-expiry, renewal endpoint
- Admin panel: manage topics, quizzes, questions, notes; view users and dashboard stats
- Real Supabase Storage upload flow for notes: admin uploads a PDF (+ optional thumbnail)
  via `POST /api/admin/notes` (multipart, ADMIN only), which is proxied server-side to
  Supabase Storage using the service role key. `GET /api/notes?topicId=` and
  `GET /api/admin/notes?topicId=` never return a stored public URL - they generate a
  fresh, 1-hour signed URL on every request, so files aren't permanently link-shareable.

## Not yet wired up (marked as TODO in code)

- Real payment gateway (Razorpay/UPI) integration - currently `/api/subscription/purchase` records payment as successful directly
- Push notifications / streak reminders
- Flutter mobile app UI (`frontend/` currently only has `pubspec.yaml` - the screens haven't been built yet)

## Running the backend

```bash
cd backend
# set env vars or edit application.yml directly
export DB_URL=jdbc:postgresql://localhost:5432/aptitude_app
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=some-long-random-string
# needed for notes upload/download (Supabase Storage REST API)
export SUPABASE_URL=https://<project-ref>.supabase.co
export SUPABASE_SERVICE_KEY=<service_role key, NOT the anon key - dashboard > Project Settings > API>
export SUPABASE_BUCKET=aptitude-app-files
./mvnw spring-boot:run
```
API will be available at `http://localhost:8080/api`.

The `aptitude-app-files` bucket must exist as a **private** bucket in Supabase Storage
before notes can be uploaded (the backend never creates buckets, only objects inside
one). On the deployed environment this bucket has already been created.

To make the first admin account: register normally via `/api/auth/register`, then in Postgres run:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

## Running the Flutter app

```bash
cd frontend
flutter pub get
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080/api   # Android emulator
# or http://localhost:8080/api for iOS simulator / web
```

## Running the admin panel

It's static HTML/JS - no build step. Just open `admin-panel/index.html` in a browser,
or serve the folder:
```bash
cd admin-panel
python3 -m http.server 5500
```
Then visit `http://localhost:5500`. Log in with an ADMIN account.

## Database schema

See `docs/schema.sql`. Spring Boot's `ddl-auto: update` will create/update tables
automatically on startup, so manual schema application is optional.

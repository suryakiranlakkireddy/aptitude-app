# Requirements Summary

## Core Content
Full exam prep: quizzes + timed mock tests + notes/PDFs + analytics

## Auth
Email/password login

## Admin Panel
Web-based admin panel to add/manage quizzes, notes, and users

## Roles
- Admin: manage content, view aggregate/individual user analytics
- User (student): take quizzes/tests, view own progress only

## Subscription / Payments
- Price: Rs 59
- Duration: 4 months from purchase date
- After expiry: user must repay (renew) to regain access
- Needs a `subscriptions` table: purchased_at, expires_at, status (active/expired), payment history

## Topics (Govt + IT jobs)
- Quantitative Aptitude
- Logical Reasoning
- Verbal Ability
- Data Interpretation
- General Awareness / Current Affairs
- Basic Computer Knowledge
- CS Fundamentals (OS, DBMS, OOP) - for IT/placement-style tests

## Progress Tracking
- Topic-wise progress %
- Quiz/test attempt history (score, time taken, accuracy, date)
- Weak-area auto-flagging by topic
- Daily streaks
- Mock test performance trend graph
- Notes/PDF read status
- **Strictly per-student**: no user can see another user's data; admin sees aggregated + individual stats

## Storage (Supabase)
- Profile images
- PDFs / Notes
- Thumbnails
- Other files

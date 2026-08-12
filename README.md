# Lostify — Lost & Found Portal

A campus/community Lost & Found web app built with Spring Boot and Thymeleaf. People can report items they've lost or found, search and filter the board, get AI-assisted item descriptions, receive smart match suggestions, claim items, and get email alerts — all behind JWT-based authentication with role-based access control.

---

## Features

### Core
- **User accounts** — register and log in with email/password (BCrypt-hashed).
- **Report Lost / Report Found** — post an item with a name, description, location, date, contact info, and photo.
- **Dashboard** — browse all items with combinable filters: keyword (matches name *and* description), location, Lost/Found status, date range, and sort order (newest/oldest).
- **My Posts** — manage your own reports: edit, delete, view claims, or find matches, depending on the item.
- **Claim system** — request to claim a found item; the owner can view every claim filed against their post.

### JWT Authentication & Role-Based Access
- Stateless authentication via a signed JWT (carried in an HttpOnly cookie for the web pages, or an `Authorization: Bearer` header for API-style calls).
- Three roles: **USER**, **MODERATOR**, **ADMIN**.
  - Everyone who self-registers gets `USER` — roles are only ever escalated by an admin, never chosen at signup.
  - `MODERATOR`/`ADMIN` can delete any item, not just their own.
  - `ADMIN` gets an in-app **Admin Panel** to manage every user's role or remove accounts.

### AI Image Description (Spring AI + Gemini)
- On the report form, click **"Generate Description with AI"** after choosing a photo, and Gemini writes a short description of the item for you — fully editable before submitting.
- Fails gracefully: if no Gemini API key is configured, the button simply shows an error and the person can type their own description instead.

### Smart Lost Item Matching
- A rule-based scoring engine compares a lost item against every found item (and vice versa) on name, description, location, and date proximity, producing a 0–100% match score.
- From **My Posts**, a lost item shows a **"Find Matches"** button linking to a ranked list of likely found-item matches, each with a one-click **Claim** button.
- Runs entirely locally — no external API required, so it always works.

### Admin Verification System (found items)
Every reported found item moves through a fixed pipeline before it's visible to the public:

```
SUBMITTED → PENDING VERIFICATION → ADMIN REVIEW → APPROVED
```

- Only `APPROVED` found items appear on the dashboard, in search results, or in Smart Matching.
- Admins review the queue at **Admin Panel → Verify Items**, advancing each item one stage at a time.
- The original reporter can see their item's current stage on **My Posts**.

### Email Notifications
Sent automatically (asynchronously, so they never slow down the page) for:
- **A claim is filed** — the item's owner is emailed the claimant's name, message, and contact number.
- **A strong possible match appears** — whichever side's item is older gets emailed once a match scores 60%+. For found items, this only fires once the item is `APPROVED`, so nobody gets notified about an unverified report.

Uses Gmail SMTP. If mail isn't configured, the app still runs fine — notifications are just silently skipped.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Runtime | Java 17 |
| Framework | Spring Boot 3.5.15 |
| Web / Templating | Spring MVC, Thymeleaf, Bootstrap 5 |
| Data | Spring Data JPA (Hibernate), MySQL |
| Security | Spring Security 6, JWT (jjwt) |
| AI | Spring AI, Google Gemini (Google GenAI / Gemini Developer API) |
| Email | Spring Mail (Gmail SMTP) |
| Build | Maven (with the Maven Wrapper, `mvnw` / `mvnw.cmd`) |

---

## Prerequisites

- **Java 17+** JDK installed
- **MySQL** running locally (or reachable), with a database created for the app
- A **Gemini API key** (free) from [aistudio.google.com/apikey](https://aistudio.google.com/apikey) — optional, only needed for AI descriptions
- A **Gmail App Password** from [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords) — optional, only needed for email notifications

Maven itself does **not** need to be installed separately — the bundled wrapper downloads it automatically.

---

## Setup

1. **Create the database:**
   ```sql
   CREATE DATABASE lost_found_db;
   ```

2. **Set the required environment variables** (the app won't start without these two):
   ```
   setx DB_PASSWORD "your_mysql_password"
   setx JWT_SECRET "any_random_string_32+_characters_long"
   ```

3. **Set the optional environment variables** (the app runs fine without these — the related feature just quietly does nothing until configured):
   ```
   setx GEMINI_API_KEY "your_gemini_api_key"
   setx MAIL_USERNAME "your_gmail_address"
   setx MAIL_PASSWORD "your_gmail_app_password"
   ```

4. **Close and reopen your terminal** (and IDE, if you use one) so the new environment variables take effect.

5. **Run the app:**
   ```
   mvnw.cmd spring-boot:run
   ```
   (or `./mvnw spring-boot:run` on macOS/Linux)

6. Open **http://localhost:8081** in your browser.

---

## Environment Variables

| Variable | Required? | Purpose |
|---|---|---|
| `DB_USERNAME` | No (defaults to `root`) | MySQL username |
| `DB_PASSWORD` | **Yes** | MySQL password |
| `JWT_SECRET` | **Yes** | Signing key for auth tokens (32+ random characters) |
| `GEMINI_API_KEY` | No | Enables AI-generated item descriptions |
| `MAIL_USERNAME` | No | Gmail address used to send notification emails |
| `MAIL_PASSWORD` | No | Gmail App Password (not your normal login password) |

---

## Making Your First Admin

Nobody can register as ADMIN directly — every new signup is a plain USER. To promote yourself the first time, run this against your database after registering normally:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'your_email@example.com';
```

Then log out and back in so your session picks up the new role.

---

## Project Structure

```
src/main/java/com/lostfound/lostfoundportal/
├── config/          # Security, async, and web configuration
├── controller/       # MVC controllers (items, claims, admin, matching, auth)
├── dto/               # Lightweight data carriers (e.g. ItemMatch)
├── model/             # JPA entities (User, Item, ClaimRequest) and enums (Role, VerificationStatus)
├── repository/        # Spring Data JPA repositories
├── security/          # JWT service and authentication filter
└── service/           # Business logic (matching, verification, email, image AI, etc.)

src/main/resources/
├── templates/         # Thymeleaf views
├── static/css/        # Stylesheet
└── application.properties
```

---

## Notes

- Item photos are currently stored on the local filesystem (`uploads/`) and served from `/uploads/**`.
- Uses the maven wrapper — no separate Maven install required.
- Built as a learning/portfolio project; see inline comments in the service classes for the reasoning behind key design choices (e.g. why some environment variables are required and others are optional).

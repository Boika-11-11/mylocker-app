# HopeConnect

A private, invite-only file and document store built with Spring Boot, Spring Security and PostgreSQL.

**Live:** https://hopeconnect.up.railway.app

**Read-only demo:** `demo@hopeconnect.dev` / `Demo1234!`

---

## What it does

- Email-based authentication with invite-only access — no public registration
- Per-user file storage with nested folders
- Administrators invite users by email; the system sends a set-password link
- Three roles: administrator, standard user, and a read-only demo account

## Security

Each of these was implemented deliberately rather than inherited from a framework default.

| Concern | Approach |
|---|---|
| Password storage | Salted BCrypt hashes. Plain text never reaches the database or logs |
| Access control | Deny by default. Every route requires authentication unless explicitly opened |
| Broken object-level access | Ownership is a condition of the database query, not a separate check that can be skipped |
| Path traversal | Uploaded filenames are never used as disk paths; files are stored under generated UUIDs |
| CSRF | Tokens on every state-changing form, with SameSite and HttpOnly session cookies |
| Credential stuffing | Five failed sign-ins locks the account for fifteen minutes |
| Malicious uploads | Allow-list of permitted file types; web-executable formats refused |
| Account recovery | Single-use tokens expiring after 30 minutes; responses never reveal whether an address exists |
| Secrets | Read from environment variables. Nothing sensitive is committed |

### Two decisions worth explaining

**Ownership in the query, not in an `if`.**

```java
Optional<StoredFile> findByIdAndOwnerUsername(Long id, String ownerUsername);
```

The lookup cannot return another user's file, so no code path can forget to check. An ownership check written as a separate step only protects the places someone remembered to add it.

**Generated filenames.**

A user's filename is attacker-controlled text. Rather than sanitising it, the application never uses it as a path — files are written under a UUID and the original name is stored in the database for display. Path traversal becomes impossible rather than filtered.

## Stack

- Java 21, Spring Boot 4
- Spring Security — authentication, authorisation, CSRF, session management
- Spring Data JPA over PostgreSQL
- Thymeleaf for server-rendered views
- Deployed on Railway with HTTPS and a persistent volume

## Running locally

Requires Java 21 and PostgreSQL.

1. Create a database named `lockerdb`
2. Create `src/main/resources/application-local.properties` (git-ignored):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/lockerdb
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD

mylocker.admin.email-login=you@example.com
mylocker.admin.username=YourName
mylocker.admin.password=YOUR_ADMIN_PASSWORD

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=you@example.com
spring.mail.password=YOUR_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
mylocker.admin.email=you@example.com
```

3. Run `MylockerApplication`. The schema and administrator account are created on first startup.

## Known limitations

- Uploaded files are stored on the application's own volume rather than object storage
- No audit log of administrative actions
- Session state and rate-limit counters are held in memory, so they reset on restart and would need a shared store across multiple instances

## Author

Boikanyo Sere — software quality assurance and technical support.
ISTQB Foundation Level and Test Automation Engineer certified.
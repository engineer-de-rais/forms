# Forms Backend

Initial backend scaffold for Google-Forms-like MVP.

## Run locally

```bash
./gradlew bootRun
```

Environment variables:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- `JWT_SECRET`, `JWT_TTL_SECONDS`

## API docs (OpenAPI)

After startup:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Current API

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/forms` (requires header `X-User-Id`)
- `GET /api/forms` (requires header `X-User-Id`)
- `GET /api/forms/{formId}`
- `PATCH /api/forms/{formId}`
- `POST /api/forms/{formId}/publish`
- `POST /api/forms/{formId}/unpublish`

# Forms Backend

Spring Boot backend for a Google-Forms-like MVP.

## Run locally

```bash
gradle bootRun
```

Environment variables:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- `JWT_SECRET`, `JWT_TTL_SECONDS`

## OpenAPI

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Main endpoints

Owner endpoints (`X-User-Id` header):
- `POST /api/forms`
- `GET /api/forms`
- `GET /api/forms/{formId}`
- `PATCH /api/forms/{formId}`
- `POST /api/forms/{formId}/publish`
- `POST /api/forms/{formId}/unpublish`
- `GET /api/forms/{formId}/structure`
- `PUT /api/forms/{formId}/structure`
- `GET /api/forms/{formId}/results`
- `GET /api/forms/{formId}/responses`
- `GET /api/forms/{formId}/responses/{submissionId}`
- `GET /api/forms/{formId}/responses.csv`

Public endpoints:
- `GET /api/public/forms/{slug}`
- `POST /api/public/forms/{slug}/responses`

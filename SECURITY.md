# Security

## Reporting a vulnerability

If you find a security vulnerability, do **NOT** report it publicly in Issues.

Send an email to: contact@millete.online

We will respond within 48 hours.

## Supported versions

| Version | Supported |
|---------|:---------:|
| 0.0.x   | ✅        |

## Best practices

- Always use `application-prod.yml` in production
- Enable Jasypt to encrypt sensitive credentials
- Generate a strong JWT_SECRET with `openssl rand -base64 64`
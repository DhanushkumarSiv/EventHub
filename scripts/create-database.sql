-- Run as a PostgreSQL superuser (typically `postgres`).
-- psql -h localhost -U postgres -f scripts/create-database.sql

SELECT 'CREATE DATABASE eventhub'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'eventhub')\gexec

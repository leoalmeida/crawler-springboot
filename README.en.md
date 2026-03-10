# crawler-springboot

[Português](README.md) | [English](README.en.md)

![Java](https://img.shields.io/badge/java-17-orange)
![Spring Boot](https://img.shields.io/badge/spring--boot-3.5.x-6DB33F)
![Status](https://img.shields.io/badge/status-active-brightgreen)

Asynchronous web crawler built with Spring Boot, REST API, and JPA/H2 persistence for job tracking and results.

## Table of Contents

- [Overview](#overview)
- [Requirements](#requirements)
- [Configuration](#configuration)
- [How to Run](#how-to-run)
- [API](#api)
- [Tests and Quality](#tests-and-quality)
- [Docker](#docker)
- [Structure](#structure)
- [Troubleshooting](#troubleshooting)

## Overview

- main endpoint under `/crawl`
- asynchronous crawling execution
- iterative crawling strategy for links
- in-memory H2 database for local runs

## Requirements

- JDK 17+
- Maven 3.9+ (or `mvnw`)
- Docker (optional)

## Configuration

`application.properties` expects these environment variables:

- `CRAWLER_URL` (required): target base URL to crawl
- `DATABASE_DB` (required): in-memory H2 database name
- `DATABASE_USER` (required): database username
- `DATABASE_PWD` (required): database password

PowerShell example:

```powershell
$env:CRAWLER_URL="https://example.com"
$env:DATABASE_DB="crawlerdb"
$env:DATABASE_USER="sa"
$env:DATABASE_PWD=""
```

## How to Run

With Maven Wrapper:

```powershell
Set-Location "c:\Users\leo_a\projetos\crawler-springboot"
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

Application runs at `http://localhost:8081`.

## API

### `POST /crawl`

Creates a new job.

Request:

```json
{
  "keyword": "spring boot"
}
```

Response `201`:

```json
{
  "id": "aBcDeF12"
}
```

### `GET /crawl/{id}`

Returns status and collected URLs for a single job.

### `GET /crawl`

Lists all submitted jobs.

## Tests and Quality

Run unit + integration tests:

```powershell
.\mvnw.cmd test
```

Run full validation pipeline:

```powershell
.\mvnw.cmd verify
```

Format code:

```powershell
.\mvnw.cmd spotless:apply
```

## Docker

Using `docker compose`:

```powershell
docker compose up --build
```

`docker-compose.yaml` reads values from `.env`.

Direct Dockerfile usage:

```powershell
docker build -t crawler-springboot .
docker run --rm -p 8081:8081 -e CRAWLER_URL="https://example.com" -e DATABASE_DB="crawlerdb" -e DATABASE_USER="sa" -e DATABASE_PWD="" crawler-springboot
```

## Structure

```text
crawler-springboot/
  src/main/java/space/lasf/crawler_app/
    component/
    controller/
    dto/
    entity/
    handler/
    mapper/
    repository/
    service/
  src/test/java/space/lasf/crawler_app/
    integration/
    unit/
  src/main/resources/application.properties
  docker-compose.yaml
  Dockerfile
```

## Troubleshooting

- Startup fails due to unresolved placeholders: ensure `CRAWLER_URL`, `DATABASE_DB`, `DATABASE_USER`, `DATABASE_PWD` are set.
- Port `8081` already in use: change `server.port` in `application.properties`.
- Context/test failures: run `.\mvnw.cmd clean test`.

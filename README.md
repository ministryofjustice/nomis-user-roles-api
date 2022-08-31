# NOMIS User Roles API

[![CircleCI](https://circleci.com/gh/ministryofjustice/court-register/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/nomis-user-roles-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://nomis-user-dev.aks-dev-1.studio-hosting.service.justice.gov.uk/swagger-ui/index.html?configUrl=/v3/api-docs)

Self-contained fat-jar micro-service to interact with users in the NOMIS database

## Building

```./gradlew build```

## Running

Various methods to run the application locally are detailed below.

Once up the application should be available on port 8101 - see the health page at http://localhost:8101/health.  

Also try http://localhost:8101/swagger-ui.html to see the API specification.
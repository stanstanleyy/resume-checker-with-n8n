# ResumeChecker

ResumeChecker is a Spring Boot web application that evaluates a candidate resume against a target job description using an n8n webhook + LLM pipeline.

Users upload a PDF resume and paste a job description. The app sends both to n8n, receives structured analysis, and renders a score-driven results page.

## Features

- Resume upload (PDF) and job description input from web UI
- Resume file is converted to Base64 before outbound request
- Integration with an n8n webhook endpoint for AI analysis
- Structured result rendering (match score, strengths, gaps, skills, recommendation)
- Friendly error handling for upstream model or webhook limits/failures

## Tech Stack

- Java 17
- Spring Boot 4.0.6
- Spring MVC + Thymeleaf server-side rendering
- Jackson (JSON + Java Time handling)
- Maven Wrapper (`mvnw`, `mvnw.cmd`)
- Tailwind CSS (CDN) in template pages

## Project Structure

```text
src/
  main/
    java/com/narra/lab/resumechecker/
      ResumecheckerApplication.java
      controllers/
        HomeController.java
        ResultController.java
      dto/
        CandidateAnalysisDTO.java
    resources/
      application.properties
      templates/
        home.html
        result.html
pom.xml
```

## Application Flow

1. `GET /` loads `home.html`.
2. User submits form to `POST /check` with:
   - `resume` (multipart PDF)
   - `jobDescription` (text)
3. Controller converts resume bytes to Base64 and sends JSON to n8n webhook.
4. n8n returns analysis JSON that maps to `CandidateAnalysisDTO`.
5. DTO is stored as flash attribute and redirected to `GET /result`.
6. `result.html` displays score, level, strengths, gaps, recommendation, and key skills.

## API Contract with n8n

### Request sent by this app

`POST {n8nWebhookUrl}` with `Content-Type: application/json`

```json
{
  "resumeBase64": "<base64-pdf-content>",
  "jobDescription": "<job-description-text>",
  "fileName": "<uploaded-file-name>"
}
```

### Expected response shape

The response body should be valid JSON (or stringified JSON that can be decoded into JSON) matching:

```json
{
  "candidateName": "John Doe",
  "matchScore": 84,
  "matchLevel": "Good",
  "strengths": ["..."],
  "gaps": ["..."],
  "recommendation": "1. ... 2. ... 3. ...",
  "keySkills": ["Java", "Spring Boot", "REST APIs"],
  "analyzedAt": "2026-04-25T03:50:00+08:00"
}
```

## Dependencies Used

From `pom.xml`:

- `org.springframework.boot:spring-boot-starter-thymeleaf`
- `org.springframework.boot:spring-boot-starter-webmvc`
- `com.fasterxml.jackson.datatype:jackson-datatype-jsr310`
- `com.fasterxml.jackson.core:jackson-databind`
- `com.fasterxml.jackson.core:jackson-annotations`
- `com.fasterxml.jackson.core:jackson-core`
- `org.springframework.boot:spring-boot-devtools` (runtime)
- `org.springframework.boot:spring-boot-starter-thymeleaf-test` (test)
- `org.springframework.boot:spring-boot-starter-webmvc-test` (test)

Build plugin:

- `org.springframework.boot:spring-boot-maven-plugin`

## n8n Workflow Design

This project currently calls:

`https://stanstanley.app.n8n.cloud/webhook-test/checkResume`

You can build an n8n workflow with the following node sequence.

### Recommended nodes

1. **Webhook** (`POST /checkResume`)
2. **Function** (validate input fields: `resumeBase64`, `jobDescription`)
3. **Move Binary Data / Function** (decode Base64 resume if needed)
4. **PDF extraction node** (extract resume text)
5. **LLM node** (compare resume text vs job description)
6. **Function** (normalize model output to strict JSON contract)
7. **Respond to Webhook** (return final JSON)

### Logic notes

- Validate required inputs early and return `400` if missing.
- Keep model prompt deterministic to reduce schema drift.
- Always coerce `matchScore` into `0-100` integer range.
- Ensure `analyzedAt` is ISO-8601 offset datetime.
- Return empty arrays (`[]`) for `strengths`, `gaps`, `keySkills` instead of `null`.

### Response guardrails

The Spring app expects:

- `matchScore > 0` to allow access to `/result`
- fields compatible with `CandidateAnalysisDTO` data types
- content type `application/json`

If your model occasionally returns markdown or malformed JSON, add a parser/repair Function node before `Respond to Webhook`.

## Configuration

### Change webhook URL

In `HomeController`, update:

- `n8nUrl` string in the `handleFileUpload(...)` method

For production, prefer moving this value into `application.properties` and reading it via `@Value`.

## Run Locally

### Requirements

- Java 17+
- Internet access to reach n8n webhook endpoint

### Start app

Windows:

```bash
./mvnw.cmd spring-boot:run
```

Mac/Linux:

```bash
./mvnw spring-boot:run
```

Then open:

`http://localhost:8080`

## Testing

Run tests with:

```bash
./mvnw test
```

## Troubleshooting

- If analysis fails, verify webhook URL and n8n workflow activation.
- If results page redirects to home, check that response includes valid `matchScore`.
- If datetime parsing fails, verify `analyzedAt` is ISO offset datetime.
- If model quota is exceeded, retry later or switch model/provider in n8n.

# Spring Boot Web Crawler

A simple RESTful web crawler application built with Spring Boot. It accepts a keyword, asynchronously crawls a target website, and returns a list of internal URLs where the keyword was found.

## Features

- **Asynchronous Crawling**: Submitting a crawl request immediately returns a unique ID, while the crawling process runs in the background.
- **RESTful API**: A clean API to create, monitor, and retrieve crawl jobs.
- **Iterative Crawling**: Uses a breadth-first search (BFS) approach with a queue to prevent `StackOverflowError` on websites with deep link structures.
- **Resource Filtering**: Ignores common file types (e.g., `.css`, `.js`, `.jpg`, `.pdf`) to focus on HTML content.
- **Robust Testing**: Includes a comprehensive suite of unit and integration tests for all layers (Controller, Service, Handler, Repository).
- **Global Exception Handling**: Provides consistent and meaningful JSON error responses for invalid requests.

## Technologies Used

- **Java 17**
- **Spring Boot 3**
- **Spring Data JPA**
- **H2 Database** (In-memory, for demonstration)
- **Maven**
- **Jsoup** (For HTML parsing)
- **Lombok**

---

## API Endpoints

The API provides endpoints for creating and querying crawl requests.

### 1. Create a Crawl Request

Starts a new asynchronous crawl job for the given keyword.

- **Endpoint**: `POST /crawl`
- **Request Body**:
  ```json
  {
    "keyword": "spring boot"
  }
  ```
- **Success Response (201 Created)**:
  ```json
  {
    "id": "aBcDeF12"
  }
  ```
- **Error Response (400 Bad Request)**: If the keyword is missing or invalid (must be between 4 and 32 characters).

### 2. Get Crawl Status and Results

Retrieves the status and results of a specific crawl job by its ID.

- **Endpoint**: `GET /crawl/{id}`
- **URL Parameter**: `id` (The unique ID returned from the POST request).
- **Success Response (200 OK)**:
  ```json
  {
    "id": "aBcDeF12",
    "status": "DONE",
    "urls": [
      "https://example.com/page-with-keyword",
      "https://example.com/another/page"
    ]
  }
  ```
  *The `status` can be `ACTIVE` (still running) or `DONE` (completed).*

- **Error Response (404 Not Found)**: If no job with the given ID exists.

### 3. Get All Crawl Requests

Returns a list of all crawl jobs that have been submitted.

- **Endpoint**: `GET /crawl`
- **Success Response (200 OK)**:
  ```json
  [
    {
      "id": "aBcDeF12",
      "status": "DONE",
      "urls": ["..."]
    },
    {
      "id": "xYz123Ab",
      "status": "ACTIVE",
      "urls": []
    }
  ]
  ```

---

## Getting Started

### Prerequisites

- JDK 17 or later
- Apache Maven 3.9 or later

### Configuration

The target website to be crawled is configured in `src/main/resources/application.properties`. You must set the `crawler.url` property:

```properties
crawler.url=https://example.com
```

### Build and Run

1. **Clone the repository:**
   ```sh
   git clone <repository-url>
   cd crawler-springboot
   ```

2. **Build the project using Maven:**
   ```sh
   mvn clean install
   ```

3. **Run the application:**
   ```sh
   java -jar target/crawler-app-0.0.1-SNAPSHOT.jar
   ```

The application will start on `http://localhost:8080`.

## Running the Tests

To run the complete test suite (unit and integration tests), execute the following command from the project root:

```sh
mvn test
```
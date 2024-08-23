# TimeTracker Application

This project is a simple time-tracking application that allows users to record, view, and 
manage their time entries. The application is built using Java, Spring Boot, Thymeleaf 
for the frontend, and includes a REST API for handling time records.

## Features

- Record and track time entries with start and end times.
- Filter and search time records by email.
- Handle timezones and display records in the user's local time.

## Getting Started

Follow the instructions below to set up the project locally.

### Prerequisites

- Docker
- Docker Compose

### Installation

1. Clone the repository.
2. Build and run the application using Docker Compose.

```bash
docker compose up --build
```

The application will start running at `http://localhost:8080`.

### Docker Services

- **timetracker-frontend**: The frontend service.
- **timetracker-backend**: The backend service.

## Usage

Access the application at `http://localhost:8080`.

### Frontend Interface
**Homepage**: Navigate to the homepage to access the time-tracking features:

- **View Records**:
    - Enter the email address in the search field.
    - Click the "Search" button.
    - View the matching records listed. *(Note: The application automatically detects your timezone and displays the records in your local time.)*


- **Create a New Record**:
    - Enter the email address, start time, and end time in the respective fields.
    - Click the "Create" button to add the new record. *(Note: The application automatically detects your timezone for accurate record entry.)*
    - If the record is created successfully, a success message "Record created successfully" will be displayed.

## Future Enhancements:

- **Improve User Feedback on Errors**:
    - Enhance error messages to provide clearer and more actionable feedback when issues occur.


- **Enhance Record Visualization**:
    - Refine the display of records to improve usability and aesthetics. Consider features like sorting, filtering, or better pagination instead of an infinite scroll. The current setup uses a "Load More" button to retrieve additional records.








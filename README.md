# Collaborative Text Annotation Platform with Quality Control

A robust web-based platform for collaborative text annotation with built-in quality control mechanisms. This application enables efficient management of annotation tasks, annotator assignments, and comprehensive analytics for text classification projects.

## Overview

This platform facilitates the annotation of text data by multiple annotators, providing administrators with tools to manage datasets, assign tasks, monitor progress, and analyze annotation quality. It's built with Spring Boot and uses modern web technologies to deliver a responsive and intuitive user experience.

## Features

### For Administrators
- **Dataset Management**: Upload, view, and manage text datasets
- **Annotator Management**: Add, remove, and monitor annotator performance
- **Task Assignment**: Assign specific datasets to annotators with deadlines
- **Quality Control**: Track annotation consistency and identify discrepancies
- **Statistics & Analytics**: View comprehensive statistics on annotation progress and quality
- **Export Functionality**: Export annotation results for further analysis

### For Annotators
- **Task Dashboard**: View assigned annotation tasks and deadlines
- **Annotation Interface**: User-friendly interface for text classification
- **Progress Tracking**: Monitor personal annotation progress
- **Task Management**: Organize and prioritize assigned tasks

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.4.5
- **Database**: MySQL
- **Frontend**: Thymeleaf, TailwindCSS, JavaScript
- **Security**: Spring Security
- **Build Tool**: Maven
- **Additional Libraries**:
  - Lombok for reducing boilerplate code
  - Apache POI for Excel file processing
  - Spring Mail for email notifications

## Project Structure

The application follows a standard Spring Boot architecture:

- **Controller Layer**: Handles HTTP requests and manages view rendering
- **Service Layer**: Contains business logic
- **Repository Layer**: Manages data access
- **Entity Layer**: Defines domain models
- **Configuration**: Contains application configuration

### Key Components

- **Datasets**: Collections of text pairs to be annotated
- **Tasks**: Assignments of datasets to annotators with deadlines
- **Annotations**: Individual classifications made by annotators
- **Users**: System users with different roles (Admin, Annotator)

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- Node.js and npm (for frontend build)

### Installation

1. Clone the repository:
   ```
   git clone https://github.com/faiz-oussama/Collaborative-Text-Annotation-Platform-with-Quality-Control.git
   ```

2. Configure the database connection in `src/main/resources/application.properties`

3. Build the application:
   ```
   mvn clean install
   ```

4. Run the application:
   ```
   mvn spring-boot:run
   ```

5. Access the application at `http://localhost:8080`

## Usage

### Administrator Workflow

1. **Login** with administrator credentials
2. **Create datasets** by uploading text files and defining possible classification labels
3. **Assign tasks** to annotators with appropriate deadlines
4. **Monitor progress** through the statistics dashboard
5. **Export results** when annotation tasks are complete

### Annotator Workflow

1. **Login** with annotator credentials
2. **View assigned tasks** on the dashboard
3. **Select a task** to begin annotation
4. **Classify text** according to the provided labels
5. **Track progress** of assigned tasks

## Security

The application implements Spring Security for authentication and authorization:
- Role-based access control (Admin, Annotator)
- Secure password storage with encryption
- Protected endpoints based on user roles

## Contributing

Contributions to improve the platform are welcome. Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Spring Boot team for the excellent framework
- All contributors who have helped improve this platform

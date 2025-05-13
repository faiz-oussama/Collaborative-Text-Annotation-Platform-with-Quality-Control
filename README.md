<div align="center">

# 🔍 Collaborative Text Annotation Platform

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)

**A modern platform for collaborative text annotation with advanced quality control & real-time analytics**

[Features](#-features) • 
[Technology](#-technology-stack) • 
[Installation](#-installation) • 
[Usage](#-usage) • 
[Architecture](#-architecture) • 
[Quality Control](#-quality-control) • 
[Contributing](#-contributing)

<img src="https://via.placeholder.com/800x400?text=Annotation+Platform+Screenshot" alt="Platform Screenshot" width="80%">

</div>

## ✨ Overview

This platform revolutionizes text annotation workflows by providing a comprehensive environment for collaborative annotation with built-in quality control mechanisms. Designed for research teams, academic institutions, and companies working with NLP and machine learning datasets, it streamlines the entire annotation lifecycle from task assignment to quality assessment.

## 🚀 Features

### 💼 For Administrators

- **Dataset Management**
  - Upload, categorize, and manage text datasets
  - Support for multiple file formats including CSV, Excel, and JSON
  - Dataset splitting and sampling capabilities

- **Annotator Management**
  - Add, remove, and monitor annotator performance metrics
  - Qualification tests for new annotators
  - Role-based permission system

- **Advanced Assignment System**
  - Assign specific datasets to annotators with deadlines
  - Balanced workload distribution algorithms
  - Priority-based task scheduling

- **Quality Control Engine**
  - Automated spam detection for low-quality annotations
  - Inter-annotator agreement metrics (Cohen's Kappa, Krippendorff's Alpha)
  - Gold standard comparison for annotator evaluation
  - Model training for quality prediction

- **Comprehensive Analytics**
  - Real-time dashboards for annotation progress
  - Detailed statistics on annotator performance and dataset characteristics
  - Exportable reports for further analysis

### 👤 For Annotators

- **Intuitive Dashboard**
  - Clear overview of assigned tasks and deadlines
  - Progress tracking and time management tools
  - Notification system for new assignments and updates

- **Streamlined Annotation Interface**
  - User-friendly text classification workflow
  - Keyboard shortcuts for efficient annotation
  - Context preservation between annotation sessions

- **Performance Insights**
  - Personal statistics on annotation speed and consistency
  - Comparison with anonymized team averages
  - Suggestions for improvement based on automated analysis

## 💻 Technology Stack

### Backend
- **Language & Framework**: Java 17, Spring Boot 3.4.5
- **Database**: MySQL 8.0 with Hibernate ORM
- **Security**: Spring Security with JWT authentication
- **API Documentation**: Springdoc OpenAPI 3.0
- **Testing**: JUnit 5, Mockito, Spring Test

### Frontend
- **Template Engine**: Thymeleaf with layout dialect
- **Styling**: TailwindCSS, Custom design system
- **Interactivity**: JavaScript with Alpine.js
- **Data Visualization**: Chart.js, D3.js

### Quality Control Components
- **NLP Processing**: Python with spaCy, NLTK
- **Machine Learning**: Scikit-learn, PyTorch
- **Integration**: Spring's ProcessBuilder for Python script execution

### DevOps & Tools
- **Build**: Maven with multi-profile configuration
- **CI/CD**: Jenkins pipeline (optional GitHub Actions)
- **Monitoring**: Spring Actuator, Prometheus (optional)
- **Additional Libraries**:
  - Lombok for boilerplate reduction
  - MapStruct for object mapping
  - Apache POI for Excel processing
  - Spring Mail for notifications

## 🏗️ Architecture

The application follows a modern, modular architecture:

```
src/
├── main/
│   ├── java/
│   │   └── com/annotations/demo/
│   │       ├── configuration/  # App configuration
│   │       ├── controller/     # Web & REST controllers
│   │       ├── dto/            # Data transfer objects
│   │       ├── entity/         # Domain models
│   │       ├── exception/      # Custom exceptions
│   │       ├── repository/     # Data access layer
│   │       ├── security/       # Authentication & authorization
│   │       ├── service/        # Business logic
│   │       │   ├── impl/       # Service implementations
│   │       │   └── interfaces/ # Service contracts
│   │       └── util/           # Helper classes
│   ├── resources/
│   │   ├── static/             # CSS, JS, images
│   │   ├── templates/          # Thymeleaf templates
│   │   ├── python/             # ML & NLP scripts
│   │   └── application.properties
└── test/                       # Test cases
```

### Key Components

- **Datasets**: Collections of text samples with metadata
- **Tasks**: Assignment units linking annotators, datasets and deadlines
- **Annotations**: Individual text classifications with confidence levels
- **Users**: System users with role-based permissions
- **Quality Control**: Metrics, detection algorithms and model training

## 🔧 Installation

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Python 3.8+ (for ML components)
- Node.js and npm (for frontend assets)

### Setup Process

1. **Clone the repository**
   ```bash
   git clone https://github.com/faiz-oussama/Collaborative-Text-Annotation-Platform-with-Quality-Control.git
   cd Collaborative-Text-Annotation-Platform-with-Quality-Control
   ```

2. **Configure database connection**
   ```properties
   # src/main/resources/application.properties
   spring.datasource.url=jdbc:mysql://localhost:3306/annotation_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Install Python dependencies** (optional, for ML features)
   ```bash
   pip install -r src/main/resources/python/requirements.txt
   ```

4. **Build the application**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

6. **Access the platform**
   - Main application: [http://localhost:8080](http://localhost:8080)
   - API documentation: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## 📋 Usage

### Administrator Workflow

1. **Access Control Panel**
   - Login with administrator credentials
   - Navigate to the admin dashboard

2. **Dataset Management**
   - Upload new datasets
   - Define annotation labels and guidelines
   - Create annotation task templates

3. **Task Assignment**
   - Allocate datasets to qualified annotators
   - Set deadlines and priority levels
   - Configure quality control thresholds

4. **Quality Monitoring**
   - Track real-time annotation progress
   - Review inter-annotator agreement scores
   - Identify potential spam or low-quality submissions
   - Run model training for quality prediction

5. **Results Management**
   - Export annotated datasets in multiple formats
   - Generate performance reports
   - Analyze quality metrics and trends

### Annotator Workflow

1. **Task Management**
   - Log in to the annotator portal
   - Review assigned tasks and priorities
   - Access annotation guidelines

2. **Annotation Process**
   - Select an active task
   - Classify text samples according to guidelines
   - Provide confidence levels for ambiguous cases
   - Submit completed batches

3. **Performance Review**
   - Monitor personal statistics
   - Review feedback on completed tasks
   - Track productivity and quality metrics

## 🛡️ Quality Control

The platform implements a multi-layered quality control system:

### Preventative Measures
- Comprehensive annotation guidelines
- Training modules for annotators
- Qualification tests before task assignment

### Real-time Monitoring
- Progress tracking and time-per-annotation metrics
- Pattern recognition for suspicious behavior
- Confidence score analysis

### Evaluation Metrics
- Inter-annotator agreement calculations
- Gold standard comparisons
- Statistical anomaly detection

### Advanced Features
- Machine learning models for quality prediction
- Spam detection algorithms
- Automated feedback generation

## 🔒 Security

- **Authentication**: Spring Security with customizable password policies
- **Authorization**: Fine-grained permission system with role hierarchies
- **Data Protection**: Encrypted sensitive information
- **API Security**: CSRF protection, rate limiting, and JWT validation

## 🤝 Contributing

We welcome contributions to improve this platform! Here's how to get started:

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/amazing-enhancement`
3. Commit your changes: `git commit -m 'Add some amazing feature'`
4. Push to the branch: `git push origin feature/amazing-enhancement`
5. Open a Pull Request

Please ensure your code follows our coding standards and includes appropriate tests.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) team for the excellent framework
- All contributors who have helped enhance this platform
- The open source community for invaluable tools and libraries

---

<div align="center">

**Designed and developed by [Faiz Oussama](https://github.com/faiz-oussama) and contributors**

</div>

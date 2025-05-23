# Spam Detection Web App - Backend

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white)](https://jwt.io/)
[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)

A robust backend service for a Spam Detection Web Application built with Spring Boot. This service provides RESTful APIs for user authentication, email spam prediction, and admin functionalities.

## ✨ Features

### 🔒 Authentication & Authorization
- JWT-based authentication with access and refresh tokens
- Role-based access control (User & Admin)
- Email verification for new registrations
- OAuth2 integration (Google sign-in)
- Password encryption with BCrypt

### 📧 Spam Detection
- Real-time email content analysis
- Confidence scoring for predictions
- Historical prediction tracking
- User feedback collection for model improvement

### 📊 Admin Dashboard
- User management (CRUD operations)
- System statistics and analytics
- Model retriggering capability
- Feedback review and processing

### 🔄 Model Integration
- Seamless integration with ML service
- Feedback-based model retraining
- Version tracking for model updates
- Performance metrics logging

## 🚀 Tech Stack

- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL
- **Security**: Spring Security, JWT, OAuth2
- **Build Tool**: Maven
- **Validation**: Jakarta Validation
- **Email**: JavaMailSender
- **Testing**: JUnit, Mockito
- **Containerization**: Docker

## 🛠️ Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 13+
- Docker (optional)

## 🏃‍♂️ Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/spam-detection-backend.git
cd spam-detection-backend
```

### 2. Configure Environment Variables

Create a `.env` file in the root directory with the following variables:

```properties
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/spamdetection
SPRING_DATASOURCE_USERNAME=your_db_username
SPRING_DATASOURCE_PASSWORD=your_db_password

# JWT Configuration
JWT_SECRET=your_jwt_secret_key
JWT_ACCESS_TOKEN_EXPIRATION=900000    # 15 minutes
JWT_REFRESH_TOKEN_EXPIRATION=604800000  # 7 days

# Email Configuration
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password

# OAuth2 Configuration
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Application Configuration
APP_BASE_URL=http://localhost:8080
```

### 3. Run with Docker (Recommended)

```bash
docker-compose up --build
```

### 4. Run Locally

```bash
# Build the application
mvn clean package

# Run the application
java -jar target/webapp-backend-0.0.1-SNAPSHOT.jar
```

The application will be available at **http://localhost:8080**

## 🔍 API Documentation

Once the application is running, you can access:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

## 🔒 Security Best Practices

- **JWT Security**: Short-lived access tokens with secure refresh token rotation
- **Password Hashing**: BCrypt with appropriate work factor
- **CORS**: Properly configured CORS policies
- **Input Validation**: Comprehensive request validation
- **HTTPS**: Enforced in production
- **Rate Limiting**: Implemented for authentication endpoints
- **CSRF Protection**: Enabled for state-changing operations
- **Secure Headers**: Security headers configured (CSP, XSS Protection, etc.)
- **SQL Injection Prevention**: Using JPA/Hibernate with parameterized queries

## 📚 API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh-token` - Refresh access token
- `POST /api/auth/google-auth` - Google OAuth2 login
- `GET /api/auth/verify` - Verify email
- `POST /api/auth/resend-verification` - Resend verification email

### Spam Detection
- `POST /api/predict` - Check if content is spam
- `GET /api/predictions/history` - Get prediction history
- `POST /api/feedback/correct-prediction` - Submit feedback on prediction

### User Management
- `GET /api/users/me` - Get current user profile
- `PUT /api/users/me` - Update user profile

### Admin Endpoints
- `GET /api/admin/users` - List all users (Admin only)
- `PUT /api/admin/users/{userId}/role` - Update user role (Admin only)
- `GET /api/admin/stats` - Get system statistics (Admin only)
- `POST /api/admin/retraining/trigger` - Trigger model retraining (Admin only)

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the **LICENSE** file for details.

## 🙏 Acknowledgments

- Spring Boot Team
- All contributors and maintainers
- Open-source community

# WebApp-Backend 🔍📧  
**ML-Powered Spam Detection System Backend**  

Spring Boot backend for real-time email spam detection, featuring:  
🚀 **JWT Authentication** | 🛡️ **Spring Security** | 🤖 **ML Integration** | 📊 **PostgreSQL Logging**  

![Tech Stack](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)  
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)  
![Python](https://img.shields.io/badge/Python_ML_Service-3776AB?style=for-the-badge&logo=python&logoColor=white)  

### Key Features  
- REST APIs for spam prediction and user management  
- Secure JWT authentication with Spring Security  
- Real-time integration with Python ML service (Naive Bayes/SVM)  
- Prediction history logging and feedback system  
- Scheduled model retraining via user feedback  

### Core Components  
```text
📁 src/main/java  
├── config/        # Security & Web config  
├── controller/   # REST endpoints  
├── service/      # Business logic  
├── model/        # JPA entities  
├── repository/   # Database operations  
└── client/       # ML Service integration  
```

### Quick Start  
```bash  
docker-compose up --build  # Starts PostgreSQL + Spring Boot  
mvn spring-boot:run        # Local development  
```

**Frontend**: [webapp-frontend](https://github.com/yourusername/webapp-frontend) (React + Tailwind CSS)  (Yet To Implement)

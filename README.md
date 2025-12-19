# 🏋️‍♂️ AI Fitness App

An AI-powered fitness platform that helps users plan workouts, generate meal plans, track progress, and receive intelligent recommendations — built with **Spring Boot**, **PostgreSQL**, and modern cloud deployment practices.

---

## 🚀 Features

- 🤖 **AI-powered meal plans**
- 🏋️ **Workout & training planning**
- 📊 **Progress tracking & check-ins**
- 📅 **Daily and weekly fitness insights**
- 🔐 **Secure authentication with JWT**
- 📧 **Email integration (SMTP health-checked)**
- 🧠 **Extensible AI services architecture**

---

## 🛠️ Tech Stack

### Backend
- **Java 17**
- **Spring Boot 3**
- **Spring Security (JWT)**
- **Spring Data JPA**
- **Flyway** (database migrations)
- **PostgreSQL**

### DevOps / Cloud
- **Railway** (production deployment)
- **Docker-ready architecture**
- **Environment-based configuration**
- **Dynamic PORT binding for cloud runtimes**

---

## 📁 Project Structure

src/
└─ main/
├─ java/com/aifitness/
│ ├─ controller/
│ ├─ service/
│ ├─ repository/
│ ├─ config/
│ └─ dto/
└─ resources/
├─ application.properties
├─ application-production.properties
└─ db/migration/
test/

yaml
Sao chép mã

---

## 🗄️ Database & Migrations

Database schema is managed using **Flyway**.

Example migrations:
- `V6__create_meal_plan_tables.sql`
- `V7__add_ingredients_to_meal_plan_entries.sql`
- `V8__add_meal_preferences.sql`
- `V9__create_daily_checkins_table.sql`

Migrations are automatically applied on startup in production.

---

## ⚙️ Configuration

### Environment Variables (Production)

```env
SPRING_PROFILES_ACTIVE=production
DATABASE_URL=postgresql://...
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email
MAIL_PASSWORD=app_password
JWT_SECRET=super-secret-key
Dynamic Port Binding (Railway-compatible)
properties
Sao chép mã
server.port=${PORT:8080}
This allows the app to run locally on 8080 and in production on Railway’s assigned port.

🔐 Security
JWT-based authentication

Stateless API

Protected endpoints by default

Public endpoints:

/api/auth/**

/api/health/**

🩺 Health Checks
Example health endpoint:

bash
Sao chép mã
GET /api/health/email
Used to verify:

Application availability

SMTP configuration

Cloud networking health

▶️ Running Locally
1️⃣ Clone the repo
bash
Sao chép mã
git clone https://github.com/AnhTuanDangJT/AI-fitness-app.git
cd AI-fitness-app
2️⃣ Configure environment variables
Set them in your IDE or .env.

3️⃣ Run the app
bash
Sao chép mã
./mvnw spring-boot:run
App will start on:

arduino
Sao chép mã
http://localhost:8080
☁️ Deployment
This project is deployed on Railway with:

Public HTTP networking

PostgreSQL managed database

Environment-based configuration

Automatic redeploy on main branch push

📌 Future Improvements
🧠 Advanced AI coaching & personalization

📱 Mobile-friendly frontend

📊 Analytics dashboard

🥗 Macro & calorie optimization

🧪 Integration tests & monitoring

👨‍💻 Author
Tuấn Anh Đăng
Computer Science @ Concordia University
GitHub: @AnhTuanDangJT

⭐️ Support
If you like this project:

⭐ Star the repo

🍴 Fork it

🧠 Open an issue or PR

yaml
Sao chép mã

---



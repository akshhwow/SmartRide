# SmartRide

## 🚀 Project Overview

SmartRide is a carpooling application inspired by platforms like BlaBlaCar. It helps drivers publish shared rides and passengers find affordable, secure travel options on common routes. The project solves the problem of underused car seats, high ride costs, and fragmented passenger-driver coordination by combining:

- user authentication with email OTP
- ride posting and search
- passenger booking management
- driver and admin ride oversight

---

## 📌 Features

### Milestone-1
- User registration and login with email OTP verification
- JWT-based authentication and role-based protection
- Driver ride posting
- Passenger search for available rides
- Booking creation and cancellation
- Driver and passenger dashboard views
- Ratings and reviews for drivers
- Basic ride management flows

### Milestone-2
- Fare estimation endpoints and pricing calculations
- Payment entities and booking payment tracking
- Razorpay payment flow support via backend models
- Notifications system with WebSocket/STOMP support
- Real-time ride event updates via SockJS and STOMP
- Enhanced booking flow with payment statuses
- Public endpoints for fare estimation and ratings

### Milestone-3
- Admin dashboard page and admin backend support
- Transaction and review management
- Advanced security configuration for admin routes
- More complete backend architecture in `src/main/java/com/smartride`
- Tailwind/PostCSS configuration files added for frontend styling
- Continued growth of frontend protected routes and user roles

### Milestone-4
- Present in repository as a future work placeholder
- No implemented content in current workspace

---

## ⚙️ Tech Stack

- Frontend
  - React 18
  - Vite
  - React Router DOM
  - Axios
  - React Toastify
  - STOMP + SockJS for WebSocket updates
- Backend
  - Spring Boot 3.5.11
  - Java 21
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Spring Mail
  - Spring Validation
  - Spring WebSocket
  - JSON Web Tokens via JJWT
- Database
  - MySQL
- Other
  - Maven wrapper (`mvnw`, `mvnw.cmd`)
  - Tailwind config files present in Milestone-3
  - PostCSS config file present in Milestone-3

---

## 📂 Project Structure

SmartRide/
- Milestone-1/
  - smartride-backend-main/
    - smartride-backend/
      - pom.xml
      - src/
  - smartride-frontend-main/
    - smartride-frontend/
      - package.json
      - src/
- Milestone-2/
  - smartride-backend-main/
    - smartride-backend/
      - pom.xml
      - src/
  - smartride-frontend/
    - package.json
    - src/
- Milestone-3/
  - smartride-backend-main/
    - smartride-backend/
      - pom.xml
      - src/
        - main/
          - java/com/smartride/
            - config/
            - controller/
            - dto/
            - entity/
            - exception/
            - repository/
            - scheduler/
            - security/
            - service/
          - resources/
            - application.properties
  - smartride-frontend/
    - package.json
    - src/
      - App.jsx
      - main.jsx
      - index.css
      - components/
        - driver/
        - passenger/
        - shared/
      - context/
        - AuthContext.jsx
      - pages/
        - AdminDashboard.jsx
        - Dashboard.jsx
        - Home.jsx
        - Login.jsx
        - MyBookings.jsx
        - MyRides.jsx
        - PostRide.jsx
        - Register.jsx
        - SearchRides.jsx
        - VerifyOtp.jsx
      - services/
    - postcss.config.js
    - tailwind.config.js
- Milestone-4/

---

## ⚙️ Installation & Setup

### Backend (Milestone-3)
1. Open terminal
2. `cd Milestone-3/smartride-backend-main/smartride-backend`
3. Install and run:
   - Windows: `mvnw.cmd spring-boot:run`
   - macOS/Linux: `./mvnw spring-boot:run`
4. Ensure MySQL is running
5. Update `src/main/resources/application.properties` if needed
6. Set environment variable:
   - `EMAIL_PASSWORD` for Gmail SMTP OTP support

### Frontend (Milestone-3)
1. Open terminal
2. `cd Milestone-3/smartride-frontend`
3. Install dependencies:
   - `npm install`
4. Start development server:
   - `npm run dev`
5. Visit the frontend in browser at the Vite URL shown in terminal

### Build / Preview
- Frontend build:
  - `npm run build`
- Frontend preview:
  - `npm run preview`

---

## 🖼️ Screenshots

Screenshot assets are available in:
- smartride-frontend-main
- Milestone-2
  - `Screenshot 2026-03-19 002753.png`
  - `Screenshot 2026-03-19 002834.png`

---

## 💡 Future Enhancements

- Real-time ride tracking on a map
- In-app chat between drivers and passengers
- Payment gateway completion and receipts
- Better admin analytics and ride moderation
- Mobile-responsive UI enhancements
- User profile management and ride history export
- Enhanced route filtering with distance and time windows

---

## 🤝 Contribution Guidelines

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Make your changes
4. Test locally
5. Submit a pull request with a clear description
6. Follow existing project structure and naming conventions

---

## 📄 License

This project is released under the MIT License.

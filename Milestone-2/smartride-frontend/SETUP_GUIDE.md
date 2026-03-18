# 🚗 SmartRide Frontend - Complete Setup Guide

## 📋 Prerequisites

- Node.js 18+ installed
- npm or yarn package manager
- SmartRide Backend running on http://localhost:8080

---

## 🚀 Quick Start

### Step 1: Install Dependencies

```bash
cd smartride-frontend
npm install
```

### Step 2: Start Development Server

```bash
npm run dev
```

The app will open at: **http://localhost:3000**

---

## 📁 Project Structure

```
smartride-frontend/
├── src/
│   ├── components/         # Reusable components
│   │   └── Navbar.jsx
│   ├── pages/             # Page components
│   │   ├── Home.jsx
│   │   ├── Register.jsx
│   │   ├── Login.jsx
│   │   ├── VerifyOtp.jsx
│   │   ├── Dashboard.jsx
│   │   ├── SearchRides.jsx
│   │   ├── PostRide.jsx
│   │   ├── MyRides.jsx
│   │   └── MyBookings.jsx
│   ├── context/           # React Context
│   │   └── AuthContext.jsx
│   ├── services/          # API calls
│   │   └── api.js
│   ├── App.jsx            # Main app component
│   ├── main.jsx           # Entry point
│   └── index.css          # Global styles
├── package.json
├── vite.config.js
└── index.html
```

---

## 🎯 Features Implemented (Milestone 1)

### ✅ User Management
- User Registration (Passenger & Driver)
- Email OTP Verification
- Login with JWT
- Role-based dashboards

### ✅ Ride Management
- Post Ride (Driver)
- Search Rides (All users)
- View Ride Details
- List Driver's Rides

### ✅ Booking System
- Book Ride (Passenger)
- View My Bookings
- Cancel Booking
- View Ride Bookings (Driver)

---

## 🔧 Configuration

The frontend is pre-configured to connect to:
- **Backend API**: http://localhost:8080/api
- **Frontend Port**: 3000

To change these, edit `vite.config.js`:

```javascript
server: {
  port: 3000,  // Change frontend port
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // Change backend URL
    },
  },
}
```

---

## 📱 User Flow

### For Passengers:
1. Register → Verify Email → Login
2. Search for rides
3. Book a ride
4. View bookings in Dashboard

### For Drivers:
1. Register (with vehicle details) → Verify Email → Login
2. Post a ride
3. View posted rides
4. See who booked your rides

---

## 🎨 Tech Stack

- **React 18** - UI Library
- **React Router v6** - Routing
- **Axios** - HTTP Client
- **React Toastify** - Notifications
- **Vite** - Build Tool
- **CSS3** - Styling

---

## 🧪 Testing the Application

### Test Passenger Flow:
1. Register as Passenger
2. Login
3. Search rides from "Mumbai" to "Pune"
4. Book a ride

### Test Driver Flow:
1. Register as Driver (fill vehicle details)
2. Login
3. Post a ride
4. View your rides

---

## 🐛 Troubleshooting

### Port 3000 already in use
```bash
# Kill process on port 3000
npx kill-port 3000

# Or change port in vite.config.js
```

### Backend connection error
- Ensure Spring Boot backend is running on port 8080
- Check CORS settings in SecurityConfig.java
- Verify `application.properties` has: `app.cors.allowed-origins=http://localhost:3000`

### Build for production
```bash
npm run build
```

Output will be in `dist/` folder.

---

## 📝 API Endpoints Used

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/auth/register` | POST | No | Register user |
| `/api/auth/verify-otp` | POST | No | Verify OTP |
| `/api/auth/login` | POST | No | Login |
| `/api/rides` | POST | Yes | Post ride |
| `/api/rides/search` | GET | No | Search rides |
| `/api/rides/my-rides` | GET | Yes | Get driver's rides |
| `/api/bookings` | POST | Yes | Book ride |
| `/api/bookings/my` | GET | Yes | Get passenger's bookings |
| `/api/bookings/ride/{id}` | GET | Yes | Get ride bookings |
| `/api/bookings/{id}/cancel` | PUT | Yes | Cancel booking |

---

## ✨ Next Steps (Milestone 2)

- Payment Integration
- Real-time Notifications
- Review & Rating System
- Admin Dashboard
- Google Maps Integration

---

## 🆘 Need Help?

Check the console for errors:
- Browser DevTools (F12) → Console
- Network tab to see API calls

---

**Happy Coding! 🚀**

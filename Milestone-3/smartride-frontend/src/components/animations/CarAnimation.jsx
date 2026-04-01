import React from 'react';
import { motion } from 'framer-motion';

const CarAnimation = ({ className }) => {
  return (
    <div className={`relative flex items-center justify-center overflow-hidden ${className}`}>
      {/* Abstract Background speed lines */}
      <motion.div
        animate={{ x: ['100%', '-100%'] }}
        transition={{ duration: 0.8, repeat: Infinity, ease: "linear" }}
        className="absolute top-1/4 w-32 h-1 bg-blue-300 rounded-full opacity-50"
      />
      <motion.div
        animate={{ x: ['100%', '-100%'] }}
        transition={{ duration: 0.6, repeat: Infinity, ease: "linear", delay: 0.2 }}
        className="absolute top-1/2 w-16 h-1 bg-blue-200 rounded-full opacity-70"
      />
      <motion.div
        animate={{ x: ['100%', '-100%'] }}
        transition={{ duration: 1, repeat: Infinity, ease: "linear", delay: 0.5 }}
        className="absolute bottom-1/4 w-48 h-1 bg-blue-400 rounded-full opacity-40"
      />

      {/* Car Body */}
      <motion.div
        animate={{ y: [0, -5, 0] }}
        transition={{ duration: 0.8, repeat: Infinity, ease: "easeInOut" }}
        className="relative z-10 w-full h-full max-w-[300px]"
      >
        <svg viewBox="0 0 200 100" className="w-full h-full drop-shadow-2xl">
          {/* Main Body */}
          <path d="M 30,60 L 30,40 C 30,30 40,20 60,20 L 120,20 C 140,20 160,30 170,40 L 180,60 C 185,60 190,65 190,70 L 190,80 L 10,80 L 10,70 C 10,65 15,60 20,60 Z" fill="#ffffff" />
          
          {/* Windows */}
          <path d="M 65,25 L 115,25 L 135,40 L 55,40 Z" fill="#3b82f6" opacity="0.8" />
          <path d="M 120,25 L 150,25 C 155,25 160,30 162,38 L 138,38 Z" fill="#2563eb" opacity="0.9" />

          {/* Details */}
          <rect x="175" y="65" width="10" height="5" fill="#ef4444" rx="2" />
          <rect x="15" y="65" width="15" height="5" fill="#fbbf24" rx="2" />
          
          {/* Wheels Base Background */}
          <circle cx="50" cy="80" r="15" fill="#1e293b" />
          <circle cx="150" cy="80" r="15" fill="#1e293b" />
          
          {/* Wheels Rotating via Framer Motion */}
          <motion.g
            animate={{ rotate: 360 }}
            transition={{ duration: 0.5, repeat: Infinity, ease: "linear" }}
            style={{ transformOrigin: "50px 80px" }}
          >
            <circle cx="50" cy="80" r="8" fill="#e2e8f0" />
            <circle cx="50" cy="75" r="2" fill="#94a3b8" />
            <circle cx="50" cy="85" r="2" fill="#94a3b8" />
            <circle cx="45" cy="80" r="2" fill="#94a3b8" />
            <circle cx="55" cy="80" r="2" fill="#94a3b8" />
          </motion.g>

          <motion.g
            animate={{ rotate: 360 }}
            transition={{ duration: 0.5, repeat: Infinity, ease: "linear" }}
            style={{ transformOrigin: "150px 80px" }}
          >
            <circle cx="150" cy="80" r="8" fill="#e2e8f0" />
            <circle cx="150" cy="75" r="2" fill="#94a3b8" />
            <circle cx="150" cy="85" r="2" fill="#94a3b8" />
            <circle cx="145" cy="80" r="2" fill="#94a3b8" />
            <circle cx="155" cy="80" r="2" fill="#94a3b8" />
          </motion.g>
        </svg>
      </motion.div>
    </div>
  );
};

export default CarAnimation;

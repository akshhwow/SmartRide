import React from 'react';
import { motion } from 'framer-motion';

const LoadingAnimation = ({ className, text = "LOADING..." }) => {
  return (
    <div className={`flex flex-col items-center justify-center ${className || ''}`}>
      <motion.div
        animate={{ rotate: 360 }}
        transition={{ duration: 1.5, repeat: Infinity, ease: "linear" }}
        className="relative w-20 h-20 mb-4 drop-shadow-lg"
      >
        <svg viewBox="0 0 100 100" className="w-full h-full">
          {/* Outer Ring */}
          <circle cx="50" cy="50" r="40" fill="none" stroke="#e0e7ff" strokeWidth="8" />
          <circle cx="50" cy="50" r="40" fill="none" stroke="#4f46e5" strokeWidth="8" strokeDasharray="60" strokeLinecap="round" />
          
          {/* Inner Steering Wheel Abstract */}
          <motion.g animate={{ rotate: -360 }} transition={{ duration: 3, repeat: Infinity, ease: "linear" }}>
            <circle cx="50" cy="50" r="20" fill="none" stroke="#60a5fa" strokeWidth="4" />
            <path d="M 30,50 L 70,50" stroke="#60a5fa" strokeWidth="4" />
            <path d="M 50,30 L 50,70" stroke="#60a5fa" strokeWidth="4" />
          </motion.g>
        </svg>
      </motion.div>
      <motion.div 
        animate={{ opacity: [0.4, 1, 0.4] }}
        transition={{ duration: 1.5, repeat: Infinity }}
        className="text-blue-600 font-bold tracking-widest text-sm"
      >
        {text}
      </motion.div>
    </div>
  );
};

export default LoadingAnimation;

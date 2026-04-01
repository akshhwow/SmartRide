import React from 'react';
import { motion } from 'framer-motion';

const EmptyAnimation = ({ className }) => {
  return (
    <div className={`relative flex items-center justify-center ${className}`}>
      <motion.div
        animate={{ y: [0, -8, 0], rotate: [0, -2, 2, 0] }}
        transition={{ duration: 4, repeat: Infinity, ease: "easeInOut" }}
        className="w-full h-full max-w-[180px]"
      >
        <svg viewBox="0 0 100 100" className="w-full h-full drop-shadow-md text-gray-300">
          {/* Box shadow beneath */}
          <ellipse cx="50" cy="85" rx="30" ry="5" fill="#e2e8f0" />
          
          {/* Main Box */}
          <path d="M 20,40 L 80,40 L 70,80 L 30,80 Z" fill="#cbd5e1" />
          <path d="M 20,40 L 50,25 L 80,40 Z" fill="#f1f5f9" />
          <path d="M 50,25 L 50,80" stroke="#94a3b8" strokeWidth="2" opacity="0.5" />
          <path d="M 35,45 L 65,45" stroke="#94a3b8" strokeWidth="2" strokeDasharray="4,2" />
          
          {/* Abstract Sad/Empty Face */}
          <circle cx="40" cy="60" r="2" fill="#64748b" />
          <circle cx="60" cy="60" r="2" fill="#64748b" />
          <path d="M 45,70 Q 50,65 55,70" stroke="#64748b" strokeWidth="2" fill="none" />
          
          {/* Small Dust/Leaves blowing */}
          <motion.circle 
            cx="20" cy="70" r="1.5" fill="#94a3b8"
            animate={{ x: [0, 10, 20], y: [0, -10, 5], opacity: [1, 0, 0] }}
            transition={{ duration: 2, repeat: Infinity }}
          />
        </svg>
      </motion.div>
    </div>
  );
};

export default EmptyAnimation;

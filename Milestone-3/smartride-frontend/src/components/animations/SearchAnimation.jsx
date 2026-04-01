import React from 'react';
import { motion } from 'framer-motion';

const SearchAnimation = ({ className }) => {
  return (
    <div className={`relative flex items-center justify-center ${className}`}>
      <motion.div
        animate={{ y: [0, -10, 0] }}
        transition={{ duration: 2, repeat: Infinity, ease: "easeInOut" }}
        className="w-full h-full max-w-[200px]"
      >
        <svg viewBox="0 0 100 100" className="w-full h-full drop-shadow-xl text-blue-600">
          {/* Map/Document */}
          <rect x="20" y="20" width="60" height="60" rx="8" fill="#f1f5f9" />
          <path d="M 30,40 L 50,30 L 70,50 L 50,70 Z" fill="#bae6fd" opacity="0.5" />
          <circle cx="35" cy="45" r="4" fill="#ef4444" />
          <circle cx="65" cy="55" r="4" fill="#3b82f6" />
          
          {/* Magnifying Glass Animated Scanning */}
          <motion.g
            animate={{ 
              x: [-10, 20, -10], 
              y: [-10, 20, -10],
              rotate: [-10, 10, -10]
            }}
            transition={{ duration: 3, repeat: Infinity, ease: "easeInOut" }}
          >
            <circle cx="45" cy="45" r="15" fill="none" stroke="currentColor" strokeWidth="6" />
            <line x1="55" y1="55" x2="70" y2="70" stroke="currentColor" strokeWidth="6" strokeLinecap="round" />
            <circle cx="45" cy="45" r="10" fill="#60a5fa" opacity="0.3" />
          </motion.g>
        </svg>
      </motion.div>
    </div>
  );
};

export default SearchAnimation;

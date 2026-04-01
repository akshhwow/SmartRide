import React, { useEffect, useState } from 'react';
import Lottie from 'lottie-react';
import { motion } from 'framer-motion';

const LottieAnimation = ({ src, className, loop = true, autoplay = true }) => {
  const [animationData, setAnimationData] = useState(null);
  const [hasError, setHasError] = useState(false);

  useEffect(() => {
    fetch(src)
      .then((res) => {
        if (!res.ok) throw new Error('CDN error');
        return res.json();
      })
      .then((data) => setAnimationData(data))
      .catch((err) => {
        console.error("Error loading lottie animation, using fallback:", err);
        setHasError(true);
      });
  }, [src]);

  if (hasError) {
    // Beautiful Framer Motion Fallback Loader when Lottiefiles CDN is blocked
    return (
      <div className={`flex flex-col items-center justify-center ${className || ''}`}>
        <motion.div
           animate={{ rotate: 360 }}
           transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
           className="w-16 h-16 border-4 border-blue-100 border-t-blue-600 rounded-full mb-3"
        />
        <motion.div 
           animate={{ opacity: [0.5, 1, 0.5] }}
           transition={{ duration: 1.5, repeat: Infinity }}
           className="text-blue-600 font-bold tracking-widest text-sm"
        >
           LOADING...
        </motion.div>
      </div>
    );
  }

  if (!animationData) {
    return (
      <div className={`flex flex-col items-center justify-center ${className || ''}`}>
        <div className="animate-pulse bg-blue-50 border border-blue-100 rounded-full w-16 h-16 flex items-center justify-center">
            <span className="text-2xl animate-bounce">🚗</span>
        </div>
      </div>
    );
  }

  return (
    <div className={className}>
      <Lottie animationData={animationData} loop={loop} autoplay={autoplay} />
    </div>
  );
};

export default LottieAnimation;

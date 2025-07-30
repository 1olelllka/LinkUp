import { useState } from "react";

interface CustomAvatarProps {
  name?: string;
  photo?: string;
  size?: number; // px
  className?: string;
}

export const CustomAvatar = ({
  name = "?",
  photo,
  size = 96,
  className = "",
}: CustomAvatarProps) => {
  const [imgError, setImgError] = useState(false);
  const firstLetter = name.trim().charAt(0).toUpperCase();

  const fallback = (
    <div
      className={`flex items-center justify-center bg-gray-300 text-gray-700 font-bold rounded-full ${className}`}
      style={{ width: size, height: size, fontSize: size * 0.4 }}
    >
      {firstLetter}
    </div>
  );

  return photo && !imgError ? (
    <img
      src={photo}
      alt="Avatar"
      onError={() => setImgError(true)}
      className={`rounded-full object-cover ${className}`}
      style={{ width: size, height: size }}
    />
  ) : (
    fallback
  );
};

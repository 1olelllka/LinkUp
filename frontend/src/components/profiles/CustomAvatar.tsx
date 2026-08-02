import { useEffect, useState } from "react";

interface CustomAvatarProps {
  name?: string;
  photo?: string;
  size?: number;
  className?: string;
}

export const CustomAvatar = ({
  name = "?",
  photo,
  size = 96,
  className = "",
}: CustomAvatarProps) => {
  const [imgError, setImgError] = useState(false);

  useEffect(() => {
    setImgError(false);
  }, [photo]);

  const firstLetter = name.trim().charAt(0).toUpperCase() || "?";

  const baseClassName = `
    rounded-full
    border-2
    border-[#C9A063]
    shadow-md
    ${className}
  `;

  if (photo && !imgError) {
    return (
      <img
        src={photo}
        alt={`${name}'s avatar`}
        onError={() => setImgError(true)}
        className={`${baseClassName} object-cover`}
        style={{
          width: size,
          height: size,
        }}
      />
    );
  }

  return (
    <div
      className={`
        ${baseClassName}
        flex
        items-center
        justify-center
        bg-[#1E1A16]
        text-[#F3EBD9]
        font-display
        font-bold
      `}
      style={{
        width: size,
        height: size,
        fontSize: size * 0.4,
      }}
      aria-label={`${name}'s avatar`}
    >
      {firstLetter}
    </div>
  );
};
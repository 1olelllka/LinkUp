import { HashLoader } from "react-spinners";

export const SubmitLoader = () => {
  return (
    <div className="absolute inset-0 z-50 flex items-center justify-center rounded-sm bg-black/40">
      <HashLoader color="#D9A441" size={60} />
    </div>
  );
};

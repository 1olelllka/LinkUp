import { HashLoader } from "react-spinners";

export const SubmitLoader = () => {
  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black/40 z-50">
      <HashLoader color="#71C9CE" size={60} />
    </div>
  );
};

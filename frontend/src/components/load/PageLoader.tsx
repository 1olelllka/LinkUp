import { HashLoader } from "react-spinners";


export const PageLoader = () => {

    return (
        <div className="flex justify-center items-center">
            <HashLoader 
                color={"#71C9CE"}
                size={60}
            />
        </div>
    );
}
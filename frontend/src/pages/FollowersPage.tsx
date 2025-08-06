import { FollowList } from "@/components/profiles/FollowList";
import { MainLayout } from "@/layouts/MainLayout";

export const FollowersPage = ({type} : {type: "follower" | "followee"}) => {

    return (
        <MainLayout>
            <FollowList type={type} />
        </MainLayout>
    );
}
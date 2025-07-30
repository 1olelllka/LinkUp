import { FollowList } from "@/components/profiles/FollowList";
import { FollowersLayout } from "@/layouts/FollowersLayout";


export const FollowersPage = ({type} : {type: "follower" | "followee"}) => {

    return (
        <FollowersLayout>
            <FollowList type={type}/>
        </FollowersLayout>
    );
}
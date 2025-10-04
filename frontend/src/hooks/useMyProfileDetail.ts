import { getMe } from "@/services/authServices";
import { getSpecificProfileInfo } from "@/services/profileServices";
import type { Profile } from "@/types/Profile";
import { AxiosError } from "axios";
import { useEffect, useState } from "react";
import { useSearchParams } from "react-router";

export const useMyProfileDetail = () => {
  const [profile, setProfile] = useState<Profile>({
    id: "",
    name: "",
    username: "",
    email: "",
    authProvider: "",
    photo: "",
    aboutMe: "",
    gender: "UNDEFINED",
    dateOfBirth: "",
    createdAt: "",
  });
  const [error, setError] = useState<AxiosError>();
  const [loading, setLoading] = useState(false);
  const [pendingParam, setPendingParam] = useSearchParams();
  const [pending, setPending] = useState(pendingParam.get("pending") === "true");

  useEffect(() => {
    let intervalId: NodeJS.Timeout | null = null;

    const fetchProfile = async () => {
      try {
        setLoading(true);
        const user = await getMe();
        setProfile(user);

        const specific = await getSpecificProfileInfo(user.userId);
        const combinedProfile = { ...user, ...specific };
        setProfile(combinedProfile);

        if (pending) {
            setPending(false);
            if (intervalId) clearInterval(intervalId);
            setPendingParam({});
        }
      } catch (err) {
        const error = err as AxiosError;
        // If there's 403 error globall the protected route will be triggered. If here, mostly it's because of late db+token handling
        if (error.status != 404 && error.status != 403) {
            setError(err as AxiosError);
            if (intervalId) clearInterval(intervalId);
        }
        
      } finally {
        setLoading(false);
      }
    };
    if (pending) {
      fetchProfile();
      intervalId = setInterval(fetchProfile, 2000);
    } else {
      fetchProfile();
    }

    return () => {
      if (intervalId) clearInterval(intervalId);
    };
  }, [pending]);

  return { profile, setProfile, error, loading };
};

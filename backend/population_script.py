import random
import string
from datetime import datetime, timedelta
import requests
from typing import List, Dict
import time

API_URL = "http://localhost:8080/api"

GENDERS = ["MALE", "FEMALE", "UNDEFINED"]


def random_date(start_year=1970, end_year=2005) -> str:
    start = datetime(start_year, 1, 1)
    end = datetime(end_year, 12, 31)
    delta = end - start
    random_days = random.randint(0, delta.days)
    return (start + timedelta(days=random_days)).strftime("%Y-%m-%d")


def generate_alias(length=8) -> str:
    chars = string.ascii_letters + string.digits + "_"
    return ''.join(random.choices(chars, k=length))


def generate_password(length=8) -> str:
    password = [
        random.choice(string.ascii_uppercase),
        random.choice(string.ascii_lowercase),
        random.choice(string.digits),
        random.choice(string.digits)
    ]
    remaining = max(length - 4, 0)
    password += random.choices(string.ascii_letters + string.digits, k=remaining)
    random.shuffle(password)
    return ''.join(password)


def generate_users(count: int = 15) -> List[Dict]:
    return [
        {
            "alias": generate_alias(random.randint(8, 12)),
            "password": generate_password(random.randint(8, 12)),
            "name": f"Name{random.randint(1, 100)} Last{i}",
            "dateOfBirth": random_date(),
            "email": f"user{i}@example.com",
            "gender": random.choice(GENDERS),
            "aboutMe": f"This is a short bio of user{i}. Loves programming and coffee.",
            "photo": f"https://randomuser.me/api/portraits/lego/{i}.jpg"
        }
        for i in range(1, count + 1)
    ]


def generate_images(count=2) -> List[Dict]:
    return [
        {
            "image": f"https://picsum.photos/seed/{random.randint(0, 100000)*10 + i}/200/200",
            "desc": f"Sample description {i+1}"
        } for i in range(count)
    ]


def register_user(user: Dict) -> Dict:
    """Register, login, and update profile. Returns dict with user_id and jwt."""
    # Register
    reg_resp = requests.post(f"{API_URL}/auth/register", json={
        "alias": user["alias"],
        "password": user["password"],
        "gender": user["gender"],
        "email": user["email"],
        "name": user["name"],
        "dateOfBirth": user["dateOfBirth"]
    })
    reg_resp.raise_for_status()
    user_id = reg_resp.json().get("userId")

    # Login
    login_resp = requests.post(f"{API_URL}/auth/login", json={
        "email": user["email"],
        "password": user["password"]
    })
    login_resp.raise_for_status()
    access_token = login_resp.json().get("accessToken")

    # Update profile
    requests.patch(f"{API_URL}/profiles/{user_id}", json={
        "aboutMe": user["aboutMe"],
        "photo": user["photo"]
    }, headers={"Authorization": f"Bearer {access_token}"})

    return {"user_id": user_id, "jwt": access_token}


def follow_users(user_data: List[Dict]):
    for follower in user_data:
        followee = random.choice([u for u in user_data if u["user_id"] != follower["user_id"]])
        try:
            resp = requests.post(f"{API_URL}/profiles/follow", json={
                "followerId": follower["user_id"],
                "followeeId": followee["user_id"]
            }, headers={"Authorization": f"Bearer {follower['jwt']}"})
            if resp.status_code == 200:
                print(f"{follower['user_id']} followed {followee['user_id']}")
            else:
                print(f"Failed: {follower['user_id']} -> {followee['user_id']} | {resp.status_code} | {resp.text}")
        except Exception as e:
            print(f"Error following: {follower['user_id']} -> {followee['user_id']}: {e}")
        time.sleep(0.1)  # slight delay to avoid overload


def create_posts_stories(user_id: str, access_token: str):
    headers = {"Authorization": f"Bearer {access_token}"}
    
    # Create posts
    for img in generate_images():
        requests.post(f"{API_URL}/posts/users/{user_id}", json=img, headers=headers)
    
    # Create stories
    for img in generate_images():
        requests.post(f"{API_URL}/stories/users/{user_id}", json={"image": img["image"]}, headers=headers)


def main():
    users = generate_users()
    user_data = []

    # Register and update profiles
    for user in users:
        print(f"Registering user {user['alias']}, password {user.get("password")}")
        data = register_user(user)
        user_data.append(data)
        time.sleep(0.1)

    # Follow users
    follow_users(user_data)

    # Create posts and stories
    for user in user_data:
        create_posts_stories(user["user_id"], user["jwt"])
        time.sleep(0.1)


if __name__ == "__main__":
    main()

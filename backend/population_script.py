import random
import string
import logging
import time
from datetime import datetime, timedelta
from typing import List, Dict, Optional
import requests

# ---------------------------------------------------------------------------
# Logging Setup
# ---------------------------------------------------------------------------
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[logging.StreamHandler()]
)
logger = logging.getLogger("DataSeeder")

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


def generate_users(count: int = 30) -> List[Dict]:
    logger.info(f"Generating mock data for {count} users...")
    users = [
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
    logger.info(f"Successfully generated {len(users)} user profiles.")
    return users


def generate_images(count=2) -> List[Dict]:
    return [
        {
            "image": f"https://picsum.photos/seed/{random.randint(0, 100000)*10 + i}/200/200",
            "desc": f"Sample description {i+1}"
        } for i in range(count)
    ]


def request_with_retry(
    method: str, 
    url: str, 
    max_retries: int = 5, 
    base_delay: float = 0.3, 
    **kwargs
) -> requests.Response:
    """Sends HTTP requests with a small base delay and retries on 503 or 429 status codes."""
    delay = 2.0  # Initial delay for 503 backoff
    
    for attempt in range(1, max_retries + 1):
        time.sleep(base_delay)
        
        try:
            resp = requests.request(method, url, **kwargs)
        except requests.RequestException as e:
            logger.error(f"Network request failed completely: {method} {url} | Error: {e}")
            raise e

        # Handle 429 Too Many Requests
        if resp.status_code == 429:
            retry_after = resp.headers.get("Retry-After")
            wait_time = float(retry_after) if retry_after and retry_after.isdigit() else 5.0
            
            if attempt < max_retries:
                logger.warning(
                    f"Rate limited (429) on {method} {url}. "
                    f"Waiting {wait_time}s before retry ({attempt}/{max_retries})..."
                )
                time.sleep(wait_time)
                continue

        # Handle 503 Service Unavailable
        if resp.status_code == 503:
            if attempt < max_retries:
                logger.warning(
                    f"Service Unavailable (503) on {method} {url}. "
                    f"Retrying in {delay}s ({attempt}/{max_retries})..."
                )
                time.sleep(delay)
                delay *= 2.0
                continue

        return resp
        
    return resp


def register_user(user: Dict, current_idx: int, total_users: int) -> Optional[Dict]:
    """Register, login, and update profile. Returns dict with user_id and jwt or None on conflict."""
    logger.info(f"[{current_idx}/{total_users}] Registering user: {user['alias']} ({user['email']})")

    # 1. Register
    reg_resp = request_with_retry("POST", f"{API_URL}/auth/register", json={
        "alias": user["alias"],
        "password": user["password"],
        "gender": user["gender"],
        "email": user["email"],
        "name": user["name"],
        "dateOfBirth": user["dateOfBirth"]
    })

    if reg_resp.status_code == 409:
        logger.warning(f"[{current_idx}/{total_users}] Conflict 409: User {user['alias']} already exists. Skipping.")
        return None

    if not reg_resp.ok:
        logger.error(f"[{current_idx}/{total_users}] Registration failed: {reg_resp.status_code} - {reg_resp.text}")
        reg_resp.raise_for_status()

    user_id = reg_resp.json().get("userId")
    logger.debug(f"[{current_idx}/{total_users}] Registered successfully. Assigned userId: {user_id}")

    # 2. Login
    logger.debug(f"[{current_idx}/{total_users}] Logging in user: {user['alias']}")
    login_resp = request_with_retry("POST", f"{API_URL}/auth/login", json={
        "email": user["email"],
        "password": user["password"]
    })

    if not login_resp.ok:
        logger.error(f"[{current_idx}/{total_users}] Login failed for {user['email']}: {login_resp.status_code}")
        login_resp.raise_for_status()

    access_token = login_resp.json().get("accessToken")
    logger.debug(f"[{current_idx}/{total_users}] Login successful. JWT obtained.")

    # 3. Update profile
    logger.debug(f"[{current_idx}/{total_users}] Updating profile details for userId: {user_id}")
    patch_resp = request_with_retry(
        "PATCH", 
        f"{API_URL}/profiles/{user_id}", 
        json={
            "aboutMe": user["aboutMe"],
            "photo": user["photo"]
        }, 
        headers={"Authorization": f"Bearer {access_token}"}
    )

    if not patch_resp.ok:
        logger.error(f"[{current_idx}/{total_users}] Profile update failed for {user_id}: {patch_resp.status_code}")
        patch_resp.raise_for_status()

    logger.info(f"[{current_idx}/{total_users}] User {user['alias']} fully setup!")
    return {"user_id": user_id, "jwt": access_token}


def follow_users(user_data: List[Dict]):
    total = len(user_data)
    if total < 2:
        logger.warning("Not enough registered users to build follow relationships.")
        return

    logger.info(f"Starting follow phase for {total} users...")
    success_count = 0

    for idx, follower in enumerate(user_data, start=1):
        followee = random.choice([u for u in user_data if u["user_id"] != follower["user_id"]])
        logger.info(f"[{idx}/{total}] User {follower['user_id']} following -> {followee['user_id']}")
        
        try:
            resp = request_with_retry(
                "POST", 
                f"{API_URL}/profiles/follow", 
                json={
                    "followerId": follower["user_id"],
                    "followeeId": followee["user_id"]
                }, 
                headers={"Authorization": f"Bearer {follower['jwt']}"}
            )
            if resp.status_code == 200:
                success_count += 1
                logger.debug(f"Follow request succeeded: {follower['user_id']} -> {followee['user_id']}")
            else:
                logger.error(
                    f"Failed follow attempt: {follower['user_id']} -> {followee['user_id']} | "
                    f"Status: {resp.status_code} | Body: {resp.text}"
                )
        except Exception as e:
            logger.error(f"Exception during follow request: {follower['user_id']} -> {followee['user_id']}: {e}")

    logger.info(f"Follow phase completed. {success_count}/{total} follow operations succeeded.")


def create_posts_stories(user_id: str, access_token: str, current_idx: int, total_users: int):
    headers = {"Authorization": f"Bearer {access_token}"}
    images = generate_images()
    
    logger.info(f"[{current_idx}/{total_users}] Creating posts and stories for user {user_id}...")
    
    # Create posts
    for post_idx, img in enumerate(images, start=1):
        resp = request_with_retry("POST", f"{API_URL}/posts/users/{user_id}", json=img, headers=headers)
        if resp.ok:
            logger.debug(f"[{current_idx}/{total_users}] Post {post_idx}/{len(images)} created for user {user_id}")
        else:
            logger.error(f"Failed to create post {post_idx} for user {user_id}: Status {resp.status_code}")

    # Create stories
    for story_idx, img in enumerate(images, start=1):
        resp = request_with_retry("POST", f"{API_URL}/stories/users/{user_id}", json={"image": img["image"]}, headers=headers)
        if resp.ok:
            logger.debug(f"[{current_idx}/{total_users}] Story {story_idx}/{len(images)} created for user {user_id}")
        else:
            logger.error(f"Failed to create story {story_idx} for user {user_id}: Status {resp.status_code}")


def main():
    logger.info("==========================================")
    logger.info("Starting API Data Seeding Script")
    logger.info("==========================================")

    users = generate_users()
    user_data = []

    # 1. Register Phase
    logger.info("--- PHASE 1: User Registration & Profile Setup ---")
    total_users = len(users)
    for idx, user in enumerate(users, start=1):
        data = register_user(user, idx, total_users)
        if data:
            user_data.append(data)

    logger.info(f"Phase 1 complete. Successfully registered {len(user_data)}/{total_users} users.")

    # 2. Follow Phase
    logger.info("--- PHASE 2: Building Follow Network ---")
    follow_users(user_data)

    # 3. Content Creation Phase
    logger.info("--- PHASE 3: Generating Posts & Stories ---")
    total_active = len(user_data)
    for idx, user in enumerate(user_data, start=1):
        create_posts_stories(user["user_id"], user["jwt"], idx, total_active)

    logger.info("==========================================")
    logger.info("Data Seeding Completed Successfully!")
    logger.info("==========================================")


if __name__ == "__main__":
    main()
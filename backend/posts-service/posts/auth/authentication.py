import jwt
from rest_framework.authentication import BaseAuthentication
from rest_framework.exceptions import AuthenticationFailed
from django.conf import settings
import base64
from .user import JWTUser

class JWTAuthentication(BaseAuthentication):
    def authenticate(self, request):
        auth_header = request.headers.get("Authorization")

        if not auth_header or not auth_header.startswith('Bearer '):
            return None

        token = auth_header.split(' ')[1]
        decoded_key = base64.b64decode(settings.JWT_SECRET)
        try:
            payload = jwt.decode(token, decoded_key, algorithms="HS384")
        except jwt.ExpiredSignatureError:
            raise AuthenticationFailed('Token expired')
        except jwt.InvalidTokenError as ex:
            raise AuthenticationFailed('Invalid token')
        user = JWTUser(payload);
        return (user, None)
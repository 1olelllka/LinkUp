

class JWTUser:
    def __init__(self, payload):
        self.payload = payload
        self.id = payload.get('sub')
    
    @property
    def is_authenticated(self):
        return True

    def get(self, attr):
        return self.payload.get(attr)
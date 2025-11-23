#!/bin/sh

for x in MONGO_USERNAME MONGO_PASSWORD RABBIT_USERNAME RABBIT_PASSWORD JWT_SECRET
do
    eval val=\$$x
    if [ -z "$val" ]; then
        echo "$x is missing..."
        unset $x
    fi
done

exec "$@"
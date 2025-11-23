#!/bin/sh

for x in RABBIT_USERNAME RABBIT_PASSWORD JWT_SECRET MONGO_PASSWORD MONGO_USERNAME
do
    eval val=\$$x
    if [ -z "$val" ]; then
        echo "$x is missing..."
        unset $x
    fi
done

exec "$@"
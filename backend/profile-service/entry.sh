#!/bin/sh

for x in RABBIT_USERNAME RABBIT_PASSWORD JWT_SECRET NEO4J_USERNAME NEO4J_PASSWORD
do
    eval val=\$$x
    if [ -z "$val" ]; then
        echo "$x is missing..."
        unset $x
    fi
done

exec "$@"
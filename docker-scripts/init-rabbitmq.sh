#!/bin/bash

sleep 10

RABBITMQ_USER="guest"
RABBITMQ_PASS="guest"
RABBITMQ_HOST="localhost"
RABBITMQ_PORT="15672"

function create_exchange {
    local exchange_name="$1"
    local exchange_type="$2"

    rabbitmqadmin --host=${RABBITMQ_HOST} --port=${RABBITMQ_PORT} --username=${RABBITMQ_USER} --password=${RABBITMQ_PASS} declare exchange name=${exchange_name} type=${exchange_type}
}

create_exchange "my-topic-exchange" "topic"
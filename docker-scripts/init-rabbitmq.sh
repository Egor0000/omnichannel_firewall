#!/bin/bash

# Set RabbitMQ connection details (adjust if needed)
HOST=localhost
PORT=5672
USERNAME=guest
PASSWORD=guest

rabbitmqadmin -H rabbitmq -u "$USERNAME" -p "$PASSWORD" declare exchange name="message.exchange" type=topic durable=true auto_delete=false
rabbitmqadmin -H rabbitmq -u "$USERNAME" -p "$PASSWORD" declare exchange name="reports.exchange" type=topic durable=true auto_delete=false  
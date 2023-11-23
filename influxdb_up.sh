#!/bin/bash

# Specify the container name
container_name="influx_dest"

# Specify the original port
original_port=8086

# Identify the process using the original port
conflicting_pid=$(sudo lsof -t -i :$original_port)

if [ -n "$conflicting_pid" ]; then
    # Kill the conflicting process
    sudo kill $conflicting_pid
    echo "Killed process with PID $conflicting_pid using port $original_port"
fi

# Attempt to restart the Docker container with the original port
docker restart $container_name

# Check if the restart was successful
if [ $? -eq 0 ]; then
    echo "Docker container $container_name restarted successfully"
else
    echo "Failed to restart Docker container $container_name with original port"

    # Find an available port within the specified range
    new_port=""
    for port in {49152..65535}; do
        if ! sudo lsof -i :$port > /dev/null; then
            new_port=$port
            break
        fi
    done

    if [ -z "$new_port" ]; then
        echo "Error: Unable to find an available port in the specified range."
        exit 1
    fi

    # Attempt to restart the Docker container with the new port
    docker run -d --name $container_name -p $new_port:$original_port your_image_name
    echo "Docker container $container_name restarted with a new port $new_port"
fi
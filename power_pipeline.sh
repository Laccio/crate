#!/bin/bash

# Function to be executed on interrupt
cleanup() {
    echo "Script interrupted. Performing cleanup..."
    
    # Additional cleanup actions here
    docker stop $container_name
    # Exit the script
    exit 2
}

# Prompt the user to enter the image name
image_name=crate-image

# Prompt the user to enter the container name
container_name=crate-container

# Prompt the user to enter the output folder
output_folder=crate-out-master

# Request user input for the number of loops
num_loops=2

# Specify the name or ID of your container
CONTAINER_NAME_OR_ID="your_container_name_or_id"



# Trap the interrupt signal (Ctrl+C) and call the cleanup function
trap cleanup INT

# Check if the container exists
if docker ps -a --format '{{.Names}}' | grep -Eq "^$container_name$"; then
    # Check if the container is running
    if docker ps --format '{{.Names}}' | grep -Eq "^$container_name$"; then
        echo "Container is running. Stopping it..."
        docker stop $container_name
    fi

    # Remove the container
    echo "Removing container..."
    docker rm $container_name
else
    echo "Container does not exist."
fi


# Ask the user if the environment is running
read -p "Is the environment running? (yes/no): " running

# Convert the user input to lowercase using tr
running=$(echo "$running" | tr '[:upper:]' '[:lower:]')

# L'environment non sta girando, va avviato
if [ "$running" == "no" ] || [ "$running" == "n" ]; then
    # Start the environment
    ./start_env.sh
    # l'immagine non esiste già, va creata
    if ! docker image inspect $image_name &>/dev/null; then
        # Build the Docker image
        echo "Building $image_name"
        docker build -t $image_name .
        echo "Build $image_name success"
    fi
    # Run the Docker container
    echo "Run 0 started"
    docker run -d -p 127.0.0.1:3005:3000 --name $container_name $image_name
    while true; do
        #Ottieni lo stato del container
        container_status=$(docker inspect --format '{{.State.Status}}' $container_name)
        #Il container è terminato
        if [ "$container_status" == "exited" ]; then
            echo "Run $i ended"
            sleep 10
            # Ottengo i dati campionati
            ./influxdb_csv_collector.sh "$container_name" "$output_folder" "0"
            break
        else
            echo "The container is running. Waiting for 3 minutes..."
            sleep 180  # Wait for 3 minutes (180 seconds)
        fi
    done
    echo "Run 0 ended"
    for ((i = 1; i <= num_loops; i++)); do
        echo "Run $i start"
        # Restart the Docker container
        echo "Restarting the $container_name"
        docker restart "$container_name"
        while true; do
            #Ottieni lo stato del container
            container_status=$(docker inspect --format '{{.State.Status}}' $container_name)
            #Il container è terminato
            if [ "$container_status" == "exited" ]; then
                echo "Run $i ended"
                sleep 10
                # Ottengo i dati campionati
                ./influxdb_csv_collector.sh "$container_name" "$output_folder" "$i"
                break
            else
                echo "The container is running. Waiting for 3 minutes..."
                sleep 180  # Wait for 3 minutes (180 seconds)
            fi
        done
        # Pause for 5 minutes before the next iteration
        #sleep 300
        sleep 15
    done
elif [ "$running" == "yes" ] || [ "$running" == "y" ]; then
    # l'immagine non esiste già, va creata
    if ! docker image inspect $image_name &>/dev/null; then
        # Build the Docker image
        echo "Building $image_name"
        docker build -t $image_name .
        echo "Build $image_name success"
    fi
    # Run the Docker container
    echo "Run 0 started"
    docker run -d -p 127.0.0.1:3005:3000 --name $container_name $image_name
    while true; do
        #Ottieni lo stato del container
        container_status=$(docker inspect --format '{{.State.Status}}' $container_name)
        #Il container è terminato
        if [ "$container_status" == "exited" ]; then
            echo "Run $i ended"
            sleep 10
            # Ottengo i dati campionati
            ./influxdb_csv_collector.sh "$container_name" "$output_folder" "0"
            break
        else
            echo "The container is running. Waiting for 3 minutes..."
            sleep 180  # Wait for 3 minutes (180 seconds)
        fi
    done
    echo "Run 0 ended"
    for ((i = 1; i <= num_loops; i++)); do
        echo "Run $i start"
        # Restart the Docker container
        echo "Restarting the $container_name"
        docker restart "$container_name"
        while true; do
            #Ottieni lo stato del container
            container_status=$(docker inspect --format '{{.State.Status}}' $container_name)
            #Il container è terminato
            if [ "$container_status" == "exited" ]; then
                echo "Run $i ended"
                # Ottengo i dati campionati
                sleep 10
                ./influxdb_csv_collector.sh "$container_name" "$output_folder" "$i"
                break
            else
                echo "The container is running. Waiting for 3 minutes..."
                sleep 180  # Wait for 3 minutes (180 seconds)
            fi
        done
        # Pause for 5 minutes before the next iteration
        #sleep 300
        sleep 15
    done
fi







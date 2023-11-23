#!/bin/bash

# Check if the required arguments are provided
if [ "$#" -ne 3 ]; then
    echo "Usage: $0 <container_name> <output_folder> <i>"
    exit 1
fi

# Assign arguments to variables
container_name="$1"
output_folder="$2"
i="$3"

# Verify if the output folder exists; create it if not
if [ ! -d "$output_folder" ]; then
    mkdir -p "$output_folder"
fi

# Get the container's started-at timestamp
started_at=$(docker inspect --format='{{.State.StartedAt}}' "$container_name")
echo "started_at: $started_at"

# Convert the timestamp to a Unix timestamp
start_timestamp=$(date -d "$started_at" +%s)

# Get the container's stopped-at timestamp
stopped_at=$(docker inspect --format='{{.State.FinishedAt}}' "$container_name")
echo "stopped_at: $stopped_at"

# Convert the timestamp to a Unix timestamp
stopped_timestamp=$(date -d "$stopped_at" +%s)

# Calculate the uptime in seconds
uptime_seconds=$((stopped_timestamp - start_timestamp))

# Print the uptime
echo "The $container_name container has been running for $uptime_seconds seconds."

# Create a current timestamp in seconds
timestamp=$(date '+%Y%m%d_%H%M%S')

# Specify the output folder for the CSV file
csv_output_path="$output_folder/ec_${timestamp}_$i.csv"


STARTED_AT=$(docker inspect --format='{{.State.StartedAt}}' "$container_name")
# Get container end time from docker inspect
END_TIME=$(docker inspect --format='{{.State.FinishedAt}}' "$container_name")

# Check if either timestamp is empty
if [ -z "$STARTED_AT" ] || [ -z "$END_TIME" ]; then
    echo "Error: Unable to retrieve timestamps."
    exit 1
fi

# Open a new terminal and execute the command
gnome-terminal --wait -- bash -c "docker logs --timestamps $container_name | awk -v start=\"$STARTED_AT\" -v end=\"$END_TIME\" '\$1 >= start && \$1 <= end {print}' > \"$output_folder/ec_log_${timestamp}_$i.txt\""

# Optionally, add a delay to allow time for the logs to be collected
sleep 5


# Execute the InfluxDB query with the calculated uptime
query="SELECT sum(\"power\") FROM \"power_consumption\" WHERE target='$container_name' AND \"time\" >= '$started_at' AND \"time\" <= '$stopped_at' GROUP BY time(1s), \"target\""

# Execute the query with curl and capture the HTTP status code
response=$(curl -s -o "$csv_output_path" -w "%{http_code}" -H 'Accept: application/csv' -G 'http://127.0.0.1:8086/query?db=power_consumption2' --data-urlencode "q=$query")

# Capture the HTTP status code in a variable
http_status=$(echo "$response" | tail -n1)

# Check the HTTP status code
if [ "$http_status" -eq 200 ]; then
    echo "Query executed successfully. The $container_name container has been recorded."
else
    echo "Error during query execution. HTTP status code: $http_status"
fi

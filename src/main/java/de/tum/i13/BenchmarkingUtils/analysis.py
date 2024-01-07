import csv

# Open the CSV file
with open('data.csv', newline='') as csvfile:
    reader = csv.reader(csvfile, delimiter=',')

    # Iterate through each row in the CSV file
    for row in reader:
        name = row[0]  # Get the name (first value)
        values = [int(value) for value in row[1:]]  # Convert values to integers

        # Calculate the average of the values
        avg_value = sum(values) / len(values) if len(values) > 0 else 0

        # Print the name and the average of the values
        print(f"Name: {name}, Average of Values: {avg_value}")
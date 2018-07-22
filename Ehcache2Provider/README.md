# Ehcache 2 Provider

## Running this provider

### Start the Terracotta Server

### Run the application

Open a second terminal and change into the directory where you have this sample.

Run the first client, which will create a distributed cache and put data into it:

  - `mvn exec:exec -P programmatic`

Run the second client, which will connect to the distributed cache and read the data written by the first client.

  - `mvn exec:exec -P xml`

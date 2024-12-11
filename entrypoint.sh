#!/bin/sh

# Start the Ollama server in the background
ollama serve &

# Wait a moment to ensure the server is up
sleep 5

# Pull the required model
ollama pull llama3

# Keep the server running in the foreground
wait

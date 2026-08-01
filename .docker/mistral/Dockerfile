ARG BASE_IMAGE=poetassistant-agent-base:latest
FROM ${BASE_IMAGE}

# Run the official Mistral CLI installation script.
RUN curl -LsSf https://mistral.ai/vibe/install.sh | bash

# Verify installations.
RUN vibe --version

CMD ["vibe"]

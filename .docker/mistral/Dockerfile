ARG BASE_IMAGE=poetassistant-agent-base:latest
FROM ${BASE_IMAGE}

# Run the official Mistral CLI installation script.
RUN curl -LsSf https://mistral.ai/vibe/install.sh | bash

# Install Android skill for mistral-vibe
RUN android skills add --all --agent=mistral-vibe && \
    mkdir -p "${HOME}/.agents/skills" && \
    mv "${HOME}/.vibe/skills" "${HOME}/.agents/."


# Verify installations.
RUN vibe --version

CMD ["vibe"]

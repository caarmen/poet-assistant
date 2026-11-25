ARG BASE_IMAGE=poetassistant-agent-base:latest
FROM ${BASE_IMAGE} AS base

# Install Android skill for mistral-vibe
RUN android skills add --all --agent=mistral-vibe && \
    mkdir -p "${HOME}/.agents/skills"

# Run the official Mistral CLI installation script.
FROM base AS installmistral
RUN curl -LsSf https://mistral.ai/vibe/install.sh | bash

RUN mv "${HOME}/.vibe/skills" "${HOME}/.agents/."

# Verify installations.
RUN vibe --version

CMD ["vibe"]

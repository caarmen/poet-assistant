ARG BASE_IMAGE=poetassistant-agent-base:latest
FROM ${BASE_IMAGE}

# Install codex
RUN curl -fsSL https://chatgpt.com/codex/install.sh | sh

# Verify installations.
RUN codex --version

CMD ["codex"]

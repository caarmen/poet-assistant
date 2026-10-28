ARG BASE_IMAGE=poetassistant-agent-base:latest
FROM ${BASE_IMAGE}

# Install codex
RUN curl -fsSL https://chatgpt.com/codex/install.sh | sh

# Install Android skill for codex
RUN android skills add --all --agent=codex && \
    mkdir -p "${HOME}/.agents/skills" && \
    mv "${HOME}/.codex/skills" "${HOME}/.agents/."

# Verify installations.
RUN codex --version

CMD ["codex"]

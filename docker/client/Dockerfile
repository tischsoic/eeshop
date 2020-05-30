FROM openjdk:8u242-jdk

ARG NPM_VERSION=6.8

RUN apt update && apt install -y \
  unzip \
  vim \
  git \
  curl

# Install npm
RUN \
  curl -sL https://deb.nodesource.com/setup_12.x | bash - && \
  apt-get install nodejs && \
  npm install -g npm@$NPM_VERSION && \
  node -v && \
  npm -v

ENV APP_DIR=/home/client
WORKDIR ${APP_DIR}

COPY docker/client/entrypoint.sh /
RUN chmod +x /entrypoint.sh
ENTRYPOINT [ "/bin/sh" ]
CMD [ "/entrypoint.sh" ]

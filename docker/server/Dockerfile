FROM openjdk:8u242-jdk

ARG SBT_VERSION=1.3.8
ARG SCALA_VERSION=2.12.8

RUN apt update && apt install -y \
  unzip \
  vim \
  git \
  curl

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get install sbt && \
  sbt sbtVersion

# Install scala
RUN \
  curl -L -o scala-$SCALA_VERSION.tgz https://downloads.lightbend.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz && \
  tar xvf scala-$SCALA_VERSION.tgz && \
  mv scala-$SCALA_VERSION /usr/lib && \
  ln -s /usr/lib/scala-$SCALA_VERSION /usr/lib/scala && \
  echo "export PATH=$PATH:/usr/lib/scala/bin" >> ~/.bashrc && \
  rm scala-$SCALA_VERSION.tgz

ENV APP_DIR=/home/server
WORKDIR ${APP_DIR}

COPY docker/server/entrypoint.sh /
RUN chmod +x /entrypoint.sh
ENTRYPOINT [ "/bin/sh" ]
CMD [ "/entrypoint.sh" ]

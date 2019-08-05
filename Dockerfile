FROM gradle:5.4.1-jdk8

WORKDIR /bouffier-java
COPY --chown=gradle:gradle . /bouffier-java
COPY ./tests/resource /bouffier-java-project

CMD ["gradle", "run"]
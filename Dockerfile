FROM gradle:5.4.1-jdk8

WORKDIR /project
COPY --chown=gradle:gradle . /project
COPY ./tests/resource /bouffier-java

CMD ["gradle", "run"]
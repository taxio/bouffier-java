FROM gradle:5.4.1-jdk8

WORKDIR /project
ADD --chown=gradle:gradle . /project
ADD ./samples /bouffier-java

CMD ["gradle", "run"]
FROM gradle:jdk8

# install git
RUN apt-get update
RUN apt-get install -y git

RUN git clone https://github.com/openlibraryenvironment/mock-ncip-server.git

WORKDIR mock-ncip-server
RUN gradle build
EXPOSE 8080


CMD ["gradle", "--stacktrace", "appRun"]
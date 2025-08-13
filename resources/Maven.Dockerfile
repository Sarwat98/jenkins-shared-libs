FROM alpine
RUN apk add -U maven openjdk11
#ARG -u root
RUN apk add sudo
RUN adduser admin --disabled-password
#RUN addgroup admin wheel
#RUN echo '%wheel ALL=(ALL:ALL) NOPASSWD: ALL' > /etc/sudoers

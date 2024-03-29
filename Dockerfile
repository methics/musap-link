# Build stage
FROM maven:3-adoptopenjdk-8 as builder

COPY ./ /usr/src/musap-link

WORKDIR /usr/src/musap-link

RUN mvn clean package


# Running stage
FROM tomcat:9

# Set the working directory in the container
WORKDIR /usr/local/tomcat

# Copy the WAR file into the Tomcat webapps directory
COPY --from=builder /usr/src/musap-link/target/MUSAPLink-*.war /usr/local/tomcat/webapps/musaplink.war

COPY ./conf/musaplink.policy /usr/local/tomcat/conf/

COPY ./conf/musaplink.conf /usr/local/tomcat/conf/

# Append custom policy to the default catalina.policy
RUN cat /usr/local/tomcat/conf/musaplink.policy >> /usr/local/tomcat/conf/catalina.policy

#COPY ./logging.properties /usr/local/tomcat/conf/
# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run Tomcat server
CMD ["catalina.sh", "run"]

# MUSAP Link

MUSAP Link is an optional Java server component for the MUSAP library. MUSAP Link allows integrating the MUSAP library to external web services with a simple REST API.

MUSAP link library does not store any critical user data. It only stores push notification tokens and transport security keys for each registered MUSAP client library. Registering a MUSAP library to the link library is always optional and a user choice.

Each MUSAP link can be integrated with any number of apps using the MUSAP library. The MUSAP link server URL must be configured on the MUSAP library to enable the connectivity between the link library and the MUSAP library.

MUSAP library can be found here:
* [MUSAP Android](https://github.com/methics/musap-android)
* [MUSAP iOS](https://github.com/methics/musap-ios)

MUSAP Link API description can be found here:
* [MUSAP API](https://github.com/methics/musap-api)

## Features
- **RESTful Integration**: MUSAP Link provides a straightforward REST API for seamless integration with external web services.
- **External SSCD Integration**: MUSAP Link supports the ETSI TS 102 204 API for integrating external SSCDs.
- **Push Notification Support**: MUSAP Link provides simple push notification integration, supporting both APNs and FCM.
- **Database Compatibility**: Supports external databases, with recommended options being PostgreSQL or SQLite3.

## Architecture

![Link_Library_Architecture](https://github.com/methics/musap-link/assets/4453264/2997cae8-d44e-4709-8192-ae31624eff62)

## Project Structure

![MUSAP_Link_Project_Structure](https://github.com/methics/musap-link/assets/4453264/a3a63099-5a80-4842-995a-6ad214505131)

## Installation

### Building

MUSAP Link can be built with:

`mvn package`

### Database

MUSAP Link requires an external database. We recommend either PostgreSQL or SQLite3.
The SQL schema can be found [here](https://github.com/methics/musap-link/blob/main/conf/musaplink.sql). 

## Usage

Installer needs to create musaplink.conf and fill PostgreSQL details. The configuration is copied into
/usr/local/tomcat/conf.

`cp musaplink.conf.sample musaplink.conf`

### Building docker image

`docker build -t musap-link .`

### Running the container

`docker run -p 8080:8080 musap-link:latest`

## Configuration

Configuration is done via standard Java properties. Example configuration can be found [here](https://github.com/methics/musap-link/blob/main/conf/musaplink.conf.sample).

### Push Notification

Both APNs and FCM are supported for push notifications. 

Example configuration:
```
# FCM
musaplink.fcm.projectid = 
musaplink.fcm.projectname = 
musaplink.fcm.apikey    = 
musaplink.fcm.debug     = true
musaplink.google.services.file =

# APNs
musaplink.apns.teamid = 
musaplink.apns.keyid = 
musaplink.apns.signingkey =
musaplink.apns.topic    = 
musaplink.apns.debug     = true
```

### External SSCD

For external SSCDs, MUSAP link currently supports the ETSI TS 102 204 API.
Example SSCD configuration:
```
musaplink.client.clientid.1                 = LOCAL
musaplink.client.apid.1                     = 
musaplink.client.appwd.1                    = 
musaplink.client.keystore.1                 = /tmp/keystore
musaplink.client.keystore.pwd.1             = 
musaplink.client.keystore.type.1            = JKS
musaplink.client.keystore.provider.1        = SUN
musaplink.client.signatureurl.1             = 
musaplink.client.statusurl.1                = 
musaplink.client.registrationurl.1          = 
musaplink.client.profileurl.1               = 
```

### Database

Example DB configuration:
```
musaplink.db.url          = jdbc:postgresql://localhost:5432/musaplink
musaplink.db.username     = musap
musaplink.db.password     = musap
musaplink.db.driver.class = org.postgresql.Driver
```

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

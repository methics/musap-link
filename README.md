# MUSAP Link

MUSAP Link is an optional Java server component for the MUSAP library. MUSAP Link allows integrating the MUSAP library to external web services with a simple REST API.

MUSAP library can be found here:
* https://github.com/methics/musap-android
* https://github.com/methics/musap-ios

## Building

MUSAP Link can be built with:

`mvn package`

## Database

MUSAP Link requires an external database. We recommend either PostgreSQL or SQLite3.
The SQL schema can be found [here](https://github.com/methics/musap-link/blob/main/conf/musaplink.sql). 

## Running

We recommend running MUSAP with:

`mvn jetty:run`

## Configuration

Configuration is done via standard Java properties. Example configuration can be found [here](https://github.com/methics/musap-link/blob/main/conf/musaplink.conf.sample)https://github.com/methics/musap-link/blob/main/conf/musaplink.conf.sample.

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

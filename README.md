# AEM MaxMind Integration

The AEM [Maxmind](https://www.maxmind.com/) integration allows retrieving geolocation information using the MaxMind GeoIP2 or GeoLite2 databases.  

You can download the free MaxMind GeoLite2 City and ASN databases directly from MaxMind at [https://dev.maxmind.com/geoip/geoip2/geolite2/](https://dev.maxmind.com/geoip/geoip2/geolite2/)

## Installation Instructions

Upload the MaxMind GeoIP2 or GeoLite2 database in DAM, e.g. at `/content/dam/maxmind/GeoLite2-City.mmdb`

### Configuration
AEM MaxMind Integration requires an OSGi configuration before it gets active.
To enable it create a configuration for PID `com.ykozlov.aem.maxmind.impl.GeoIPServiceImpl`, e.g.
```text
/apps/my-app/config/com.ykozlov.aem.maxmind.impl.GeoIPServiceImpl
```
and set the `dbPath` property to point to the GeoIP2 database in DAM:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0" xmlns:jcr="http://www.jcp.org/jcr/1.0"
          jcr:primaryType="sling:OsgiConfig"
          dbPath="/content/dam/maxmind/GeoLite2-City.mmdb"/>
```


## Usage

### Postal Code by IP

```java
    GeoIPService geoService = sling.getService(GeoIPService.class);
    InetAddress ipAddress = InetAddress.getByName("128.101.101.101");
    String postalCode = geoService.getPostalCode(ipAddress);

```

## How to build

To build all the modules run in the project root directory the following command with Maven 3:

    mvn clean install

To build all the modules and deploy the `all` package to a local instance of AEM, run in the project root directory the following command:

    mvn clean install -PautoInstallPackage


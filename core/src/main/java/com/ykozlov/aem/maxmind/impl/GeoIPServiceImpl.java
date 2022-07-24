package com.ykozlov.aem.maxmind.impl;

import com.day.cq.dam.api.Asset;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.ykozlov.aem.maxmind.GeoIPService;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Optional;

@Component(
        configurationPolicy = ConfigurationPolicy.REQUIRE
)
@Designate(ocd = GeoIPServiceImpl.Configuration.class)
public class GeoIPServiceImpl implements GeoIPService {
    private static final Logger logger = LoggerFactory.getLogger(GeoIPServiceImpl.class);

    private static final String SERVICE_NAME = "maxmind-reader";

    @ObjectClassDefinition(name = "Geo Lookup Configuration")
    public @interface Configuration {
        @AttributeDefinition(name = "GeoIP2 Database Path", description = "The GeoIP2 database file to use", type = AttributeType.STRING)
        String dbPath();

        @AttributeDefinition(name = "Cache lookups", description = "Improve lookup performance by using a simple in-memory cache",
                type = AttributeType.BOOLEAN)
        boolean cacheLookups() default true;
    }

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private DatabaseReader reader;

    @Activate
    protected void activate(Configuration config) throws IOException, LoginException {
        initialize(config);
    }

    @Deactivate
    protected void deactivate() {
        try {
            reader.close();
        } catch (IOException e) {
            logger.error("failed to close database reader", e);
        }
    }

    void initialize(Configuration config) throws IOException, LoginException {
        try (ResourceResolver resolver = resourceResolverFactory.getServiceResourceResolver(
                Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SERVICE_NAME))) {
            Resource resource = resolver.resolve(config.dbPath());
            InputStream inputStream;
            if (resource.isResourceType("dam:Asset")) {
                Asset asset = resource.adaptTo(Asset.class);
                inputStream = asset.getOriginal().getStream();
            } else if (resource.isResourceType("nt:file")) {
                inputStream = resource.getChild("jcr:content")
                        .getValueMap().get("jcr:data", InputStream.class);
            } else {
                throw new IllegalArgumentException("GeoIP2 is not a dam:Asset or nt:file");
            }

            try {
                DatabaseReader.Builder builder = new DatabaseReader.Builder(inputStream);
                if(config.cacheLookups()){
                    builder.withCache(new CHMCache());
                }
                reader = builder.build();
            } finally {
                inputStream.close();
            }
        }
    }

    @Override
    public String getPostalCode(InetAddress ipAddress) throws IOException {
        try {
            Optional<CityResponse> response = reader.tryCity(ipAddress);

            return response.map(cityResponse -> cityResponse.getPostal().getCode()).orElse(null);
        } catch (GeoIp2Exception e) {
            throw new IOException("Failed to fetch postal code for " + ipAddress, e);
        }
    }

}

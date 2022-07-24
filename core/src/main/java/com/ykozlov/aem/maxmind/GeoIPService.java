package com.ykozlov.aem.maxmind;

import java.io.IOException;
import java.net.InetAddress;

public interface GeoIPService {
    String getPostalCode(InetAddress ipAddress) throws IOException;
}

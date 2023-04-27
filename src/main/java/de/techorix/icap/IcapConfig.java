package de.techorix.icap;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "icap")
public interface IcapConfig {

    boolean isEnabled();

    String hostname();

    int port();

    String servicename();

    String username();

    String requestSource();
}

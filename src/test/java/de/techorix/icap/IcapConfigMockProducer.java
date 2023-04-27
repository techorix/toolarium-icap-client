package de.techorix.icap;

import io.smallrye.config.SmallRyeConfig;
import org.eclipse.microprofile.config.Config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class IcapConfigMockProducer {

        @Inject
        Config config;

        @Produces
        @ApplicationScoped
        @io.quarkus.test.Mock
        IcapConfig icap() {
            return config.unwrap(SmallRyeConfig.class).getConfigMapping(IcapConfig.class);
        }
}

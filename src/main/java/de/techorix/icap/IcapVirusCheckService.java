package de.techorix.icap;

import com.github.toolarium.icap.client.ICAPClientFactory;
import com.github.toolarium.icap.client.dto.*;
import com.github.toolarium.icap.client.exception.ContentBlockedException;
import io.smallrye.common.annotation.Blocking;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

@ApplicationScoped
public class IcapVirusCheckService {

    @Inject
    IcapConfig config;

    @Inject
    Logger log;

    @Blocking
    public boolean isInfected(String name, InputStream inputStream) {
        // if disabled exe files are reported infected
        if (!config.isEnabled()) {
            log.debug("Dataport ICAP Service not configured. Using mock feature.");
            return name.endsWith("exe");
        }

        // this is a blocking request due to the usage of InputStream
        try {

            // length calculation like this is only OK for ByteArrayInputStream
            // we have to make sure not to actually consume the InputStream because it will be consumed in the client once again
            // it is used in case our file is smaller than the servers preview size
            // cf. ICAPClientImpl.java
            int size = inputStream.available();
            log.debug("Relaying virus checking for file named <" + name + "> and size <" + size + ">.");

            ICAPClientFactory.getInstance().getICAPClient(
                            config.hostname(),
                            config.port(),
                            config.servicename())
                    .validateResource(
                            ICAPMode.REQMOD,
                            new ICAPRequestInformation(config.username(), config.requestSource()),
                            new ICAPResource(name, inputStream, size));

            // If no exception is thrown the resource can be used and is valid.
            log.debug("No virus found for InputStream <" + name + ">.");
        } catch (IOException ioe) {  // I/O error

            log.error("Resource could not be accessed: " + ioe.getMessage(), ioe);

            throw new RuntimeException(ioe);

        } catch (IndexOutOfBoundsException ioobe) {

            log.warn("MODULF-1792: I/O Stream Bug in Quarkus or Toolarium ICAP implementation does not conform with Dataport ICAP Service", ioobe);

            return true;

        } catch (ContentBlockedException e) { // !!! The resource has to be blocked !!!

            // The e.getMessage() gives technical the proper information. It's already logged by the library.

            // The ICAP header contains structured information about virus.
            ICAPHeaderInformation icapHeaderInformation = e.getICAPHeaderInformation();
            icapHeaderInformation.getHeaderValues(ICAPConstants.HEADER_KEY_X_VIOLATIONS_FOUND);
            icapHeaderInformation.getHeaderValues(ICAPConstants.HEADER_KEY_X_INFECTION_FOUND);

            log.warn(e.getContent());
            // The e.getContent() contains the returned error information from the ICAP-Server.
            // It can be ignored as long as the resource is blocked; otherwise it gives a well structured response.
            return true;
        }

        return false;
    }

}

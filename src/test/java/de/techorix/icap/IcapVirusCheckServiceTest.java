package de.techorix.icap;

import com.github.toolarium.icap.client.ICAPClientFactory;
import com.github.toolarium.icap.client.impl.ICAPClientImpl;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@QuarkusTest
class IcapVirusCheckServiceTest {

    @InjectMock
    IcapConfig icap;
    @Inject
    IcapVirusCheckService sut;

    @BeforeEach
    void before() {
        Mockito.when(icap.hostname()).thenReturn("localhost");
        Mockito.when(icap.requestSource()).thenReturn("file");
        Mockito.when(icap.servicename()).thenReturn("srv_clamav");
        Mockito.when(icap.username()).thenReturn("user");
    }

    @Test
    public void checkVirusInfectedWhenDataportIcapActsWeirdMODULF1792() throws Exception {
        Mockito.when(icap.isEnabled()).thenReturn(true);

        ICAPClientImpl mockedClient = mock(ICAPClientImpl.class);
        Mockito.when(mockedClient.validateResource(Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new IndexOutOfBoundsException());
        ICAPClientFactory mockedFactoryDynamic = mock(ICAPClientFactory.class);
        Mockito.when(mockedFactoryDynamic.getICAPClient(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString())).thenReturn(mockedClient);

        try (MockedStatic<ICAPClientFactory> factory = Mockito.mockStatic(ICAPClientFactory.class)) {
            factory.when(ICAPClientFactory::getInstance).thenReturn(mockedFactoryDynamic);
            assertTrue(sut.isInfected("name", new ByteArrayInputStream("teststring".getBytes())));
        }
    }

    @Test
    public void checkNotInfectedWhenDisabled() throws Exception {
        Mockito.when(icap.isEnabled()).thenReturn(false);
        assertFalse(sut.isInfected("name", new ByteArrayInputStream("teststring".getBytes())));
    }

    @Test
    public void checkInfectedWhenDisabled() throws Exception {
        Mockito.when(icap.isEnabled()).thenReturn(false);
        assertTrue(sut.isInfected("name.exe", new ByteArrayInputStream("teststring".getBytes())));
    }
}

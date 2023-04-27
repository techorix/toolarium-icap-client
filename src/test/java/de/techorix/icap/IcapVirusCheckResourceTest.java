package de.techorix.icap;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.specification.MultiPartSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@Testcontainers
@TestHTTPEndpoint(IcapVirusCheckResource.class)
class IcapVirusCheckResourceTest {

    static final String WORKING_DIR = "src/test/resources";
    static final String TEST_FILE = "FileNeedsToBeSanitized.pdf";
    static final String TEST_FILE_VIRUS = "FileNeedsToBeSanitized.exe";
    static final String ANTIVIRUS_TEST_FILE = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*";
    static final String IMAGE_NAME = "toolarium/toolarium-icap-calmav-docker:0.0.1";
    static final int ICAP_PORT_NUMBER = 1344;

    @InjectMock
    IcapConfig icap;

    // By using TestContainers this actually becomes an Integration-Test
    @Container
    static final GenericContainer<?> ICAP_CONTAINER = new GenericContainer<>(DockerImageName.parse(IMAGE_NAME))
            .withExposedPorts(ICAP_PORT_NUMBER)
            .waitingFor(Wait.forListeningPort());

    @BeforeAll
    static void beforeAll() {
        ICAP_CONTAINER.start();
    }

    @BeforeEach
    void before() {
        Mockito.when(icap.hostname()).thenReturn("localhost");
        Mockito.when(icap.requestSource()).thenReturn("file");
        Mockito.when(icap.servicename()).thenReturn("srv_clamav");
        Mockito.when(icap.username()).thenReturn("user");

        Mockito.when(icap.port()).thenReturn(ICAP_CONTAINER.getMappedPort(ICAP_PORT_NUMBER));
        Mockito.when(icap.isEnabled()).thenReturn(true);
    }

    @Test
    @TestSecurity(user = "user", roles = {"User"})
    void checkVirusHappyCase() throws Exception {
        Path workingDir = Path.of("", WORKING_DIR);
        Path file = workingDir.resolve(TEST_FILE);

        given().multiPart(getMultiPartSpecification(file))
                .multiPart("name", TEST_FILE)
                .expect().statusCode(200)
                .and().expect().body(is("no virus detected"))
                .when().post();
    }

    @Test
    @TestSecurity(user = "user", roles = {"User"})
    public void checkVirusInfected() throws Exception {
        // this is the real virus test!
        given().multiPart(getMultiPartSpecificationVirus())
                .multiPart("name", "testfile.txt")
                .expect().statusCode(409)
                .when().post();
    }

    @Test
    @TestSecurity(user = "user", roles = {"User"})
    public void checkVirusInfectedWhenDisabled() throws Exception {
        Mockito.when(icap.isEnabled()).thenReturn(false);

        Path workingDir = Path.of("", WORKING_DIR);
        Path file = workingDir.resolve(TEST_FILE_VIRUS);

        given().multiPart(getMultiPartSpecification(file))
                .multiPart("name", TEST_FILE_VIRUS)
                .expect().statusCode(409)
                .when().post();
    }


    @Test
    @TestSecurity(user = "user", roles = {"User"})
    public void testVersionEndpoint() {
        given()
                .when().get()
                .then()
                .statusCode(200)
                .body(is("this is api v1 of virus checker"));
    }

    @Test
    public void testVersionEndpointWithoutCredentials() {
        given()
                .when().get()
                .then()
                .statusCode(401);
    }

    private MultiPartSpecification getMultiPartSpecification(Path file) throws Exception {
        return new MultiPartSpecBuilder(new FileInputStream(file.toFile()).readAllBytes()).
                fileName(file.getFileName().toString()).
                controlName("file").
                mimeType(Files.probeContentType(file)).
                build();
    }

    private MultiPartSpecification getMultiPartSpecificationVirus() {
        return new MultiPartSpecBuilder(ANTIVIRUS_TEST_FILE.getBytes()).
                fileName("").
                controlName("file").
                mimeType("application/text").
                build();
    }
}

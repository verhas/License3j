package javax0.license3j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

public class SimpleLicenseTest {

    @Test
    void testSimpleLicense() throws NoSuchAlgorithmException {
        // snippet CREATE_SIMPLE_LICENSE
        final var lic = SimpleLicense
                            .withSecret("abraka dabra")
                            .forValue("my special user");
        final var code = lic.toString();
        // end snippet
        //snippet CHECK_SIMPLE_LICENSE
        Assertions.assertTrue(lic.isOK(code));
        // end snippet
        final var lic2 = SimpleLicense.withSecret("abraka dabra").forValue("my special user");
        Assertions.assertTrue(lic2.isOK(code));
    }

}

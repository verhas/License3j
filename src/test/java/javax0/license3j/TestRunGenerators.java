package javax0.license3j;

import javax0.geci.engine.Geci;
import javax0.geci.iterate.Iterate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestRunGenerators {

    @Test
    @DisplayName("run Iterate on the sources")
    void runIterate() throws IOException {
        Geci geci = new Geci();
        Assertions.assertFalse(
            geci.register(Iterate.builder()
                              .define(ctx -> ctx.segment().param("TYPE", ctx.segment().getParam("Type").orElse("").toUpperCase()))
                              .build())
                .generate()
            , geci.failed()
        );
    }
}

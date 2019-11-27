package javax0.license3j;

import javax0.geci.engine.Geci;
import javax0.geci.iterate.Iterate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static javax0.geci.tools.CaseTools.lcase;

public class TestRunGenerators {

    @Test
    @DisplayName("run Iterate on the sources")
    public void testRunIterate() throws Exception {
        Geci geci = new Geci();
        Assertions.assertFalse(
            geci.register(Iterate.builder()
                              .define(ctx -> {
                                      if (ctx.segment().getParam("Type").isPresent()) {
                                          ctx.segment().param("TYPE", ctx.segment().getParam("Type").orElse("").toUpperCase());
                                          ctx.segment().param("type", lcase(ctx.segment().getParam("Type").orElse("")));
                                      }
                                  }
                              )
                              .build())
                .generate()
            , geci.failed()
        );
    }
}

package works.weave.socks.cart.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import works.weave.socks.cart.cart.CartDAO;
import works.weave.socks.cart.cart.CartResource;
import works.weave.socks.cart.entities.Cart;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracer;
import io.opencensus.*;

@RestController
@RequestMapping(path = "/carts")
public class CartsController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final Tracer tracer = Tracing.getTracer();

    @Autowired
    private CartDAO cartDAO;

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public Cart get(@PathVariable String customerId) {
        Span currentSpan = tracer.spanBuilder("GET cart/customerId" ).startSpan();
        Cart cart = new CartResource(cartDAO, customerId).value().get();
        currentSpan.end();
        return cart;
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @RequestMapping(value = "/{customerId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable String customerId) {
        Span currentSpan = tracer.spanBuilder("DELETE cart/customerId" ).startSpan();
        new CartResource(cartDAO, customerId).destroy().run();
        currentSpan.end();
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @RequestMapping(value = "/{customerId}/merge", method = RequestMethod.GET)
    public void mergeCarts(@PathVariable String customerId, @RequestParam(value = "sessionId") String sessionId) {
        Span currentSpan = tracer.spanBuilder("GET cart/customerId.merge" ).startSpan();
        logger.debug("Merge carts request received for ids: " + customerId + " and " + sessionId);
        CartResource sessionCart = new CartResource(cartDAO, sessionId);
        CartResource customerCart = new CartResource(cartDAO, customerId);
        customerCart.merge(sessionCart.value().get()).run();
        delete(sessionId);
        currentSpan.end();
    }
}

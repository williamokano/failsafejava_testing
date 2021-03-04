package dev.okano.failsafejava;

import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class XptoService {

    private final RetryPolicy<XptoResponse> retryPolicy;
    private final Timeout<XptoResponse> timeout;
    private final XptoApi xptoApi;

    @Autowired
    public XptoService(RetryPolicy<XptoResponse> retryPolicy, Timeout<XptoResponse> timeout, XptoApi xptoApi) {
        this.retryPolicy = retryPolicy;
        this.timeout = timeout;
        this.xptoApi = xptoApi;
    }

    public XptoResponse request() {
        return Failsafe.with(timeout, retryPolicy)
                .get(xptoApi::request);
    }
}

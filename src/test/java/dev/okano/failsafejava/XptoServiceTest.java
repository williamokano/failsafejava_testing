package dev.okano.failsafejava;

import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.Timeout;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.mockito.internal.stubbing.answers.AnswersWithDelay;
import org.mockito.internal.stubbing.answers.Returns;
import org.springframework.util.StopWatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class XptoServiceTest {

    private final FailsafeConfiguration failsafeConfig = new FailsafeConfiguration();
    private final RetryPolicy<XptoResponse> retryPolicy = failsafeConfig.retryPolicy();
    private final Timeout<XptoResponse> timeout = failsafeConfig.timeoutPolicy();

    private final XptoApi xptoApi = mock(XptoApi.class);
    private final XptoService underTest = new XptoService(retryPolicy, timeout, xptoApi);

    @Test
    void shouldExecute1AttemptWhenSuccessful() {
        when(xptoApi.request()).thenReturn(new XptoResponse(200, "OK"));

        XptoResponse response = underTest.request();

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getResponse()).isEqualTo("OK");

        verify(xptoApi).request();
        verifyNoMoreInteractions(xptoApi);
    }

    @Test
    void shouldExecute2AttemptsWhenException() {
        when(xptoApi.request())
                .thenThrow(new RuntimeException("Something bad happened"))
                .thenReturn(new XptoResponse(200, "OK"));

        XptoResponse response = underTest.request();

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getResponse()).isEqualTo("OK");

        verify(xptoApi, times(2)).request();
        verifyNoMoreInteractions(xptoApi);
    }

    @Test
    void shouldExecute2AttemptsWhenStatusCodeNot200() {
        StopWatch stopWatch = new StopWatch();
        when(xptoApi.request())
                .thenReturn(new XptoResponse(500, "WUT"))
                .thenReturn(new XptoResponse(200, "OK"));

        stopWatch.start();
        XptoResponse response = underTest.request();
        stopWatch.stop();

        assertThat(stopWatch.getTotalTimeSeconds() < 3).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getResponse()).isEqualTo("OK");

        verify(xptoApi, times(2)).request();
        verifyNoMoreInteractions(xptoApi);
    }

    @Test
    void shouldMockitoChainExpectations() {
        when(xptoApi.request())
                .thenAnswer(new AnswersWithDelay(2000, new Returns(new XptoResponse(500, "WUT"))))
                .thenReturn(new XptoResponse(200, "OK"));

        XptoResponse r1 = xptoApi.request();
        XptoResponse r2 = xptoApi.request();

        assertThat(r1.getStatusCode()).isEqualTo(500);
        assertThat(r2.getStatusCode()).isEqualTo(200);
    }

    @Test
    void shouldThrowTimeoutExceptionIfExecutionTakesMoreThan3Seconds() {
        StopWatch stopWatch = new StopWatch();
        when(xptoApi.request())
                .thenAnswer(new AnswersWithDelay(2000, new Returns(new XptoResponse(500, "WUT"))))
                .thenAnswer(new AnswersWithDelay(2000, new Returns(new XptoResponse(200, "OK"))));

        stopWatch.start();
        assertThatThrownBy(underTest::request);
        stopWatch.stop();
        double totalExecutionInSeconds = stopWatch.getTotalTimeSeconds();

        assertThat(totalExecutionInSeconds).isEqualTo(3, Offset.offset(0.3));

        verify(xptoApi, times(2)).request();
    }

}

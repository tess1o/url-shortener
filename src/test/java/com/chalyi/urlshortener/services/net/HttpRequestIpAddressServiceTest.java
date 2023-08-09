package com.chalyi.urlshortener.services.net;

import com.chalyi.urlshortener.BaseTest;
import com.chalyi.urlshortener.SpringBootTestWithDirtyContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTestWithDirtyContext
@Slf4j
class HttpRequestIpAddressServiceTest extends BaseTest {

    public static final String EXPECTED_IP_ADDRESS = "10.10.1.25";
    public static final String UNKNOWN = "unknown";
    public static final String LOCALHOST = "127.0.0.1";

    @ParameterizedTest
    @ValueSource(strings = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"})
    public void testWithHeaders(String header) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(header)).thenReturn(EXPECTED_IP_ADDRESS);

        HttpRequestIpAddressService service = new HttpRequestIpAddressService();
        Assertions.assertEquals(EXPECTED_IP_ADDRESS, service.getIpAddress(request).getHostAddress(),
                "IP address should be " + EXPECTED_IP_ADDRESS);
    }

    @ParameterizedTest
    @ValueSource(strings = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"})
    public void testWithHeadersUnknown(String header) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(header)).thenReturn(UNKNOWN);

        HttpRequestIpAddressService service = new HttpRequestIpAddressService();
        Assertions.assertEquals(LOCALHOST, service.getIpAddress(request).getHostAddress(), "The result must be " + LOCALHOST);
    }

    @ParameterizedTest
    @ValueSource(strings = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"})
    public void testWithHeadersEmpty(String header) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(header)).thenReturn("");

        HttpRequestIpAddressService service = new HttpRequestIpAddressService();
        Assertions.assertEquals(LOCALHOST, service.getIpAddress(request).getHostAddress(), "The result must be " + LOCALHOST);
    }


    @Test
    public void testNoHeadersGetRemoteAddr() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(EXPECTED_IP_ADDRESS);

        HttpRequestIpAddressService service = new HttpRequestIpAddressService();
        Assertions.assertEquals(EXPECTED_IP_ADDRESS, service.getIpAddress(request).getHostAddress(),
                "IP address should be " + EXPECTED_IP_ADDRESS);
    }

    @Test
    public void testNoHeaderNoRemoteAddr() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpRequestIpAddressService service = new HttpRequestIpAddressService();
        Assertions.assertEquals(LOCALHOST, service.getIpAddress(request).getHostAddress(), "The result must be " + LOCALHOST);
    }

    @Test
    public void testWrongIpAddress() {
        String wrongIpAddress = "555.0.0.1";
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(wrongIpAddress);
        HttpRequestIpAddressService service = new HttpRequestIpAddressService();
        Assertions.assertNull(service.getIpAddress(request), "The result must be null");
    }
}

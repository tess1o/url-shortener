package com.chalyi.urlshortener.services.net;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This service is used to get IP Address from a HttpServletRequest.
 */
@Service
public class HttpRequestIpAddressService {

    /**
     * We'll try to get the IP address from the request headers. If no IP address could be retrieved from the headers
     * or the retrieved value is not a valid IP address, then a null will be returned.
     * @param request - http request from which an IP address should be taken
     * @return IP address of the client or null if it can't be parsed
     */
    public InetAddress getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            return null;
        }
    }
}

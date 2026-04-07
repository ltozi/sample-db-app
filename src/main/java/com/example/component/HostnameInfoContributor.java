package com.example.component;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class HostnameInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        try {
            builder.withDetail("hostname", InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            builder.withDetail("hostname", "unknown");
        }
    }
}

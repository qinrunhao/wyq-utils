package com.wyq.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class NetUtil {

    /**
     * 获取linux eth0 网卡mac e.g. 00:90:27:e2:43:98 --> 009027E24398
     *
     * @return
     */
    public static String getMac() {
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> el = NetworkInterface.getNetworkInterfaces();
            while (el.hasMoreElements()) {
                NetworkInterface networkInterface = el.nextElement();
                if ("eth0".equals(networkInterface.getName())) {
                    byte[] mac = networkInterface.getHardwareAddress();
                    if (mac != null) {
                        for (byte b : mac) {
                            //convert to hex string.
                            String hex = Integer.toHexString(0xff & b).toUpperCase();
                            if (hex.length() == 1) {
                                hex = "0" + hex;
                            }
                            sb.append(hex);
                        }
                    }
                    if (sb.length() > 0) {
                        return sb.toString();
                    } else {
                        InetAddress inetAddress = InetAddress.getLocalHost();
                        return inetAddress.getHostAddress() + "-" + inetAddress.getHostName();
                    }
                }
            }
            throw new RuntimeException("无法获取网卡[eth0] mac!!!");
        } catch (Exception e) {
            throw new RuntimeException("无法获取网卡[eth0] mac!!!", e);
        }
    }

    public static String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean validIp(String ip) {
        if (StringUtils.hasText(ip)) {
            ip = ip.trim();
            try {
                List<Integer> bs = Arrays.stream(ip.split("\\."))
                        .map(e -> e.trim())
                        .map(e -> Integer.parseInt(e)).collect(Collectors.toList());
                if (bs.size() != 4 || bs.get(3) <= 0) {
                    return false;
                }
                return bs.stream().allMatch(e -> e < 255 && e > 0);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public static boolean validNetmask(String netmask) {
        if (StringUtils.hasText(netmask)) {
            netmask = netmask.trim();
            try {
                List<Integer> bs = Arrays.stream(netmask.split("\\."))
                        .map(e -> e.trim())
                        .map(e -> Integer.parseInt(e)).collect(Collectors.toList());
                if (bs.size() != 4) {
                    return false;
                }
                int v = (bs.get(0) & 0xFF) << 24 | (bs.get(1) & 0xFF) << 16 | (bs.get(2) & 0xFF) << 8 | (bs.get(3) & 0xFF);
                return (v - 1 | v) == 0xFFFFFFFF;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}

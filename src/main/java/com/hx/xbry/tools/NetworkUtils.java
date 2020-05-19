package com.hx.xbry.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName NetworkUtils
 * @Description 获取网络相关信息工具类
 * @Author fmy
 * @Date 2020/5/14 11:53
 * @Version 1.0
 */
public class NetworkUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkUtils.class);

    /**
     * @Description 是否运行本机上
     * @Author fengmingyang
     * @Date 2020/5/14 12:47
     * @Param []
     * @Return boolean
     **/
    public static boolean isRunningOnLocalHost() {
        Properties properties = new Properties();
        try {
            properties.load(NetworkUtils.class.getClassLoader().getResourceAsStream("init.properties"));
            if ((properties.getProperty("LOCAL.USER","").equals(getLocalHostName()))) {
                return true;
            }
            Matcher matcher = Pattern.compile(properties.getProperty("LOCAL.IP")).matcher(getLocalIp4Address().toString());
            return matcher.find();
        } catch (IOException e) {
            LOGGER.error("",e);
        }
        return false;
    }

    /**
     *@Description 获取本机用户名
     *@Author fmy
     *@Date 2020/5/14 15:32
     *@Param []
     *@Return java.lang.String
     **/
    private static String getLocalHostName() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        return inetAddress.getHostName();
    }


    public static Optional<Inet4Address> getLocalIp4Address() throws SocketException {
        final List<Inet4Address> ipByNi = getLocalIp4AddressFromNetworkInterface();
        if (ipByNi.size() != 1) {
            final Optional<Inet4Address> ipBySocketOpt = getIpBySocket();
            if (ipBySocketOpt.isPresent()) {
                return ipBySocketOpt;
            }
            return ipByNi.isEmpty() ? Optional.empty() : Optional.of(ipByNi.get(0));
        }
        return Optional.of(ipByNi.get(0));
    }

    public static List<Inet4Address> getLocalIp4AddressFromNetworkInterface() throws SocketException {
        List<Inet4Address> addresses = new ArrayList<>(1);
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        if (e == null) {
            return addresses;
        }
        while (e.hasMoreElements()) {
            NetworkInterface n = e.nextElement();
            if (!isValidInterface(n)) {
                continue;
            }
            Enumeration<InetAddress> ee = n.getInetAddresses();
            while (ee.hasMoreElements()) {
                InetAddress i = ee.nextElement();
                if (isValidAddress(i)) {
                    addresses.add((Inet4Address) i);
                }
            }
        }
        return addresses;
    }

    /**
     * 过滤回环网卡、点对点网卡、非活动网卡、虚拟网卡并要求网卡名字是eth或ens开头
     *
     * @param ni 网卡
     * @return 如果满足要求则true，否则false
     */
    private static boolean isValidInterface(NetworkInterface ni) throws SocketException {
        return !ni.isLoopback() && !ni.isPointToPoint() && ni.isUp() && !ni.isVirtual()
                && (ni.getName().startsWith("eth") || ni.getName().startsWith("ens"));
    }

    /**
     * 判断是否是IPv4，并且内网地址并过滤回环地址.
     */
    private static boolean isValidAddress(InetAddress address) {
        return address instanceof Inet4Address && address.isSiteLocalAddress() && !address.isLoopbackAddress();
    }

    private static Optional<Inet4Address> getIpBySocket() throws SocketException {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            if (socket.getLocalAddress() instanceof Inet4Address) {
                return Optional.of((Inet4Address) socket.getLocalAddress());
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

}

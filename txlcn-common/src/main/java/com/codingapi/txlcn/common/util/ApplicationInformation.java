/*
 * Copyright 2017-2019 CodingApi .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codingapi.txlcn.common.util;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Objects;
import java.util.UUID;

/**
 * Description:
 * Date: 19-1-24 下午1:44
 *
 * @author ujued
 */
public class ApplicationInformation {

    private static final String UID = UUID.randomUUID().toString().replace("_", "").substring(0,7);

    /**
     * 模块标识
     *
     * @param environment      Spring Env
     * @param serverProperties serverProperties
     * @return 标识
     */
    public static String modId(ConfigurableEnvironment environment, ServerProperties serverProperties) {
        return getIpAddress() + ":" + serverPort(serverProperties) + ":" + UID;
    }

    /**
     * 拆分网络地址为host and port
     *
     * @param hostAndPort 主机和端口
     * @return 主机端口数组
     */
    public static String[] splitAddress(String hostAndPort) {
        if (hostAndPort.indexOf(':') == -1) {
            throw new IllegalStateException("non exists port");
        }
        String[] result = hostAndPort.split(":");
        if (StringUtils.isEmpty(result[0])) {
            result[0] = "0.0.0.0";
            return result;
        }
        if (result[0].charAt(0) == '/') {
            result[0] = result[0].substring(1);
            return result;
        }
        return result;
    }

    /**
     * 模块HTTP端口号
     *
     * @param serverProperties serverProperties
     * @return int
     */
    public static int serverPort(ServerProperties serverProperties) {
        return Objects.isNull(serverProperties) ? 0 : (Objects.isNull(serverProperties.getPort()) ? 8080 :
                serverProperties.getPort());
    }

    /**
     * 根据网卡获得IP地址
     *
     * @throws SocketException
     * @throws UnknownHostException
     */
    public static String getIpAddress(){
        try{
            String ip = "";
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface network = en.nextElement();
                String name = network.getName();
                if (name.contains("docker") && !name.contains("lo") && !name.contains("br-")) {
                    continue;
                }
                for (Enumeration<InetAddress> enumIpAddr = network.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    //获得IP
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress.isLoopbackAddress()) {
                        continue;
                    }
                    String ipAddress = inetAddress.getHostAddress();
                    if (!ipAddress.contains("::") && !ipAddress.contains("0:0:") && !ipAddress.contains("fe80")) {
                        if (!"127.0.0.1".equals(ip)) {
                            ip = ipAddress;
                        }
                    }
                }
            }
            return ip;
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.erybobo.message.core.discovery;

/**
 * Created on 2023/8/31.
 * Description:
 * Copyright (c) 2018, 成都冰鉴信息科技有限公司
 * All rights reserved.
 *
 * @author zhangbo
 */
public class ImportedNacosService {
    private final String serviceName;

    private final Integer port;

    private final String ip;

    private String instanceId;

    public ImportedNacosService(String serviceName, Integer port, String ip, String instanceId) {
        this.serviceName = serviceName;
        this.port = port;
        this.ip = ip;
        this.instanceId = instanceId;
    }

    public ImportedNacosService(String serviceName, Integer port, String ip) {
        this.serviceName = serviceName;
        this.port = port;
        this.ip = ip;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getIp() {
        return ip;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Integer getPort() {
        return port;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public static final class Builder {
        private String serviceName;
        private Integer port;
        private String ip;
        private String instanceId;

        private Builder() {
        }

        public static Builder anImportedNacosService() {
            return new Builder();
        }

        public Builder withServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder withPort(Integer port) {
            this.port = port;
            return this;
        }

        public Builder withIp(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder withInstanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public ImportedNacosService build() {
            return new ImportedNacosService(serviceName, port, ip, instanceId);
        }
    }
}
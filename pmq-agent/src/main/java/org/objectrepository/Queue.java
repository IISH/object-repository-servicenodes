package org.objectrepository;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public final class Queue extends ThreadPoolTaskExecutor {

    private String queueName;
    private String shellScript;

    public Queue(String queueName, String shellScript) {
        this.queueName = queueName;
        this.shellScript = shellScript;
        setCorePoolSize(1);
        setMaxPoolSize(1);
        setQueueCapacity(1);
    }

    public String getQueueName() {
        return queueName;
    }

    public String getShellScript() {
        return shellScript;
    }
}

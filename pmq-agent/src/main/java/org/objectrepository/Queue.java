package org.objectrepository;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public final class Queue extends ThreadPoolTaskExecutor {

    private String queueName;
    private String bash;
    private String shellScript;
    private boolean topic = false;

    public Queue(String queueName, String bash, String shellScript, boolean topic) {
        this.queueName = queueName;
        this.bash = bash ;
        this.shellScript = shellScript;
        setCorePoolSize(1);
        setMaxPoolSize(1);
        setQueueCapacity(1);
        setTopic(topic);
    }

    public String getQueueName() {
        return queueName;
    }

    public String getBash() {
        return bash;
    }

    public String getShellScript() {
        return shellScript;
    }

    public boolean isTopic() {
        return topic;
    }

    public void setTopic(boolean topic) {
        this.topic = topic;
    }
}

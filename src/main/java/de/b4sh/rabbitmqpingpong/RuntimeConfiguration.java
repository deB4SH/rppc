package de.b4sh.rabbitmqpingpong;

public enum RuntimeConfiguration {
    //GENERAL CONFIGURATION
    USERNAME(getValueFromEnv("rabbitmq.username"), "rabbitmq"),
    PASSWORD(getValueFromEnv("rabbitmq.password"), ""),
    HOST(getValueFromEnv("rabbitmq.host"), "localhost"),
    PORT(getValueFromEnv("rabbitmq.port"), "5672"),
    VIRTUALHOST(getValueFromEnv("rabbitmq.virtualhost"), "/"),
    MODE(getValueFromEnv("client.mode"),"receiver"),
    //TLS CONFIGURATION
    USE_TLS_CLIENTCERT(getValueFromEnv("rabbitmq.useclientcert"),"false"),
    KEYSTORE_CLIENTKEYSTORE_PASSPHRASE(getValueFromEnv("rabbitmq.clientkeystorepassphase"),"superawesomepassword"),
    KEYSTORE_CLIENTKEYSTORE_PATH(getValueFromEnv("rabbitmq.clientkeystorepath"),"/tmp/clientcert.p12"),
    KEYSTORE_SERVERKEYSTORE_PASSPHRASE(getValueFromEnv("rabbitmq.serverkeystorepassphase"),"superawesomepassword"),
    KEYSTORE_SERVERKEYSTORE_PATH(getValueFromEnv("rabbitmq.serverkeystorepath"),"/tmp/servercert.p12"),
    //SENDER CONFIGURATION
    QUEUE_SENDER(getValueFromEnv("rabbitmq.sender.queue"),"ping-pong-sender"),
    SENDER_CRON(getValueFromEnv("rabbitmq.sender.cron"),"0/5 * * * * ?"),
    //RECEIVER CONFIGURATION
    QUEUE_RECEIVER(getValueFromEnv("rabbitmq.receiver.queue"),"ping-pong-receiver"),
    //METRICS CONFIGURATION
    METRICS_ENVIRONMENT_NAME(getValueFromEnv("rabbitmq.metrics.envname"), ""),

    ;

    private final String value;
    private final String defaultValue;

    RuntimeConfiguration(String value, String defaultValue) {
        this.value = value;
        this.defaultValue = defaultValue;
    }

    public String getValue(){
        if(value == null){
            return defaultValue;
        }
        return value;
    }

    static String getValueFromEnv(final String envName){
        return System.getenv(envName);
    }
}

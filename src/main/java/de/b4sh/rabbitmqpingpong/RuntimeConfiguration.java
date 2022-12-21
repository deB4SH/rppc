package de.b4sh.rabbitmqpingpong;

public enum RuntimeConfiguration {

    USERNAME(getValueFromEnv("rabbitmq.username"), "rabbitmq"),
    PASSWORD(getValueFromEnv("rabbitmq.password"), "password"),
    HOST(getValueFromEnv("rabbitmq.host"), "localhost"),
    PORT(getValueFromEnv("rabbitmq.port"), "5672"),
    VIRTUALHOST(getValueFromEnv("rabbitmq.virtualhost"), "/")

    ;


    private final String value;
    private final String defaultValue;

    RuntimeConfiguration(String value, String defaultValue) {
        this.value = value;
        this.defaultValue = defaultValue;
    }

    public String getValue(){
        if(value.isEmpty() || value.isBlank()){
            return defaultValue;
        }
        return value;
    }

    static String getValueFromEnv(final String envName){
        return System.getenv(envName);
    }
}

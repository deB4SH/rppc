Rabbitmq Ping Pong Client
====

The rabbitmq ping pong client provides a small service to check the connectivity of your rabbitmq instance. 

## Overview

The application itself is split into three parts:
* sender
* receiver
* metrics

### Message Structure
Each message is build with the same structure: `message-$EPOCHTIMESENDER-$EPOCHTIMERECEIVER`.
This provides an easy approach to check if there is a high delay between your sender and receiver which may point towards a slow running rabbitmq and full queues in your environment.

### Sender
The sender is the initial component to send messages into a queue. It will generate a message with the current epoch time embedded and pushes it towards your configured rabbitmq instance.
The configuration itself is done via environment variables.
#### Environment Configuration
```
client.mode: sender
rabbitmq.clientkeystorepassphase: ping-pong
rabbitmq.clientkeystorepath: /tmp/certs/client.p12
rabbitmq.host: YOUR_AWESOME_BROKER_URL
rabbitmq.password: YOUR_AWESOME_PASSWORD
rabbitmq.port: "5671"
rabbitmq.receiver.queue: ping-pong-receiver
rabbitmq.sender.queue: ping-pong-sender
rabbitmq.serverkeystorepassphase: ping-pong
rabbitmq.serverkeystorepath: /tmp/certs/serverks.jks
rabbitmq.useclientcert: "true"
rabbitmq.username: YOUR_AWESOME_USERNAME
rabbitmq.virtualhost: /
```

### Receiver
The receiver is the second part of this application and will watch a configured queue in your rabbitmq and consume any message that gets put into. 
After receiving a message the receiver will put his own epoch time behind the already existing and push the message into the second queue.
As already seen the configuration itself is done via environment variable and only differs in the `client.mode` set to receiver.
#### Environment Configuration
```
client.mode: receiver
rabbitmq.clientkeystorepassphase: ping-pong
rabbitmq.clientkeystorepath: /tmp/certs/client.p12
rabbitmq.host: YOUR_AWESOME_BROKER_URL
rabbitmq.password: YOUR_AWESOME_PASSWORD
rabbitmq.port: "5671"
rabbitmq.receiver.queue: ping-pong-receiver
rabbitmq.sender.queue: ping-pong-sender
rabbitmq.serverkeystorepassphase: ping-pong
rabbitmq.serverkeystorepath: /tmp/certs/serverks.jks
rabbitmq.useclientcert: "true"
rabbitmq.username: YOUR_AWESOME_USERNAME
rabbitmq.virtualhost: /
```

### Metrics
The metrics component provides the final part of the rppc and does the evaluation of the passed-through message. 
It provides a prometheus endpoint with the metrics for `rabbitmq_receiver_timestamp` and `rabbitmq_sender_timestamp` as gauge. 
Based on these two epoch timestamps you should be able to calculate the time in milliseconds that passed by from sender to receiver. 
The environment configuration differs only for the `client.mode` in this example. 
#### Environment Configuration
```
client.mode: metrics
rabbitmq.clientkeystorepassphase: ping-pong
rabbitmq.clientkeystorepath: /tmp/certs/client.p12
rabbitmq.host: YOUR_AWESOME_BROKER_URL
rabbitmq.password: YOUR_AWESOME_PASSWORD
rabbitmq.port: "5671"
rabbitmq.receiver.queue: ping-pong-receiver
rabbitmq.sender.queue: ping-pong-sender
rabbitmq.serverkeystorepassphase: ping-pong
rabbitmq.serverkeystorepath: /tmp/certs/serverks.jks
rabbitmq.useclientcert: "true"
rabbitmq.username: YOUR_AWESOME_USERNAME
rabbitmq.virtualhost: /
```

## Setup
For a general purpose this repository provides a kustomize kubernetes deployment found under [src/main/kubernetes](https://github.com/deB4SH/rppc/tree/main/src/main/kubernetes).
The setup is following the kustomize components setup and patches everything together based on your decisions withing the overlays.
If you are not familiar with kustomize commponents I strongly advice you to take a quick detour towards the official [documentation](https://github.com/kubernetes-sigs/kustomize/blob/master/examples/components.md#components-example)
```shell
/src/main/kubernetes
├── base
│   ├── deployment.yaml
│   ├── kustomization.yaml
│   └── service.yaml
├── components
│   ├── metrics
│   │   ├── configuration-metrics.yaml
│   │   ├── kustomization.yaml
│   │   └── patch-deployment.yaml
│   ├── receiver
│   │   ├── configuration-receiver.yaml
│   │   ├── kustomization.yaml
│   │   └── patch-deployment.yaml
│   └── sender
│       ├── configuration-sender.yaml
│       ├── kustomization.yaml
│       └── patch-deployment.yaml
├── overlay
│   ├── example_enterprise
│   │   ├── certificates
│   │   │   ├── client.p12
│   │   │   └── serverks.jks
│   │   ├── configuration-metrics.yaml
│   │   ├── configuration-receiver.yaml
│   │   ├── configuration-sender.yaml
│   │   ├── kustomization.yaml
│   │   └── servicemonitor.yaml
│   └── example_without_metrics
│       ├── certificates
│       │   ├── client.p12
│       │   └── serverks.jks
│       └── kustomization.yaml
└── README.md
```
Qatja Processing library
==========

## MQTT 3.1.1 compliant client library for Processing

# About MQTT
MQTT stands for MQ Telemetry Transport. It is a publish/subscribe, extremely simple and lightweight messaging protocol, designed for constrained devices and low-bandwidth, high-latency or unreliable networks. The design principles are to minimise network bandwidth and device resource requirements whilst also attempting to ensure reliability and some degree of assurance of delivery. These principles also turn out to make the protocol ideal of the emerging “machine-to-machine” (M2M) or “Internet of Things” world of connected devices, and for mobile applications where bandwidth and battery power are at a premium.

*source: [MQTT.org](http://www.mqtt.org)*

# Installation

1. Download the latest version [here](http://www.santiclaws.se/qatja/Qatja.zip)
2. Extract the zip-file into your /sketchbook/libraries/ folder.
3. Restart Processing IDE

# Getting started with Qatja in Processing.

**Create the library object**

``` java
Qatja client = new Qatja(this);
```

**Subscribing to a topic**

``` java
client.subscribe( "mytopic" );

void mqttCallback(MQTTPublish msg){
  String message = new String(msg.getPayload());
}
```

**Publish message to a topic**

``` java
String message = "msg" + random(0,1000);
client.publish( "mytopic", message );
```
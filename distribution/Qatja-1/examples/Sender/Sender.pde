import se.goransson.mqtt.*;

MQTT mqtt;

void setup() {
  mqtt = new MQTT( this );
  mqtt.DEBUG = true;

  mqtt.connect( "127.0.0.1", 1883, "mqtt_sender" );
}

void draw() {
}

void mouseDragged(){
  String doc = "abcdef";

  mqtt.publish("mytopic", doc);
}
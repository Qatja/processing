import se.goransson.mqtt.*;

MQTT mqtt;

int mx = -20, my = -20;

void setup() {
  mqtt = new MQTT( this );
  mqtt.connect( "127.0.0.1", 1883, "mqtt_receiver" );
  mqtt.DEBUG = true;
}

void keyPressed(){
  mqtt.subscribe( "mytopic", "mymethod" );
}

void draw() {
  background( 0 );
  fill( 255 );
  ellipse( mx, my, 20, 20 );
}

void mymethod(MQTTMessage msg){
  println( msg.toString() );
  println( new String(msg.payload) );
}
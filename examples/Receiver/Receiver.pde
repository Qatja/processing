/*
 * Qatja
 * Example: Receiver
 * 
 * Subscribes to mouse coordinates on topic
 *
 * author: Andreas Göransson, 2014
 */
import se.goransson.qatja.*;
import se.goransson.qatja.messages.*;

Qatja client;

int mx = -20, my = -20;

void setup() {
  client = new Qatja( this );
  client.DEBUG = true;
  
  client.connect( "127.0.0.1", 1883, "qatja-receiver" );
}

void keyPressed(){
  client.subscribe( "mouse" );
}

void draw() {
  background( 0 );
  fill( 255 );
  ellipse( mx, my, 20, 20 );
}

void mqttCallback(MQTTPublish msg){
  String payload = new String(msg.getPayload());
  String[] coords = split(payload, ",");
  mx = parseInt(coords[0]);
  my = parseInt(coords[1]);
}

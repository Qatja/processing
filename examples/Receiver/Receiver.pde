/*
 * Qatja (https://github.com/Qatja)
 *
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
  // 1. Initialize the library object
  client = new Qatja( this );
  
  // To send the DISCONNECT message when sketch is closed
  registerMethod("dispose", this);
  
  // If you want the debugging messages, set this to true!
  client.DEBUG = true;
  
  // 2. Set connection details
  client.setKeepalive(5000);
  client.setHost("localhost");
  client.setPort(1883);
  client.setClientIdentifier("qatja-receiver");
  
  // 3. Request a connection to a broker. The identification
  //    string at the end must be unique for that broker!
  client.connect();
}

void keyPressed(){
  client.subscribe( "mouse" );
}

void draw() {
  background( 0 );
  fill( 255 );
  ellipse( mx, my, 20, 20 );
}

void dispose(){
  client.disconnect();
}

void mqttCallback(MQTTPublish msg){
  String payload = new String(msg.getPayload());
  String[] coords = split(payload, ",");
  mx = parseInt(coords[0]);
  my = parseInt(coords[1]);
}

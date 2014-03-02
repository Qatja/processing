/*
 * Qatja
 * Example: Sender
 * 
 * Publishes mouse coordinates to topic
 *
 * author: Andreas Göransson, 2014
 */
import se.goransson.qatja.messages.*;
import se.goransson.qatja.*;

Qatja client;

void setup() {
  client = new Qatja( this );
  client.DEBUG = true;

  client.connect( "127.0.0.1", 1883, "qatja-sender" );
}

void draw() {
}

void mouseDragged(){
  String payload = mouseX+","+mouseY;
  client.publish("mouse", payload);
}

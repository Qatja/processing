/*
 * Qatja (https://github.com/Qatja)
 *
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
  client.setClientIdentifier("qatja-sender");
  
  // 3. Request a connection to a broker. The identification
  //    string at the end must be unique for that broker!
  client.connect();
}

void draw() {
}

void mouseDragged(){
  String payload = mouseX+","+mouseY;
  client.publish("mouse", payload);
}

void dispose(){
  client.disconnect();
}

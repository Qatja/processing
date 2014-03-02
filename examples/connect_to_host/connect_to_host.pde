/*
 * Qatja
 * Example 1: Connect to MQTT broker
 * 
 * author: Andreas Göransson, 2014
 */
import se.goransson.qatja.messages.*;
import se.goransson.qatja.*;

Qatja client;

void setup() {
  // 1. Initialize the library object
  client = new Qatja( this );
  
  // If you want the debugging messages, set this to true!
  client.DEBUG = true;
  
  // 2. Request a connection to a broker. The identification
  //    string at the end must be unique for that broker!
  client.connect( "127.0.0.1", 1883, "qatja-client" );
}

void draw() {
}

void exit() {
  client.disconnect();
  super.exit();
}

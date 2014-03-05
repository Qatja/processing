/*
 * Qatja (https://github.com/Qatja)
 *
 * Example 3: Publish to a topic
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
  client.setClientIdentifier("qatja-processing");
  
  // 3. Request a connection to a broker. The identification
  //    string at the end must be unique for that broker!
  client.connect();
}

void draw() {
}

void dispose() {
  client.disconnect();
}

void keyPressed(){
  String message = "my message" + random(0,10000);
  // 4. Use "publish" to send a message to a specific topic
  client.publish( "test", message );
}

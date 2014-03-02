/*
 * Qatja
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

void keyPressed(){
  String message = "msg" + random(0,10000);
  // 3. Use "publish" to send a message to a specific topic
  client.publish( "mytopic", "my message" + message );
}

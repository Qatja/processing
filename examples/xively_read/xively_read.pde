/*
 * Qatja
 * Example: Xively read
 * 
 * Subscribes to a Xively feed
 *
 * Example feed URL: https://xively.com/feeds/113938466
 *
 * author: Andreas Göransson, 2014
 */
import se.goransson.qatja.*;
import se.goransson.qatja.messages.*;

Qatja client;

String apiKey = "KqFeWm0shfMInCGRz8LfMSPTRBPpgRhDSSZNEvrBZP2klChe";

void setup() {
  client = new Qatja( this );
  client.DEBUG = true;
  
  // Only need to provide the apiKey as username, password is ignored
  // Read more here: https://xively.com/dev/docs/api/communicating/mqtts/
  client.setKeepalive(5000);
  client.setHost("api.xively.com");
  client.setPort(1883);
  client.setClientIdentifier("qatja-xively-receiver");
  client.setUsername(apiKey);
  client.setPassword("");
  
  client.connect();
}

void draw() {
}

void keyPressed() {
  client.subscribe("/v2/feeds/113938466.json");
}

void mqttCallback(MQTTPublish msg){
  println( msg.toString() );
}

void dispose(){
  client.disconnect();
}

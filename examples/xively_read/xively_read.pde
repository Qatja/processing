/*
 * Qatja
 * Example: Xively read
 * 
 * Subscribe to Xively feed
 *
 * Example feed URL: https://xively.com/feeds/113938466
 *
 * author: Andreas Göransson, 2014
 */
import se.goransson.qatja.*;
import se.goransson.qatja.messages.*;


Qatja client;

String apiKey = "VVWofEKUPEORQA5LHXETAixVI6cnwPQD0FLd1YYcEkYFOvr8";

void setup() {
  client = new Qatja( this );
  client.DEBUG = true;

  client.connect( "api.xively.com", 1883, "qatja-xively-rec" );
}

void draw() {
}

void keyPressed(){
  client.subscribe(apiKey+"/v2/feeds/113938466.json");
}

void mqttCallback(MQTTPublish msg){
  String payload = new String(msg.getPayload());
  println(payload);
}
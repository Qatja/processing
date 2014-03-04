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

void setup() {
  client = new Qatja( this );
  client.DEBUG = true;

  client.connect( "api.xively.com", 1883, "qatja-xively" );
}

void draw() {
}

void keyPressed() {
  client.subscribe("/v2/feeds/113938466.json");
}

void mqttCallback(MQTTPublish msg){
  println( msg.toString() );
}

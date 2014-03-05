/*
 * Qatja (https://github.com/Qatja)
 *
 * Example: Xively read
 * 
 * Subscribes to a Xively feed, press any key to subscribe to 
 * the desired feed.
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

float value = 0;

void draw() {
  background(190);
  noStroke();
  fill( 75, 75, 75 );
  rect(40, height, 20, -value );
}

// Update the visual representation of the values
void updateGraph(int curValue, float maxValue, float minValue ){
  value = map( curValue, minValue, maxValue, 0, height );
}

void keyPressed() {
  client.subscribe("/v2/feeds/113938466.json");
}

void mqttCallback(MQTTPublish msg){
  // Turn the response from Xively into JSON
  String json = new String(msg.getPayload());
  JSONObject obj = JSONObject.parse(json);
  
  // Get the desired values from JSON
  JSONArray datastreams = obj.getJSONArray("datastreams");
  for( int i = 0; i < datastreams.size(); i++){
    JSONObject stream = datastreams.getJSONObject(i);
    if( stream.getString("id").equals("processing") ){
      int curValue = stream.getInt("current_value");
      float maxValue = stream.getFloat("max_value");
      float minValue = stream.getFloat("min_value");
      updateGraph( curValue, maxValue, minValue );
    }
  }
}

void dispose(){
  client.disconnect();
}

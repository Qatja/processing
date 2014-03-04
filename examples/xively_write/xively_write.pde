/*
 * Qatja
 * Example: Xively write
 * 
 * Publishes data to Xively feed
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
  
  registerMethod("dispose", this);
  
  // Only need to provide the apiKey as username, password is ignored
  // Read more here: https://xively.com/dev/docs/api/communicating/mqtts/
  client.setKeepalive(5000);
  client.setHost("api.xively.com");
  client.setPort(1883);
  client.setClientIdentifier("qatja-xively-sender");
  client.setUsername(apiKey);
  client.setPassword("");
  
  client.connect();
}

void draw() {
}

/*
  Example Xively JSON publish format
  
  {
    "version":"1.0.0",
    "datastreams" : [
      {
        "id" : "processing",
        "current_value" : "mouseX"
      }
    ]
  }
*/
void mousePressed() {
  JSONObject datastream = new JSONObject();
  datastream.setString("id", "processing");
  datastream.setInt("current_value", mouseX);

  JSONArray datastreams = new JSONArray();
  datastreams.setJSONObject(0, datastream);

  JSONObject message = new JSONObject();
  message.setString("version", "1.0.0");
  message.setJSONArray("datastreams", datastreams);

  client.publish("/v2/feeds/113938466.json", message.toString());
}

void dispose(){
  client.disconnect();
}

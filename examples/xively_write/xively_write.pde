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

String apiKey = "VVWofEKUPEORQA5LHXETAixVI6cnwPQD0FLd1YYcEkYFOvr8";

void setup() {
  client = new Qatja( this );
  client.DEBUG = true;

  client.connect( "api.xively.com", 1883, "qatja-xively" );
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

  // If you don't authenticate with the service, you need to prepend all 
  // published messages with your API KEY. 
  // Read more: https://xively.com/dev/docs/api/communicating/mqtts/
  client.publish(apiKey+"/v2/feeds/113938466.json", message.toString());
}


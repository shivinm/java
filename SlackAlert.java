import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;


public class SlackAlert {
        
    public static void sendAlert (String errorTitle, String errorMessage, ArrayList<String> additionalData) {
       
        /* JSON message structure
        {
            "text":"java.lang.NullPointerException",
            "attachments":[
               {
                  "fallback":"server.log",
                  "fields":[
                     {
                        "title":"Value in JsonObjects name/value pair cannot be null",
                        "value":"<stack-trace>",
                        "short":false
                     },
                     {
                        "title":"Additional data",
                        "value":"<data>",
                        "short":false
                     },
                     
                  ]
               }
            ]
         }
        */
        JsonObjectBuilder jsonObjBuilder = Json.createObjectBuilder();
        
        JsonArrayBuilder jsonAttachmentArrayBuilder = Json.createArrayBuilder();
        JsonObjectBuilder jsonAttachmentBuilder = Json.createObjectBuilder();
        
        JsonArrayBuilder    jsonFieldsArrayBuilder = Json.createArrayBuilder();
        JsonObjectBuilder   jsonExceptionFieldBuilder = Json.createObjectBuilder();
        
        
        jsonExceptionFieldBuilder.add("title", errorTitle);
        jsonExceptionFieldBuilder.add("value", errorMessage);
        jsonExceptionFieldBuilder.add("short", Boolean.FALSE);
        
        JsonObject jsonExceptionField = jsonExceptionFieldBuilder.build();
        
        JsonObject jsonAdditionalDataField = null;
        if (additionalData != null && additionalData.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (String data : additionalData) {
                builder.append(data + "\n");
            }

            JsonObjectBuilder jsonAdditionalDataFieldBuilder  = Json.createObjectBuilder();
            jsonAdditionalDataFieldBuilder.add("title", "Additional data");
            jsonAdditionalDataFieldBuilder.add("value", builder.toString());
            jsonAdditionalDataFieldBuilder.add("short", Boolean.FALSE);
            jsonAdditionalDataField = jsonAdditionalDataFieldBuilder.build();
        }
       
        jsonFieldsArrayBuilder = jsonFieldsArrayBuilder.add(jsonExceptionField);
        if (jsonAdditionalDataField != null) { 
            jsonFieldsArrayBuilder = jsonFieldsArrayBuilder.add(jsonAdditionalDataField);
        }
        
        JsonObject jsonAttachment = jsonAttachmentBuilder.add("fallback"        , "server.log"    )
                                                         .add("fields"          , jsonFieldsArrayBuilder.build())
                                                         .add("color"           , "#CC0000") // Red
                                                         .build();

        JsonObject jsonPayloadObj = jsonObjBuilder.add("text", "attachment")
                                                  .add("attachments", jsonAttachmentArrayBuilder.add(jsonAttachment).build())
                                                  .build();
        
        String url                        = "https://hooks.slack.com/services/FOO/BAR/abcdef";  // URL created by Slack's Incoming Webhook app
        String body                       = jsonPayloadObj.toString();
        
        // Send HTTP Post message to Slack
        OutputStream os = null;
        try { 
            URL obj = new URL(url);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setConnectTimeout(5000); // set timeout to 5 seconds
            con.setDoOutput(true);
            os = con.getOutputStream();
            if (body != null) {
                os.write(body.getBytes());
            }
            os.flush();
            os.close();
        } catch (Exception e) { 
            e.printStackTrace();
        }
    }
}

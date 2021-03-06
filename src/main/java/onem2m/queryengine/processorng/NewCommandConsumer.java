package onem2m.queryengine.processorng;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import onem2m.queryengine.common.SubQuery;
import onem2m.queryengine.processorng.preprocessor.PreProcessorThread;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

public class NewCommandConsumer extends DefaultConsumer {
    JSONParser parser;
    public static Boolean timerStarted = false;
    boolean isExternalQuery = false;
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    public NewCommandConsumer(Channel channel) {
        super(channel);
        parser = new JSONParser();
        LOGGER.info("Ready to receive command");
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws UnsupportedEncodingException {
        try {
            //System.out.println("Received a command");
            String message = new String(body, "UTF-8");
            JSONObject inputJson = (JSONObject) parser.parse(message);
            String command = (String) inputJson.get("cmd");
            JSONObject arg = (JSONObject) inputJson.get("args");

            switch (command) {
                case "start_query":
                    PreProcessorThread.newQuery(arg);
                    break;
                case "start_query_direct":
                    SubQuery query = new SubQuery(arg);
                    query.adjustArgsFieldNaming();
                    System.out.println(query.toJSON());
                    //query.provisionQuery();
                    query.provisionQueryNg();
                    break;
            }

            LOGGER.info("Finish processing command");
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

}

package com.sample.lambda;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class LambdaFunctionHandler implements RequestHandler<S3Event, Object> {
	
	private static final String ACCEPTABLE_INSTANCE_TYPE = "t2.micro";
	private static final String TOPIC_ARN = "arn:aws:sns:ap-northeast-1:694273932022:NginxErrorTopic";

    @Override
    public Object handleRequest(S3Event s3event, Context context) {
    	LambdaLogger logger = context.getLogger();
   		logger.log("start");
    	try { 

    		logger.log("Input(json): " + s3event.toJson());
        
    		S3EventNotificationRecord record = s3event.getRecords().get(0);
    		String srcBucket = record.getS3().getBucket().getName();
    		String srcKey = record.getS3().getObject().getKey()
                .replace('+', ' ');
    		srcKey = URLDecoder.decode(srcKey, "UTF-8");
    		
    		logger.log("srcBucket = " + srcBucket + ", srcKey = " + srcKey);
    		
    		JSONObject cloudTrailLog = Util.getCloudTrailLog(srcBucket, srcKey);
    		
    		if(cloudTrailLog == null) {
    			logger.log("cloudTrailLog is null");
    			return "";
    		}
    		
  			logger.log("get cloudtrail log");
    		List<JSONObject> items = Util.getEc2ist(cloudTrailLog,
    				ACCEPTABLE_INSTANCE_TYPE);

    		if(items == null || items.isEmpty()) {
    			logger.log("don't exsits invalid EC2");
    			return "";
    		}
  			
  			List<String> messageIds = Util.publish(items, TOPIC_ARN);

    		if(messageIds == null || messageIds.isEmpty()) {
    			logger.log("cloud not publish SNS");
    			return "";
    		}

    		for( String messageId: messageIds) {
    			logger.log("messageId is " + messageId);
    		}

    	} catch (IOException e) {
    		logger.log(e.getMessage());
            throw new RuntimeException(e);
        } catch (JSONException e) {
    		logger.log(e.getMessage());
            throw new RuntimeException(e);
		}
        return "OK";
    }
}
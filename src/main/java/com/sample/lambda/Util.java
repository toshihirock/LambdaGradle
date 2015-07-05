package com.sample.lambda;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class Util {

    public static JSONObject getCloudTrailLog(String bucket, String key) throws IOException, JSONException {
    	JSONObject cloudTraillog = null;
    	
    	AmazonS3Client s3 = new AmazonS3Client();
    	Region northEast1 = Region.getRegion(Regions.AP_NORTHEAST_1);
    	s3.setRegion(northEast1);
    	
    	S3Object object = null;
    	object = s3.getObject(bucket, key);

    	if(object == null) return cloudTraillog;
    	
    	GZIPInputStream gzipInputStream = null;

    	try {
    		gzipInputStream = new GZIPInputStream(object.getObjectContent());
    		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    		for (;;) {
    			int iRead = gzipInputStream.read();
    			if (iRead < 0) break;
    			outputStream.write(iRead);
    		}
    		outputStream.flush();
    		outputStream.close();
    		cloudTraillog = new JSONObject(new String(outputStream.toByteArray()));
		} finally {
			if (gzipInputStream != null) {
				gzipInputStream.close();
				
			}
    	}
   		return cloudTraillog;
    }
    
    public static List<JSONObject> getEc2ist(JSONObject cloudTrailLog, String instanceType) throws JSONException {
    	List<JSONObject> instanceIds = new ArrayList<JSONObject>();
    	
    	JSONArray records = cloudTrailLog.getJSONArray("Records");

    	for(int i = 0;i < records.length(); i++) {
    	  JSONObject record = records.getJSONObject(i);
    	  if (record.getString("eventName").equals("RunInstances") 
    			  && record.has("responseElements")) {
    	    JSONObject item = record.getJSONObject("responseElements")
    	    		.getJSONObject("instancesSet").getJSONArray("items").getJSONObject(0);
    	    if (!(item.getString("instanceType").equals(instanceType))) {
    	    	instanceIds.add(item);
    	    }
    	  }
    	}
    	return instanceIds;
    }
    
    public static List<String> publish(List<JSONObject> items, String topicArn) throws JSONException {
    	List<String> messageIds = new ArrayList<String>();
    	
    	AmazonSNSClient sns = new AmazonSNSClient();
    	Region northEast1 = Region.getRegion(Regions.AP_NORTHEAST_1);
    	sns.setRegion(northEast1);
    	
    	for (JSONObject item: items) {
    		PublishResult result = sns.publish(topicArn,
    				"invalid EC2 instance type launched.Instance id = " 
    		+ item.getString("instanceId") + ", Instance Type = " + item.getString("instanceType"));
    		messageIds.add(result.getMessageId());
    	}
    	return messageIds;
    }

}

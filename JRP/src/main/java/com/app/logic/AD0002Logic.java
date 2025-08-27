package com.app.logic;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.app.constant.CollectionName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Component
public class AD0002Logic {

	@Autowired
	private Firestore firestore;

	@Value("${gcs.bucket.name}")
	private String bucketName;
	
	private static final String PROMPT_BLOB_NAME = "prompt/mindmapPrompt_7.md";

	private static final String MOTIVATION_BLOB_NAME = "/motivationHistory.md";

	@Value("${line.bot.channelAccessToken}")
	private String channelAccessToken;
	
	@Autowired
	private AnthropicApiLogic anthropicApiLogic;
	
	@Autowired
	private GoogleCloudStorageLogic googleCloudStorageLogic;
	
	@Autowired
	private MessageSource messageSource;
	
	public void createMotivationData(String id) {
		ApiFuture<QuerySnapshot> data = firestore.collection(CollectionName.MOTIVATION)
				.document(id)
				.collection(CollectionName.MOTIVATION_ITEMS)
				.orderBy("order")
				.get();

		try {
			List<QueryDocumentSnapshot> documents = data.get().getDocuments();
			StringBuilder mdContent = new StringBuilder();

			for (QueryDocumentSnapshot document : documents) {
				mdContent.append("# 年齢: ").append(document.getString("age")).append("\n");
				mdContent.append("### ポジティブ度: ").append("\n").append(document.getString("motivation")).append("\n");
				mdContent.append("### ネガティブ度: ").append("\n")
						.append(100 - Integer.parseInt(document.getString("motivation"))).append("\n");
				mdContent.append("### 出来事・事実").append("\n").append(document.getString("event")).append("\n");
				mdContent.append("### 当時の感情・思考 ").append("\n").append(document.getString("mind")).append("\n");
			}
			// Convert content to bytes
			byte[] contentBytes = mdContent.toString().getBytes(StandardCharsets.UTF_8);

			String fileName = id + MOTIVATION_BLOB_NAME;

			Storage storage = StorageOptions.getDefaultInstance().getService();
			BlobId blobId = BlobId.of(bucketName, fileName);
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/markdown").build();

			storage.create(blobInfo, contentBytes);
		} catch (InterruptedException e) {
			throw new RuntimeException("Motivation data creation failed", e);
		} catch (ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String id, String lineId) {
		
	        String messageText = messageSource.getMessage("ST0002.lineFirstMessage", null, Locale.JAPAN);

	        try {		
		        HttpClient client = HttpClient.newHttpClient();
		        
		        // 内容を文字列に変換
		        String prompt = googleCloudStorageLogic.loadFile(PROMPT_BLOB_NAME);
		        
		        String motivationHistory = googleCloudStorageLogic.loadFile(id + MOTIVATION_BLOB_NAME);
		        
		        prompt = prompt.replace("motivationData", motivationHistory);
		        
		        googleCloudStorageLogic.createChatHistoryFile(id, "7", null);
		        
		        String chatHistory = googleCloudStorageLogic.loadFile(id + "/chatHistory_7.md");
		        
		        String reply = anthropicApiLogic.callAnthropicApi(prompt, chatHistory);
		        
		        Map<String, Object> body1 = new HashMap<>();
				body1.put("to", lineId);
				
				Map<String, String> message1 = new HashMap<>();
				message1.put("type", "text");
				message1.put("text", messageText);
				
				body1.put("messages", List.of(message1));
				
				Map<String, Object> body2 = new HashMap<>();
				body2.put("to", lineId);
				
				Map<String, String> message2 = new HashMap<>();
				message2.put("type", "text");
				message2.put("text", reply);

				body2.put("messages", List.of(message2));
				
				ObjectMapper mapper = new ObjectMapper();
				String jsonBody1 = mapper.writeValueAsString(body1);
				String jsonBody2 = mapper.writeValueAsString(body2);
		        
		        HttpRequest request1 = HttpRequest.newBuilder()
			            .uri(URI.create("https://api.line.me/v2/bot/message/push"))
			            .header("Content-Type", "application/json")
			            .header("Authorization", "Bearer " + channelAccessToken)
			            .POST(HttpRequest.BodyPublishers.ofString(jsonBody1))
			            .build();
		        
		        HttpRequest request2 = HttpRequest.newBuilder()
			            .uri(URI.create("https://api.line.me/v2/bot/message/push"))
			            .header("Content-Type", "application/json")
			            .header("Authorization", "Bearer " + channelAccessToken)
			            .POST(HttpRequest.BodyPublishers.ofString(jsonBody2))
			            .build();

				client.send(request1, HttpResponse.BodyHandlers.ofString());
				client.send(request2, HttpResponse.BodyHandlers.ofString());
				
				StringBuilder mdContent = new StringBuilder();
				mdContent.append("けいと").append(":\n");
				mdContent.append(reply).append("\n");

				// Convert content to bytes
				googleCloudStorageLogic.createChatHistoryFile(id, "7", mdContent);
				
			} catch (IOException | InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
	        
	        

	        

		
	}

}

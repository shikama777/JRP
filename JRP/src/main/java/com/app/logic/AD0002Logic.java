package com.app.logic;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.app.constant.CollectionName;
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
	
	private static final String BLOB_NAME_TEMPLATE = "/motivationHistory.md";

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
	            mdContent.append("### ネガティブ度: ").append("\n").append(100 - Integer.parseInt(document.getString("motivation"))).append("\n");
	            mdContent.append("### 出来事・事実").append("\n").append(document.getString("event")).append("\n");
	            mdContent.append("### 当時の感情・思考 ").append("\n").append(document.getString("mind")).append("\n");
			}
			// Convert content to bytes
	        byte[] contentBytes = mdContent.toString().getBytes(StandardCharsets.UTF_8);

	        String fileName = id + BLOB_NAME_TEMPLATE;

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

}

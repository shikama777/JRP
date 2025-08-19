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
public class AD0001Logic {
	
	@Autowired
	private Firestore firestore;

	@Value("${gcs.bucket.name}")
	private String bucketName;

	private static final String BLOB_NAME_TEMPLATE = "/chatHistory_%s.md";

	public void createKachikanData(String id, String userName) {
		ApiFuture<QuerySnapshot> data = firestore.collection(CollectionName.KACHIKAN)
				.document(id)
				.collection(CollectionName.KACHIKAN_ITEMS)
				.get();

		try {
			List<QueryDocumentSnapshot> documents = data.get().getDocuments();

			for (QueryDocumentSnapshot document : documents) {
				StringBuilder mdContent = new StringBuilder();
				mdContent.append("これまでの会話履歴をみてけいととして返信してください\n");
				mdContent.append("けいとからの次の返信文のみ送ってください\n\n");
				mdContent.append("### 会話履歴\n\n");
				mdContent.append(userName).append(":\n");
				mdContent.append("- ").append(document.getString("data1")).append("\n");
				mdContent.append("- ").append(document.getString("data2")).append("\n");
				mdContent.append("- ").append(document.getString("data3")).append("\n");
				// Convert content to bytes
				byte[] contentBytes = mdContent.toString().getBytes(StandardCharsets.UTF_8);
	
				String fileName = id + BLOB_NAME_TEMPLATE.formatted(document.getLong("id").toString());
	
				Storage storage = StorageOptions.getDefaultInstance().getService();
				BlobId blobId = BlobId.of(bucketName, fileName);
				BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/markdown").build();
	
				storage.create(blobInfo, contentBytes);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Motivation data creation failed", e);
		} catch (ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

}

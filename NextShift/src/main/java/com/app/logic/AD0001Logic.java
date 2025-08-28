package com.app.logic;

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

@Component
public class AD0001Logic {
	
	@Autowired
	private Firestore firestore;

	@Value("${gcs.bucket.name}")
	private String bucketName;
	
	@Autowired
	private GoogleCloudStorageLogic googleCloudStorageLogic;

	public void createKachikanData(String id, String userName) {
		ApiFuture<QuerySnapshot> data = firestore.collection(CollectionName.KACHIKAN)
				.document(id)
				.collection(CollectionName.KACHIKAN_ITEMS)
				.get();

		try {
			List<QueryDocumentSnapshot> documents = data.get().getDocuments();

			for (QueryDocumentSnapshot document : documents) {
				StringBuilder mdContent = new StringBuilder();
				mdContent.append(userName).append(":\n");
				mdContent.append("- ").append(document.getString("data1")).append("\n");
				mdContent.append("- ").append(document.getString("data2")).append("\n");
				mdContent.append("- ").append(document.getString("data3")).append("\n");
				// Convert content to bytes
				googleCloudStorageLogic.createChatHistoryFile(id, document.getLong("id").toString(), mdContent);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Motivation data creation failed", e);
		} catch (ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

}

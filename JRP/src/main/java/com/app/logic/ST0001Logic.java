package com.app.logic;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.app.constant.CollectionName;
import com.app.dto.ST0001.ST0001CreateDto;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Component
public class ST0001Logic {

	@Autowired
	private Firestore firestore;
	
	@Value("${gcs.bucket.name}")
    private String bucketName;
	
	private static final String BLOB_NAME_TEMPLATE = "/motivationHistory.md";

	private final String[] NEW_AGE_LIST = { "0～5歳", "6歳", "7歳", "8歳", "9歳", "10歳",
			"11歳", "12歳", "13歳", "14歳", "15歳", "16歳",
			"17歳", "18歳" };

	// 今はAD0001から呼び出し
	public void createMotivation(String Id) {

		List<ST0001CreateDto> dtoList = new ArrayList<>();

		CollectionReference collection = firestore.collection(CollectionName.MOTIVATION)
				.document(Id)
				.collection(CollectionName.MOTIVATION_ITEMS);

		for (int i = 0; i < NEW_AGE_LIST.length; i++) {
			ST0001CreateDto dto = new ST0001CreateDto();
			dto.setAge(NEW_AGE_LIST[i]);
			dto.setMotivation("0");
			dto.setEvent("");
			dto.setMind("");
			dto.setOrder(i);

			dtoList.add(dto);
			collection.add(dto);

		};
	}

	// 今はAD0001から呼び出し
	public void deleteMotivation(String Id) {
		try {
			DocumentReference motivationRef = firestore.collection("motivation").document(Id);

			List<QueryDocumentSnapshot> motivationItems = motivationRef
					.collection(CollectionName.MOTIVATION_ITEMS)
					.get()
					.get()
					.getDocuments();

			for (QueryDocumentSnapshot item : motivationItems) {
				motivationRef.collection(CollectionName.MOTIVATION_ITEMS)
						.document(item.getId())
						.delete()
						.get();
			}

			motivationRef.delete().get();
		} catch (InterruptedException e) {
			System.out.println(e);
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
	
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

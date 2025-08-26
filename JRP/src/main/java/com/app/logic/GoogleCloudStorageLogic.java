package com.app.logic;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Component
public class GoogleCloudStorageLogic {
	
	@Value("${gcs.bucket.name}")
	private String bucketName;

	private static final String BLOB_NAME_TEMPLATE = "/chatHistory_%s.md";
	
	public void createChatHistoryFile(String id, String historyId, StringBuilder addContent) {
		
		StringBuilder mdContent = new StringBuilder();
		mdContent.append("これまでの会話履歴をみてけいととして返信してください\n");
		mdContent.append("けいとからの次の返信文のみ送ってください\n\n");
		mdContent.append("### 会話履歴\n\n");
		
		if (addContent != null) {
			mdContent.append(addContent);
		}

		byte[] contentBytes = mdContent.toString().getBytes(StandardCharsets.UTF_8);
		
		String fileName = id + BLOB_NAME_TEMPLATE.formatted(historyId);

		Storage storage = StorageOptions.getDefaultInstance().getService();
		BlobId blobId = BlobId.of(bucketName, fileName);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/markdown").build();

		storage.create(blobInfo, contentBytes, Storage.BlobTargetOption.doesNotExist());
	}
	
	public String loadFile (String blobName) {
		Storage storage = StorageOptions.getDefaultInstance().getService();
		Blob blob = storage.get(BlobId.of(bucketName, blobName));
		String content = new String(blob.getContent(), StandardCharsets.UTF_8);
		
		return content;
	}

}

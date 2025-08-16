package com.app.logic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import com.app.dto.AD0101.AD0101DownloadDto;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@RestController
public class AD0101Logic {

	@Value("${gcs.bucket.name}")
	private String bucketName;

	private static final String BLOB_NAME_TEMPLATE = "/chatHistory_%d.md";

	public File downloadChatHistory(AD0101DownloadDto dto) throws IOException {

		String name = dto.getName();
		String docId = dto.getId();
		String historyPath = docId + BLOB_NAME_TEMPLATE;
		Storage storage = StorageOptions.getDefaultInstance().getService();

		Bucket bucket = storage.get(bucketName);

		File tempFile = File.createTempFile("chatHistory", ".md");

		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(tempFile, false), StandardCharsets.UTF_8))) {
			String chatHistoryText = "";

			if (dto.getDownloadNo() == 1) {
				chatHistoryText += downloadChatHistory1(name, historyPath, bucket);
			} else if (dto.getDownloadNo() == 2) {
				chatHistoryText += downloadChatHistory2(name, historyPath, bucket);
			} else if (dto.getDownloadNo() == 0) {
				chatHistoryText += downloadChatHistory1(name, historyPath, bucket);
				chatHistoryText += "\n\n__次のテーマへの移行__\n\n";
				chatHistoryText += downloadChatHistory2(name, historyPath, bucket);
			}

			writer.write(chatHistoryText);

		}

		return tempFile;
	}

	private static String extractConversation(String text) {
		String marker = "### 会話履歴";
		int index = text.indexOf(marker);
		if (index >= 0) {
			return text.substring(index + marker.length()).stripLeading();
		} else {
			return "";
		}
	}

	private static String downloadChatHistory1(String name, String historyPath, Bucket bucket) {
		String chatHistoryText = "";

		for (int i = 1; i <= 4; i++) {
			String blobName = String.format(historyPath, i);
			Blob blob = bucket.get(blobName);

			if (blob == null || !blob.exists()) {
				continue;
			}

			String textData = new String(blob.getContent(), StandardCharsets.UTF_8);
			textData = extractConversation(textData);

			if (i == 1) {
				chatHistoryText += "### 会話履歴\n"
						+ "けいと:\n\n"
						+ "こんにちは、" + name + "\n"
						+ "まずは「自分にとっての幸せとは」というテーマで価値観を深探りしていきましょう 。\n"
						+ "まずはあなたにとっての幸せとは何か1〜3個あげてください。\n\n"
						+ "例:\n"
						+ "- お金をかせぐこと\n"
						+ "- 週末にちょっと贅沢して、好きなお店でごはんを食べる時間\n"
						+ "- 来年はこんなことしたいな、って目標や夢がある状態\n\n"
						+ textData;
			} else if (i == 2) {
				chatHistoryText += "\n\n__次のテーマへの移行__\n\n"
						+ "けいと:\n\n"
						+ name + "ありがとうございます。\n"
						+ "次は「何を大切にして生きてるのか」というテーマで価値観を深探りしていきましょう 。\n"
						+ "まずはあなたは何を大切にして生きてるのか1〜3個あげてください\n\n"
						+ textData;
			} else if (i == 3) {
				chatHistoryText += "\n\n__次のテーマへの移行__\n\n"
						+ "けいと:\n\n"
						+ name + "ありがとうございます。\n"
						+ "次は「なぜ今その選択をしているのか」というテーマで価値観を深探りしていきましょう 。\n"
						+ textData;
			} else if (i == 4) {
				chatHistoryText += "\n\n__次のテーマへの移行__\n\n"
						+ "けいと:\n\n"
						+ name + "ありがとうございます。\n"
						+ "次は「社会に不満を感じることは」というテーマで価値観を深探りしていきましょう 。\n"
						+ "\nまずはあなたが社会に不満を感じることを1〜3個あげてください。\n\n"
						+ "例：\n"
						+ "- 周りの人が冷たい\n"
						+"- 結果がすべてみたいな風潮があること\n"
						+ textData;
			} else if (i == 5) {
				chatHistoryText += "\n\n__次のテーマへの移行__\n\n"
						+ "けいと:\n\n"
						+ name + "ありがとうございます。\n"
						+ "次は「尊敬する人、友人、好きなキャラクターは誰?」というテーマで価値観を深掘りしていきましょう 。\n"
						+ "あなたが尊敬する人、友人、好きなキャラクターを1〜3人あげてください。\n\n"
						+ "例：\n"
						+ "- 母\n"
						+"- 友人のA\n"
						+ textData;
			}
		}

		return chatHistoryText;
	}

	private String downloadChatHistory2(String name, String historyPath, Bucket bucket) {
		String blobName = String.format(historyPath, 7);
		Blob blob = bucket.get(blobName);
		String chatHistoryText = "";

		if (blob != null && blob.exists()) {
			String textData = new String(blob.getContent(), StandardCharsets.UTF_8);
			chatHistoryText = extractConversation(textData);
		}

		return chatHistoryText;
	}
}

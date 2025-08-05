package com.app.logic;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import com.app.dto.AD0001.AD0001DownloadDto;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@RestController
public class AD0001Logic {

    @Value("${gcs.bucket.name}")
    private String bucketName;

    private static final String BLOB_NAME_TEMPLATE = "/chatHistory_%d.md";

    public  File downloadChatHistory(AD0001DownloadDto dto) throws IOException {
    	
    	String name = dto.getName();
    	String historyPath = name + BLOB_NAME_TEMPLATE;
        Storage storage = StorageOptions.getDefaultInstance().getService();
   
        Bucket bucket = storage.get(bucketName);

        File tempFile = File.createTempFile("chatHistory", ".md");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tempFile, false), StandardCharsets.UTF_8))) {

            for (int i = 1; i <= 4; i++) {
                String blobName = String.format(historyPath, i);
                Blob blob = bucket.get(blobName);

                if (blob == null || !blob.exists()) {
                    continue;
                }

                String textData = new String(blob.getContent(), StandardCharsets.UTF_8);
                String chatHistoryText = extractConversation(textData);

                if (i == 1) {
                    chatHistoryText = "### 会話履歴\n"
                            + "けいと:\n\n"
                            + "こんにちは、" + name + "\n"
                            + "まずは「自分にとっての幸せとは」というテーマで価値観を深探りしていきましょう 。\n"
                            + "まずはあなたにとっての幸せとは何か1〜3個あげてください。\n\n"
                            + "例:\n"
                            + "- お金をかせぐこと\n"
                            + "- 週末にちょっと贅沢して、好きなお店でごはんを食べる時間\n"
                            + "- 来年はこんなことしたいな、って目標や夢がある状態\n\n"
                            + chatHistoryText;
                } else if (i == 2) {
                    chatHistoryText = "\n\n__次のテーマへの移行__\n\n"
                            + "けいと:\n\n"
                            + name + "ありがとうございます。\n"
                            + "次は「何を大切にして生きてるのか」というテーマで価値観を深探りしていきましょう 。\n"
                            + "まずはあなたは何を大切にして生きてるのか1〜3個あげてください\n\n"
                            + chatHistoryText;
                } else if (i == 3) {
                    chatHistoryText = "\n\n__次のテーマへの移行__\n\n"
                            + "けいと:\n\n"
                            + name + "ありがとうございます。\n"
                            + "次は「なぜ今その選択をしているのか」というテーマで価値観を深探りしていきましょう 。\n"
                            + chatHistoryText;
                } else if (i == 4) {
                    chatHistoryText = "\n\n__次のテーマへの移行__\n\n"
                            + "けいと:\n\n"
                            + name + "ありがとうございます。\n"
                            + "次は「社会に不満を感じることは」というテーマで価値観を深探りしていきましょう 。\n"
                            + chatHistoryText;
                }

                writer.write(chatHistoryText);
            }
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
}

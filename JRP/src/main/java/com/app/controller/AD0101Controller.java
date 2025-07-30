package com.app.controller;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.constant.ActionName;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@RestController
@RequestMapping("/api/AD0101")
public class AD0101Controller {

    @Value("${gcs.bucket.name}")
    private String bucketName;

    private static final String NAME = "ゆうやさん";
    private static final String BLOB_NAME_TEMPLATE = NAME + "/chatHistory_%d.md";

    @GetMapping(ActionName.DOWNLOAD)
    public ResponseEntity<InputStreamResource> downloadChatHistory() throws IOException {
        Storage storage = StorageOptions.getDefaultInstance().getService();
   
        Bucket bucket = storage.get(bucketName);

        File tempFile = File.createTempFile("chatHistory", ".md");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tempFile, false), StandardCharsets.UTF_8))) {

            for (int i = 1; i <= 4; i++) {
                String blobName = String.format(BLOB_NAME_TEMPLATE, i);
                Blob blob = bucket.get(blobName);

                if (blob == null || !blob.exists()) {
                    continue;
                }

                String textData = new String(blob.getContent(), StandardCharsets.UTF_8);
                String chatHistoryText = extractConversation(textData);

                if (i == 1) {
                    chatHistoryText = "### 会話履歴\n"
                            + "けいと:\n\n"
                            + "こんにちは、" + NAME + "\n"
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
                            + NAME + "ありがとうございます。\n"
                            + "次は「何を大切にして生きてるのか」というテーマで価値観を深探りしていきましょう 。\n"
                            + "まずはあなたは何を大切にして生きてるのか1〜3個あげてください\n\n"
                            + chatHistoryText;
                } else if (i == 3) {
                    chatHistoryText = "\n\n__次のテーマへの移行__\n\n"
                            + "けいと:\n\n"
                            + NAME + "ありがとうございます。\n"
                            + "次は「なぜ今その選択をしているのか」というテーマで価値観を深探りしていきましょう 。\n"
                            + chatHistoryText;
                } else if (i == 4) {
                    chatHistoryText = "\n\n__次のテーマへの移行__\n\n"
                            + "けいと:\n\n"
                            + NAME + "ありがとうございます。\n"
                            + "次は「社会に不満を感じることは」というテーマで価値観を深探りしていきましょう 。\n"
                            + chatHistoryText;
                }

                writer.write(chatHistoryText);
            }
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(tempFile));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=chatHistory.md")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(tempFile.length())
                .body(resource);
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

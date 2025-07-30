package com.app.config;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

@Configuration
public class FirebaseConfig {
	@Bean
	public Firestore firestore() throws IOException  {
		// FirebaseAppがすでに初期化されていない場合のみ実行
        if (FirebaseApp.getApps().isEmpty()) {
            // 環境変数 GOOGLE_APPLICATION_CREDENTIALS からパスを取得
            String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            FileInputStream serviceAccount = new FileInputStream(credentialsPath);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        }

        // FirestoreインスタンスをBeanとして返却
        return FirestoreClient.getFirestore();
	}
}

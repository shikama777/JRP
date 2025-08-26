package com.app.controller.ST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

public class STController {
	
	@Autowired
	private Firestore firestore;
	
	@Autowired
	private MessageSource messageSource;
	
	protected String getHistoryId(String userId) {
		ApiFuture<DocumentSnapshot>  data = firestore.collection("users").document(userId).get();
		String historyId = "";
		
		try {
		    DocumentSnapshot document = data.get();
		    if (document.exists()) {
		        // history_id フィールドを String として取得
		        historyId = document.getString("history_id");
		    } else {
		        System.out.println("指定した documentId のドキュメントは存在しません。");
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}

		return historyId;
	}

}

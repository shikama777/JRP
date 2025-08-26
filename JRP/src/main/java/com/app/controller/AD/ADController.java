package com.app.controller.AD;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.app.dto.UserDto;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

public class ADController {
	@Autowired
	private Firestore firestore;
	
	@Autowired
	private MessageSource messageSource;
	
	protected List<UserDto> getUserList() {
		ApiFuture<QuerySnapshot> data = firestore.collection("users").get();
		
		List<UserDto> dtoList = new ArrayList<>();
		
		try {
			List<QueryDocumentSnapshot> documents = data.get().getDocuments();
			
			for (QueryDocumentSnapshot document : documents) {
				if (document.getId().equals("base")) {
					// コレクション削除防止用
					continue;
				}
	
				UserDto dto = new UserDto();
				dto.setId(document.getId()); // ドキュメントIDをセット
				dto.setName(document.getString("name")); // ドキュメントから名前を取得
				dtoList.add(dto);
			}
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		
		return dtoList;
	}
	
	protected String getMessage(String code) {
		return messageSource.getMessage(code, null, Locale.JAPAN);
	}
}

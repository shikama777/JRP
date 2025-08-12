package com.app.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.constant.ActionName;
import com.app.constant.CollectionName;
import com.app.dto.ResponseDto;
import com.app.dto.AD0002.AD0002Dto;
import com.app.dto.AD0002.AD0002GetDto;
import com.app.dto.AD0002.AD0002UpdateDto;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

@RestController
@RequestMapping("/api/AD0002")
public class AD0002Controller {
	@Autowired
	private Firestore firestore;
	
	@Value("${gcs.bucket.name}")
    private String bucketName;

	@GetMapping(ActionName.DEFAULT)
	public List<AD0002Dto> initializer() {
		ApiFuture<QuerySnapshot> data = firestore.collection("users").get();
		
		List<AD0002Dto> dtoList = new ArrayList<>();
		
		try {
			List<QueryDocumentSnapshot> documents = data.get().getDocuments();
			
			for (QueryDocumentSnapshot document : documents) {
				if (document.getId().equals("base")) {
					// コレクション削除防止用
					continue;
				}
	
				AD0002Dto dto = new AD0002Dto();
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
	
	@PostMapping(ActionName.GET)
	public List<AD0002GetDto> getAD0002(@RequestBody AD0002Dto searchDto) {
		
		String id = searchDto.getId();
		ApiFuture<QuerySnapshot> data = firestore.collection(CollectionName.MOTIVATION)
				.document(id)
				.collection(CollectionName.MOTIVATION_ITEMS)
				.orderBy("order")
				.get();
		
		List<AD0002GetDto> dtoList = new ArrayList<>();
		
		try {
			List<QueryDocumentSnapshot> documents = data.get().getDocuments();
			
			for (QueryDocumentSnapshot document : documents) {
				if (document.getId().equals("base")) {
					// コレクション削除防止用
					continue;
				}

				AD0002GetDto dto = document.toObject(AD0002GetDto.class);
				dto.setId(document.getId()); // ドキュメントIDをセット
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
	
	@PostMapping(ActionName.UPDATE)
	public ResponseDto  update(@RequestBody AD0002UpdateDto dto) {
		ResponseDto response = new ResponseDto();
		String id = dto.getId();

		try {
			// Retrieve the single document in the COMMENT collection
	        ApiFuture<QuerySnapshot> query = firestore.collection(CollectionName.MOTIVATION)
	                .document(id)
	                .collection(CollectionName.COMMENT)
	                .get();

	        List<QueryDocumentSnapshot> documents = query.get().getDocuments();

	        if (documents.size() == 1) {
	            // Get the document ID
	            String documentId = documents.get(0).getId();

	            // Perform the update
	            firestore.collection(CollectionName.MOTIVATION)
	                    .document(id)
	                    .collection(CollectionName.COMMENT)
	                    .document(documentId)
	                    .update(
								"comment", dto.getComment()
	                    )
	                    .get();

	            
	            response.setSuccess(true);
	        } else {
	           response.setMessage("Expected exactly one document, but found: " + documents.size());
	        }
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		return response;
	}
}

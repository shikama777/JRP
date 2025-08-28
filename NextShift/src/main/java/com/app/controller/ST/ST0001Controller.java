package com.app.controller.ST;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.constant.ActionName;
import com.app.constant.CollectionName;
import com.app.dto.ResponseDto;
import com.app.dto.ST0001.ST0001Dto;
import com.app.dto.ST0001.ST0001UpdateDto;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

@RestController
@RequestMapping("/api/ST0001")
public class ST0001Controller {
	
	@Autowired
	private Firestore firestore;

	@GetMapping(ActionName.DEFAULT)
	public ST0001Dto get(Authentication authentication) {
		
		String id = authentication.getName();
		ApiFuture<QuerySnapshot> data = firestore.collection(CollectionName.KACHIKAN)
				.document(id)
				.collection(CollectionName.KACHIKAN_ITEMS)
				.get();
		
		ST0001Dto dto = new ST0001Dto();

		ApiFuture<QuerySnapshot> comment = firestore.collection(CollectionName.KACHIKAN)
				.document(id)
				.collection(CollectionName.COMMENT)
				.get();

		try {
			List<QueryDocumentSnapshot> documents = data.get().getDocuments();
			
			for (QueryDocumentSnapshot document : documents) {
				String[] dataList = new String[3];
				switch (document.getLong("id").intValue()) {
					case 1:
						dataList[0] = document.getString("data1");
						dataList[1] = document.getString("data2");
						dataList[2] = document.getString("data3");
						dto.setData1(dataList);
						break;
					case 2:
			            dataList[0] = document.getString("data1");
			            dataList[1] = document.getString("data2");
			            dataList[2] = document.getString("data3");
			            dto.setData2(dataList);
			            break;
			        case 3:
			            dataList[0] = document.getString("data1");
			            dataList[1] = document.getString("data2");
			            dataList[2] = document.getString("data3");
			            dto.setData3(dataList);
			            break;
			        case 4:
			            dataList[0] = document.getString("data1");
			            dataList[1] = document.getString("data2");
			            dataList[2] = document.getString("data3");
			            dto.setData4(dataList);
			            break;
			        case 5:
			            dataList[0] = document.getString("data1");
			            dataList[1] = document.getString("data2");
			            dataList[2] = document.getString("data3");
			            dto.setData5(dataList);
			            break;
				}
				
				dto.setComment(comment.get().getDocuments().get(0).getString("comment"));
			}
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	    
		return dto;
	}
	
	@PostMapping(ActionName.UPDATE)
	public ResponseDto update(Authentication authentication, @RequestBody ST0001UpdateDto dto) {
		String id = authentication.getName();
		CollectionReference data = firestore.collection(CollectionName.KACHIKAN)
				.document(id)
				.collection(CollectionName.KACHIKAN_ITEMS);

		try {
				data
				.whereEqualTo("id", 1)
				.get()
				.get()
				.getDocuments()
				.get(0)
				.getReference()
				.update(
						"data1", dto.getData1()[0],
						"data2", dto.getData1()[1],
						"data3", dto.getData1()[2]
						)
				.get();
				
				data
				.whereEqualTo("id", 2)
				.get()
				.get()
				.getDocuments()
				.get(0)
				.getReference()
				.update(
						"data1", dto.getData2()[0],
						"data2", dto.getData2()[1],
						"data3", dto.getData2()[2]
						)
				.get();
				
				data
				.whereEqualTo("id", 3)
				.get()
				.get()
				.getDocuments()
				.get(0)
				.getReference()
				.update(
						"data1", dto.getData3()[0],
						"data2", dto.getData3()[1],
						"data3", dto.getData3()[2]
						)
				.get();
				
				data
				.whereEqualTo("id", 4)
				.get()
				.get()
				.getDocuments()
				.get(0)
				.getReference()
				.update(
						"data1", dto.getData4()[0],
						"data2", dto.getData4()[1],
						"data3", dto.getData4()[2]
						)
				.get();
				
				data
				.whereEqualTo("id", 5)
				.get()
				.get()
				.getDocuments()
				.get(0)
				.getReference()
				.update(
						"data1", dto.getData5()[0],
						"data2", dto.getData5()[1],
						"data3", dto.getData5()[2]
						)
				.get();
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		ResponseDto response = new ResponseDto();
		response.setSuccess(true);
		response.setMessage("更新しました。");
		return response;
	}
}

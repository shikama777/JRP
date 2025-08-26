package com.app.controller.AD;

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
import com.app.dto.UserDto;
import com.app.dto.AD0001.AD0001GetDto;
import com.app.dto.AD0001.AD0001PostDto;
import com.app.dto.AD0001.AD0001UpdateDto;
import com.app.logic.AD0001Logic;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

@RestController
@RequestMapping("/api/AD0001")
public class AD0001Controller extends ADController {
	@Autowired
	private Firestore firestore;
	
	@Autowired
	private AD0001Logic AD0001Logic;
	
	@GetMapping(ActionName.DEFAULT)
	public List<UserDto> initializer() {	    
		return getUserList();
	}
	
	@PostMapping(ActionName.GET)
	public AD0001GetDto get(@RequestBody UserDto userDto) {
		
		String id = userDto.getId();
		ApiFuture<QuerySnapshot> data = firestore.collection(CollectionName.KACHIKAN)
				.document(id)
				.collection(CollectionName.KACHIKAN_ITEMS)
				.get();
		
		ApiFuture<QuerySnapshot> comment = firestore.collection(CollectionName.KACHIKAN)
				.document(id)
				.collection(CollectionName.COMMENT)
				.get();
		
		AD0001GetDto dto = new AD0001GetDto();
		
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
	public ResponseDto  update(Authentication authentication, @RequestBody AD0001UpdateDto dto) {
		ResponseDto response = new ResponseDto();
		String id = dto.getId();

		try {
			// Retrieve the single document in the COMMENT collection
	        ApiFuture<QuerySnapshot> query = firestore.collection(CollectionName.KACHIKAN)
	                .document(id)
	                .collection(CollectionName.COMMENT)
	                .get();

	        List<QueryDocumentSnapshot> documents = query.get().getDocuments();

	        if (documents.size() == 1) {

	            documents
		            .get(0)
		            .getReference()
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

	@PostMapping(ActionName.POST)
	public ResponseDto  post(Authentication authentication, @RequestBody AD0001PostDto dto) throws InterruptedException, ExecutionException {
		String id = dto.getId();
		ResponseDto response = new ResponseDto();
		
		if (getHistoryId(id) == null || getHistoryId(id).isEmpty() || Integer.parseInt(getHistoryId(id)) > 0 ){
			response.setSuccess(false);
			response.setMessage(getMessage("message.alreadyNextStep"));
			return response;
		}
		
		DocumentReference document = firestore.collection(CollectionName.USERS).document(id);
		document.update("history_id", "1");
		
		AD0001Logic.createKachikanData(id, document.get().get().getString("name"));

		
        return response;
    }

}

package com.app.controller.ST;

import java.util.ArrayList;
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
import com.app.dto.CommentDto;
import com.app.dto.ResponseDto;
import com.app.dto.ST0002.ST0002CreateDto;
import com.app.dto.ST0002.ST0002Dto;
import com.app.dto.ST0002.ST0002UpdateDto;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import lombok.Data;

@RestController
@RequestMapping("/api/ST0002")
public class ST0002Controller extends STController {
	
	@Autowired
	private Firestore firestore;
	
	@Data
	public class ST0002Response {
		private boolean inComplete;
		List<ST0002Dto> list;
	}

	@GetMapping(ActionName.DEFAULT)
	public ST0002Response initializer(Authentication authentication) {
		
		ST0002Response response = new ST0002Response();
		
		String id = authentication.getName();
		
		if (getHistoryId(id) == null || getHistoryId(id).isEmpty() || Integer.parseInt(getHistoryId(id)) < 6 ){
			response.setInComplete(true);
		} else {
			response.setInComplete(false);
		}
		
		ApiFuture<QuerySnapshot> data = firestore.collection(CollectionName.MOTIVATION)
				.document(id)
				.collection(CollectionName.MOTIVATION_ITEMS)
				.orderBy("order")
				.get();
		
		List<ST0002Dto> dtoList = new ArrayList<>();
		
		try {
			List<QueryDocumentSnapshot> documents = data.get().getDocuments();
			
			for (QueryDocumentSnapshot document : documents) {
				if (document.getId().equals("base")) {
					// コレクション削除防止用
					continue;
				}

				ST0002Dto dto = document.toObject(ST0002Dto.class);
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
		
		response.setList(dtoList);
	    
		return response;
	}

	@PostMapping(ActionName.GET)
	public CommentDto getComment(Authentication authentication) {
		String userId = authentication.getName();
		ApiFuture<QuerySnapshot> comment = firestore.collection(CollectionName.MOTIVATION)
				.document(userId)
				.collection(CollectionName.COMMENT)
				.get();

		CommentDto dto = new CommentDto();

		try {
			List<QueryDocumentSnapshot> documents = comment.get().getDocuments();
			dto.setComment(documents.get(0).getString("comment"));
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return dto;
	}

	@PostMapping(ActionName.UPDATE)
	public ResponseDto update(Authentication authentication, @RequestBody List<ST0002UpdateDto> dtoList) {
		String id = authentication.getName();

		try {
			for (ST0002UpdateDto item : dtoList) {
				if (item.getId() == null || item.getId().isEmpty()) {
					ST0002CreateDto addDto = new ST0002CreateDto();
					addDto.setAge(item.getAge());
					addDto.setMotivation(item.getMotivation());
					addDto.setEvent(item.getEvent());
					addDto.setMind(item.getMind());
					addDto.setOrder(item.getOrder());

					// 新規行の追加処理
					firestore.collection("motivation")
					.document(id)
					.collection(CollectionName.MOTIVATION_ITEMS)
					.add(addDto)
					.get();
				} else if (item.isChanged())  {
					firestore.collection("motivation")
							.document(id)
							.collection(CollectionName.MOTIVATION_ITEMS)
							.document(item.getId())
							.update(
									"age", item.getAge(),
									"motivation", item.getMotivation(),
									"event", item.getEvent(),
									"mind", item.getMind()
							)
							.get();
				} else if (item.isDeleted()) {
                    // 削除処理
                    firestore.collection("motivation")
                            .document(id)
                            .collection(CollectionName.MOTIVATION_ITEMS)
                            .document(item.getId())
                            .delete()
                            .get();
                }
			}
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

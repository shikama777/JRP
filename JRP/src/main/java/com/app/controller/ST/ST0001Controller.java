package com.app.controller.ST;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.constant.ActionName;
import com.app.constant.CollectionName;
import com.app.dto.ResponseDto;
import com.app.dto.ST0001.ST0001CreateDto;
import com.app.dto.ST0001.ST0001Dto;
import com.app.dto.ST0001.ST0001UpdateDto;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

@RestController
@RequestMapping("/api/ST0001")
public class ST0001Controller {
	
	@Autowired
	private Firestore firestore;

	@GetMapping(ActionName.DEFAULT)
	public List<ST0001Dto> getAD0001() {
		ApiFuture<QuerySnapshot> aaa = firestore.collection("motivation")
				.document( "NNFx28crvQsiGzbSHnqR")
				.collection(CollectionName.MOTIVATION_ITEMS)
				.orderBy("order")
				.get();
		
		List<ST0001Dto> dtoList = new ArrayList<>();
		
		try {
			List<QueryDocumentSnapshot> documents = aaa.get().getDocuments();
			
			for (QueryDocumentSnapshot document : documents) {
				if (document.getId().equals("base")) {
					// コレクション削除防止用
					continue;
				}

				ST0001Dto dto = document.toObject(ST0001Dto.class);
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
	public ResponseDto update(@RequestBody List<ST0001UpdateDto> dtoList) {		
		try {
			for (ST0001UpdateDto item : dtoList) {
				if (item.getId() == null || item.getId().isEmpty()) {
					ST0001CreateDto addDto = new ST0001CreateDto();
					addDto.setAge(item.getAge());
					addDto.setMotivation(item.getMotivation());
					addDto.setEvent(item.getEvent());
					addDto.setMind(item.getMind());
					addDto.setOrder(item.getOrder());

					// 新規行の追加処理
					firestore.collection("motivation")
					.document("NNFx28crvQsiGzbSHnqR")
					.collection(CollectionName.MOTIVATION_ITEMS)
					.add(addDto)
					.get();
				} else if (item.isChanged())  {
					firestore.collection("motivation")
							.document("NNFx28crvQsiGzbSHnqR")
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
                            .document("NNFx28crvQsiGzbSHnqR")
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

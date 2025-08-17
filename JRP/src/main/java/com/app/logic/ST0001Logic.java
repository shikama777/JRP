package com.app.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.app.constant.CollectionName;
import com.app.dto.CommentDto;
import com.app.dto.ST0001.ST0001CreateDto;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;

@Component
public class ST0001Logic {

	@Autowired
	private Firestore firestore;

	// 今はAD0101から呼び出し
	public void createKachikan(String Id) {

		List<ST0001CreateDto> dtoList = new ArrayList<>();

		CollectionReference collection = firestore.collection(CollectionName.KACHIKAN)
				.document(Id)
				.collection(CollectionName.KACHIKAN_ITEMS);
		
		CollectionReference commentCollection = firestore.collection(CollectionName.KACHIKAN)
				.document(Id)
				.collection(CollectionName.COMMENT);

		for (int i = 1; i < 6; i++) {
			ST0001CreateDto dto = new ST0001CreateDto();
			dto.setId(i);
			dto.setData1("");
			dto.setData2("");
			dto.setData3("");

			dtoList.add(dto);
			collection.add(dto);
		}
		
		CommentDto commentDto = new CommentDto();
		commentDto.setComment("");
		
		commentCollection.add(commentDto);
	}

	// 今はAD0101から呼び出し
	public void deleteKachikan(String Id) {
		try {
			DocumentReference motivationRef = firestore.collection(CollectionName.KACHIKAN).document(Id);

			List<QueryDocumentSnapshot> motivationItems = motivationRef
					.collection(CollectionName.KACHIKAN_ITEMS)
					.get()
					.get()
					.getDocuments();
			
			List<QueryDocumentSnapshot> commentItems = motivationRef
					.collection(CollectionName.KACHIKAN_ITEMS)
					.get()
					.get()
					.getDocuments();

			for (QueryDocumentSnapshot item : motivationItems) {
				motivationRef.collection(CollectionName.KACHIKAN_ITEMS)
						.document(item.getId())
						.delete()
						.get();
			}
			
			for (QueryDocumentSnapshot item : commentItems) {
				motivationRef.collection(CollectionName.COMMENT)
						.document(item.getId())
						.delete()
						.get();
			}

			motivationRef.delete().get();
		} catch (InterruptedException e) {
			System.out.println(e);
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
}

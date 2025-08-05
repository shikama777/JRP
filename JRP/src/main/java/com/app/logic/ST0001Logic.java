package com.app.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.app.constant.CollectionName;
import com.app.dto.ST0001.ST0001CreateDto;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;

@Component
public class ST0001Logic {

	@Autowired
	private Firestore firestore;

	private final String[] NEW_AGE_LIST = { "0～5歳", "6歳", "7歳", "8歳", "9歳", "10歳",
			"11歳", "12歳", "13歳", "14歳", "15歳", "16歳",
			"17歳", "18歳" };

	// 今はAD0001から呼び出し
	public void createMotivation(String Id) {

		List<ST0001CreateDto> dtoList = new ArrayList<>();

		CollectionReference collection = firestore.collection(CollectionName.MOTIVATION)
				.document(Id)
				.collection(CollectionName.MOTIVATION_ITEMS);

		for (int i = 0; i < NEW_AGE_LIST.length; i++) {
			ST0001CreateDto dto = new ST0001CreateDto();
			dto.setAge(NEW_AGE_LIST[i]);
			dto.setMotivation("0");
			dto.setEvent("");
			dto.setMind("");
			dto.setOrder(i);

			dtoList.add(dto);
			collection.add(dto);

		};
	}

	// 今はAD0001から呼び出し
	public void deleteMotivation(String Id) {
		try {
			DocumentReference motivationRef = firestore.collection("motivation").document(Id);

			List<QueryDocumentSnapshot> motivationItems = motivationRef
					.collection(CollectionName.MOTIVATION_ITEMS)
					.get()
					.get()
					.getDocuments();

			for (QueryDocumentSnapshot item : motivationItems) {
				motivationRef.collection(CollectionName.MOTIVATION_ITEMS)
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

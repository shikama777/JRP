package com.app.controller.AD;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.constant.ActionName;
import com.app.constant.CollectionName;
import com.app.dto.ResponseDto;
import com.app.dto.AD0101.AD0101CreateDto;
import com.app.dto.AD0101.AD0101DeleteDto;
import com.app.dto.AD0101.AD0101DownloadDto;
import com.app.dto.AD0101.AD0101Dto;
import com.app.dto.AD0101.AD0101UpdateDto;
import com.app.logic.AD0101Logic;
import com.app.logic.ST0001Logic;
import com.app.logic.ST0002Logic;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

@RestController
@RequestMapping("/api/AD0101")
public class AD0101Controller {

	@Autowired
	private Firestore firestore;

	@Autowired
	private AD0101Logic logic;

	@Autowired
	private ST0001Logic ST0001Logic;
	
	@Autowired
	private ST0002Logic ST0002Logic;

	@Value("${gcs.bucket.name}")
	private String bucketName;

	@GetMapping(ActionName.DEFAULT)
	public List<AD0101Dto> getAD0101() {
		ApiFuture<QuerySnapshot> data = firestore.collection(CollectionName.USERS).get();

		List<AD0101Dto> dtoList = new ArrayList<>();

		try {
			List<QueryDocumentSnapshot> documents = data.get().getDocuments();

			for (QueryDocumentSnapshot document : documents) {
				if (document.getId().equals("base")) {
					// コレクション削除防止用
					continue;
				}

				AD0101Dto dto = document.toObject(AD0101Dto.class);
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

	@PostMapping(ActionName.INSERT)
	public ResponseDto create(@RequestBody AD0101CreateDto dto) {

		if (dto.getName() == null || dto.getName().equals("さん")) {
			ResponseDto response = new ResponseDto();
			response.setSuccess(false);
			response.setMessage("ユーザー名を入力してください。");
			return response;
		}

		try {
			DocumentReference documentReference = firestore.collection(CollectionName.USERS).add(dto).get();

			ST0001Logic.createKachikan(documentReference.getId());
			ST0002Logic.createMotivation(documentReference.getId());
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		ResponseDto response = new ResponseDto();
		response.setSuccess(true);
		response.setMessage("登録しました。");
		return response;
	}

	@PostMapping(ActionName.UPDATE)
	public ResponseDto update(@RequestBody AD0101UpdateDto dto) {
		if (dto.getId() == null || dto.getId().isEmpty()) {
			ResponseDto response = new ResponseDto();
			response.setSuccess(false);
			response.setMessage("ユーザーを選択してください。");
			return response;
		}

		if (dto.getName() == null || dto.getName().equals("さん")) {
			ResponseDto response = new ResponseDto();
			response.setSuccess(false);
			response.setMessage("ユーザー名を入力してください。");
			return response;
		}

		try {
			firestore.collection("users")
					.document(dto.getId())
					.update(
							"name", dto.getName(),
							"spreadsheet_id", dto.getSpreadsheet_id(),
							"gmail", dto.getGmail())
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

	@PostMapping(ActionName.DELETE)
	public ResponseDto delete(@RequestBody AD0101DeleteDto dto) {
		if (dto.getId() == null || dto.getId().isEmpty()) {
			ResponseDto response = new ResponseDto();
			response.setSuccess(false);
			response.setMessage("ユーザーを選択してください。");
			return response;
		}

		try {
			firestore.collection("users")
					.document(dto.getId())
					.delete().get();

			ST0001Logic.deleteKachikan(dto.getId());
			ST0002Logic.deleteMotivation(dto.getId());
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		ResponseDto response = new ResponseDto();
		response.setSuccess(true);
		response.setMessage("削除しました。");
		return response;

	}

	@PostMapping(ActionName.DOWNLOAD)
	public ResponseEntity<InputStreamResource> download(@RequestBody AD0101DownloadDto dto) {

		File tempFile;
		InputStreamResource resource;

		// chatHistory.md ファイルをダウンロードする
		try {
			tempFile = logic.downloadChatHistory(dto);
			resource = new InputStreamResource(new FileInputStream(tempFile));

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=chatHistory.md")
					.contentType(MediaType.TEXT_PLAIN)
					.contentLength(tempFile.length())
					.body(resource);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return ResponseEntity.status(500).body(null);
	}
}

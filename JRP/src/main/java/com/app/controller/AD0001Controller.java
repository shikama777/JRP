package com.app.controller;

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
import com.app.dto.ResponseDto;
import com.app.dto.AD0001.AD0001CreateDto;
import com.app.dto.AD0001.AD0001DeleteDto;
import com.app.dto.AD0001.AD0001DownloadDto;
import com.app.dto.AD0001.AD0001Dto;
import com.app.dto.AD0001.AD0001UpdateDto;
import com.app.logic.AD0001Logic;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

@RestController
@RequestMapping("/api/AD0001")
public class AD0001Controller {
	
	@Autowired
	private Firestore firestore;
	
	@Autowired
	private AD0001Logic logic;
	
	@Value("${gcs.bucket.name}")
    private String bucketName;

    private static final String BLOB_NAME_TEMPLATE = "/chatHistory_%d.md";

	@GetMapping(ActionName.DEFAULT)
	public List<AD0001Dto> getAD0001() {
		ApiFuture<QuerySnapshot> aaa = firestore.collection("users").get();
		
		List<AD0001Dto> dtoList = new ArrayList<>();
		
		try {
			List<QueryDocumentSnapshot> documents = aaa.get().getDocuments();
			
			for (QueryDocumentSnapshot document : documents) {
				if (document.getId().equals("base")) {
					// コレクション削除防止用
					continue;
				}

				AD0001Dto dto = document.toObject(AD0001Dto.class);
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
	public ResponseDto create(@RequestBody AD0001CreateDto dto) {
			try {
				firestore.collection("users").add(dto).get();
			} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}

		ResponseDto response = new ResponseDto();
		response.setSuccess(true);
		return response;
	}
	
	@PostMapping(ActionName.UPDATE)
	public ResponseDto update(@RequestBody AD0001UpdateDto dto) {
		try {
			firestore.collection("users")
				.document(dto.getId())
				.update(
						"name", dto.getName(),
						"spreadsheet_id", dto.getSpreadsheet_id()
					).get();
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		ResponseDto response = new ResponseDto();
		response.setSuccess(true);
		return response;
	}
	
	@PostMapping(ActionName.DELETE)
	public ResponseDto delete(@RequestBody AD0001DeleteDto dto) {
        try {
            firestore.collection("users")
                .document(dto.getId())
                .delete().get();
        } catch (InterruptedException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }

        ResponseDto response = new ResponseDto();
        response.setSuccess(true);
        return response;

	}
	
	@PostMapping(ActionName.DOWNLOAD)
	public ResponseEntity<InputStreamResource> delete(@RequestBody AD0001DownloadDto dto) {
		
		File tempFile;
		InputStreamResource  resource;

		// chatHistory.md ファイルをダウンロードする
		try {
			tempFile = logic.downloadChatHistory(dto);
			resource = new InputStreamResource(new FileInputStream(tempFile));
			
			return ResponseEntity.ok()
	                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=chatHistory.md")
	                .contentType(MediaType.TEXT_PLAIN)
	                .contentLength(tempFile.length())
	                .body(resource);} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

        return ResponseEntity.status(500).body(null);
	}
}

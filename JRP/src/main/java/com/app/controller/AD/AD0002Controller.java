package com.app.controller.AD;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.app.dto.AD0002.AD0002GetDto;
import com.app.dto.AD0002.AD0002PostDto;
import com.app.dto.AD0002.AD0002UpdateDto;
import com.app.logic.AD0002Logic;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

@RestController
@RequestMapping("/api/AD0002")
public class AD0002Controller extends ADController {
	@Autowired
	private Firestore firestore;
	
	@Autowired
	private AD0002Logic AD0002Logic;
	
	@Value("${gcs.bucket.name}")
    private String bucketName;
	
	@Value("${line.bot.channelAccessToken}")
	private String channelAccessToken;

	@GetMapping(ActionName.DEFAULT)
	public List<UserDto> initializer() {	    
		return getUserList();
	}
	
	@PostMapping(ActionName.GET)
	public List<AD0002GetDto> getAD0002(@RequestBody UserDto userDto) {
		
		String id = userDto.getId();
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
	public ResponseDto  post(Authentication authentication, @RequestBody AD0002PostDto dto) {
		String id = dto.getId();
		ResponseDto response = new ResponseDto();
		
		try {
			DocumentReference document = firestore.collection("users").document(id);

	        String userId = document.get().get().getString("line_id"); // 送り先の line_id

			if (userId == null || userId.isEmpty()) {
				return new ResponseDto() {
					{
						setSuccess(false);
						setMessage("LINE IDが設定されていません。");
					}
				};
			} else {

				if (document.get().get().getString("history_id").equals("6")) {
					document.update("history_id", "7");
					AD0002Logic.createMotivationData(id);

			        String messageText = "Javaからのテスト送信！";
			
			        String jsonPayload = """
			            {
			              "to": "%s",
			              "messages": [
			                {
			                  "type": "text",
			                  "text": "%s"
			                }
			              ]
			            }
			            """.formatted(userId, messageText);
			
			        HttpRequest request = HttpRequest.newBuilder()
			            .uri(URI.create("https://api.line.me/v2/bot/message/push"))
			            .header("Content-Type", "application/json")
			            .header("Authorization", "Bearer " + channelAccessToken)
			            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
			            .build();
			
			        HttpClient client = HttpClient.newHttpClient();
			        client.send(request, HttpResponse.BodyHandlers.ofString());
			        response.setMessage("メッセージを送信しました。");
				} else if(Integer.parseInt(document.get().get().getString("history_id")) > 6) {
					response.setMessage("ユーザーは既に次のステップに進んでます。");
				}  else if(Integer.parseInt(document.get().get().getString("history_id")) < 6) {
					response.setMessage("ユーザーは前のステップが完了していません。");
				} else {
					response.setMessage("ユーザーが存在しません。");
				}
			}
		} catch (IOException | InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

        return response;
    }
}

package com.app.dto.AD0001;

import lombok.Data;

@Data
public class AD0001UpdateDto {
	private String id; // ドキュメントID
	private String name;
	private String spreadsheet_id;
	
	public void setName(String name) {
        this.name = name + "さん";
    }
}

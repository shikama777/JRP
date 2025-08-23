package com.app.dto.AD0101;

import lombok.Data;

@Data
public class AD0101UpdateDto {
	private String id; // ドキュメントID
	private String name;
	private String spreadsheet_id;
	private String gmail;
	
	public void setName(String name) {
        this.name = name + "さん";
    }
}

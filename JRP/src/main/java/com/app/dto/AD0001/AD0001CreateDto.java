package com.app.dto.AD0001;

import lombok.Data;

@Data
public class AD0001CreateDto {
	private String name;
	private String spreadsheet_id;
	private String history_id;
	private String line_id;
	
	public void setName(String name) {
        this.name = name + "さん";
    }
}

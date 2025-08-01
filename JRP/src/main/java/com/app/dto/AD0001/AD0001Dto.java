package com.app.dto.AD0001;

import lombok.Data;

@Data
public class AD0001Dto {
	private String id;
	private String name;
	private String spreadsheet_id;
	private String history_id;
	private String line_id;
	
	public String getName() {
		return name.substring(0, name.length() - 2);
	}
}

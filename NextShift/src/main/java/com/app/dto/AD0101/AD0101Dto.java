package com.app.dto.AD0101;

import lombok.Data;

@Data
public class AD0101Dto {
	private String id;
	private String name;
	private String spreadsheet_id;
	private String history_id;
	private String line_id;
	private String gmail;
	
	public String getName() {
		return name.substring(0, name.length() - 2);
	}
}

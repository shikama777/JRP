package com.app.dto.AD0101;

import lombok.Data;

@Data
public class AD0101DownloadDto {
	String name;
	String id;
	int downloadNo;
	
	public void setName(String name) {
        this.name = name + "さん";
	}
}

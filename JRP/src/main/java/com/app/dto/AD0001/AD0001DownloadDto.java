package com.app.dto.AD0001;

import lombok.Data;

@Data
public class AD0001DownloadDto {
	String name;
	
	public void setName(String name) {
        this.name = name + "さん";
    }
}

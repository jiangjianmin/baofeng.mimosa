package com.guohuai.file;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDownloadReq {

	private List<DownloadFkey> fkeys;

	private List<DownloadFile> files;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class DownloadFkey {
		private String fkey;
		private String cate;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class DownloadFile {
		private String furl;
		private int size;
		private String name;
	}

}

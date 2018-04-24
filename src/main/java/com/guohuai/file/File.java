package com.guohuai.file;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.Disview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_GAM_FILE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class File {

	public static final int STATE_Invalid = 0;
	public static final int STATE_Valid = 1;

	public static final String CATE_User = "USER";

	@Id
	private String oid;
	@Disview
	private String fkey;
	private String name;
	private String furl;
	private long size;
	private String sizeh;
	@Disview
	private String cate;
	@Disview
	private int state;
	private String fversion;
	@Disview
	private String operator;
	@Disview
	private Timestamp updateTime;
	@Disview
	private Timestamp createTime;

	public String getNameWithoutSuffix() {
		if (StringUtil.isEmpty(this.name)) {
			return "";
		}
		if (this.name.indexOf(".") == -1) {
			return this.name;
		}
		return this.name.substring(0, this.name.lastIndexOf("."));
	}

}

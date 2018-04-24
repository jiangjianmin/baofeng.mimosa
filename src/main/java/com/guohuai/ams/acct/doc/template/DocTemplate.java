package com.guohuai.ams.acct.doc.template;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.guohuai.ams.acct.doc.type.DocType;
import com.guohuai.ams.acct.doc.word.DocWord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "T_ACCT_DOC_TEMPLATE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
public class DocTemplate implements Serializable {

	private static final long serialVersionUID = -5452547693688808084L;

	// 状态: 启用
	public static final String STATE_Enable = "ENABLE";
	// 状态: 禁用
	public static final String STATE_Disable = "DISABLE";

	@Id
	private String oid;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "docwordOid", referencedColumnName = "oid")
	private DocWord docword;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "typeOid", referencedColumnName = "oid")
	private DocType type;
	private String name;
	private String initName;
	private String state;
}

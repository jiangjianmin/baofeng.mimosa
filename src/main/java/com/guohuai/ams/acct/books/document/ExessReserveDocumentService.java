package com.guohuai.ams.acct.books.document;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.ams.acct.account.AccountService;
import com.guohuai.ams.acct.books.AccountBook;
import com.guohuai.ams.acct.books.AccountBookService;
import com.guohuai.ams.acct.books.document.entry.DocumentEntry;
import com.guohuai.ams.acct.books.document.entry.DocumentEntryService;
import com.guohuai.ams.acct.books.document.sn.DocumentSnService;
import com.guohuai.ams.acct.doc.template.DocTemplate;
import com.guohuai.ams.acct.doc.template.DocTemplateService;
import com.guohuai.ams.acct.doc.template.entry.DocTemplateEntry;
import com.guohuai.ams.acct.doc.template.entry.DocTemplateEntryService;
import com.guohuai.ams.acct.doc.word.DocWordService;
import com.guohuai.component.util.StringUtil;

/**
 * 会计凭证 - 备付金业务
 * 
 * @author Arthur
 *
 */
@Service
public class ExessReserveDocumentService {

	@Autowired
	private DocumentDao documentDao;
	@Autowired
	private DocWordService docWordService;
	@Autowired
	private DocumentSnService documentSnService;
	@Autowired
	private DocumentEntryService documentEntryService;
	@Autowired
	private DocTemplateService docTemplateService;
	@Autowired
	private DocTemplateEntryService docTemplateEntryService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private AccountBookService accountBookService;

	// 从外场调入备付金补充资产池流动性
	@Transactional
	public Document credit(String relative, String ticket, BigDecimal amount) {
		DocTemplate template = this.docTemplateService.safeGet("0040_BFJM_0001_BFJDR");
		Map<String, DocTemplateEntry> maps = null;
		if (null != template) {
			maps = this.docTemplateEntryService.entryMap(template);
		}
		int seq = 1;
		long timestamp = System.currentTimeMillis();
		Date date = new Date(timestamp);
		Timestamp now = new Timestamp(timestamp);

		Document document = new Document();
		document.setOid(StringUtil.uuid());
		document.setDocword(null == template || null == template.getDocword() ? this.docWordService.get() : template.getDocword());
		document.setType(Document.TYPE_EXESS_RESERVE);
		document.setRelative(relative);
		document.setTicket(ticket);
		document.setAcctSn(this.documentSnService.genSn(date));
		document.setAcctDate(date);
		document.setInvoiceNum(0);
		document.setDrAmount(BigDecimal.ZERO);
		document.setCrAmount(BigDecimal.ZERO);
		document.setCreateTime(now);
		document.setUpdateTime(now);

		AccountBook b_1001 = this.accountBookService.safeGet(relative, this.accountService.get("1001"));
		AccountBook b_2101 = this.accountBookService.safeGet(relative, this.accountService.get("2101"));

		// 借 银行存款 1001
		DocumentEntry e1_1001 = new DocumentEntry();
		e1_1001.setOid(StringUtil.uuid());
		e1_1001.setRelative(relative);
		e1_1001.setTicket(ticket);
		e1_1001.setDocument(document);
		e1_1001.setBook(b_1001);
		e1_1001.setDigest(null != maps && maps.containsKey("0040_0001_1001_01") ? maps.get("0040_0001_1001_01").getDigest() : "");
		e1_1001.setDrAmount(amount);
		e1_1001.setCrAmount(BigDecimal.ZERO);
		e1_1001.setSeq(seq);
		document.setDrAmount(document.getDrAmount().add(amount));
		b_1001 = this.accountBookService.incrCredit(b_1001, amount);
		seq++;

		// 贷 应付备付金 2101
		DocumentEntry e2_2101 = new DocumentEntry();
		e2_2101.setOid(StringUtil.uuid());
		e2_2101.setRelative(relative);
		e2_2101.setTicket(ticket);
		e2_2101.setDocument(document);
		e2_2101.setBook(b_2101);
		e2_2101.setDigest(null != maps && maps.containsKey("0040_0001_2101_02") ? maps.get("0040_0001_2101_02").getDigest() : "");
		e2_2101.setDrAmount(BigDecimal.ZERO);
		e2_2101.setCrAmount(amount);
		e2_2101.setSeq(seq);
		document.setCrAmount(document.getCrAmount().add(amount));
		b_2101 = this.accountBookService.incrCredit(b_2101, amount);
		seq++;

		document = this.documentDao.save(document);
		this.documentEntryService.save(e1_1001);
		this.documentEntryService.save(e2_2101);

		return document;

	}

	// 资产池流动性充裕偿还外场备付金
	public Document repayment(String relative, String ticket, BigDecimal amount) {
		DocTemplate template = this.docTemplateService.safeGet("0040_BFJM_0002_BFJCH");
		Map<String, DocTemplateEntry> maps = null;
		if (null != template) {
			maps = this.docTemplateEntryService.entryMap(template);
		}
		int seq = 1;
		long timestamp = System.currentTimeMillis();
		Date date = new Date(timestamp);
		Timestamp now = new Timestamp(timestamp);

		Document document = new Document();
		document.setOid(StringUtil.uuid());
		document.setDocword(null == template || null == template.getDocword() ? this.docWordService.get() : template.getDocword());
		document.setType(Document.TYPE_EXESS_RESERVE);
		document.setRelative(relative);
		document.setTicket(ticket);
		document.setAcctSn(this.documentSnService.genSn(date));
		document.setAcctDate(date);
		document.setInvoiceNum(0);
		document.setDrAmount(BigDecimal.ZERO);
		document.setCrAmount(BigDecimal.ZERO);
		document.setCreateTime(now);
		document.setUpdateTime(now);

		AccountBook b_2101 = this.accountBookService.safeGet(relative, this.accountService.get("2101"));
		AccountBook b_1001 = this.accountBookService.safeGet(relative, this.accountService.get("1001"));

		// 借 应付备付金 2101
		DocumentEntry e1_2101 = new DocumentEntry();
		e1_2101.setOid(StringUtil.uuid());
		e1_2101.setRelative(relative);
		e1_2101.setTicket(ticket);
		e1_2101.setDocument(document);
		e1_2101.setBook(b_2101);
		e1_2101.setDigest(null != maps && maps.containsKey("0040_0002_2101_01") ? maps.get("0040_0002_2101_01").getDigest() : "");
		e1_2101.setDrAmount(amount);
		e1_2101.setCrAmount(BigDecimal.ZERO);
		e1_2101.setSeq(seq);
		document.setDrAmount(document.getDrAmount().add(amount));
		b_2101 = this.accountBookService.decrCredit(b_2101, amount);
		seq++;

		// 贷 银行存款 1001
		DocumentEntry e2_1001 = new DocumentEntry();
		e2_1001.setOid(StringUtil.uuid());
		e2_1001.setRelative(relative);
		e2_1001.setTicket(ticket);
		e2_1001.setDocument(document);
		e2_1001.setBook(b_1001);
		e2_1001.setDigest(null != maps && maps.containsKey("0040_0002_1001_02") ? maps.get("0040_0002_1001_02").getDigest() : "");
		e2_1001.setDrAmount(BigDecimal.ZERO);
		e2_1001.setCrAmount(amount);
		e2_1001.setSeq(seq);
		document.setCrAmount(document.getCrAmount().add(amount));
		b_1001 = this.accountBookService.decrCredit(b_1001, amount);
		seq++;

		document = this.documentDao.save(document);
		this.documentEntryService.save(e1_2101);
		this.documentEntryService.save(e2_1001);

		return document;

	}

}

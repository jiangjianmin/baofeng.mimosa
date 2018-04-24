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
 * 会计凭证 - 结算业务
 * 
 * @author Arthur
 *
 */
@Service
public class SettlementDocumentService {

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

	// receipt 入款
	@Transactional
	public Document receipt(String relative, String ticket, BigDecimal amount) {

		DocTemplate template = this.docTemplateService.safeGet("0020_JSZF_0001_JSRK");
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
		document.setType(Document.TYPE_SETTLEMENT);
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
		AccountBook b_1301 = this.accountBookService.safeGet(relative, this.accountService.get("1301"));

		// 借 银行存款 1001
		DocumentEntry e1_1001 = new DocumentEntry();
		e1_1001.setOid(StringUtil.uuid());
		e1_1001.setRelative(relative);
		e1_1001.setTicket(ticket);
		e1_1001.setDocument(document);
		e1_1001.setBook(b_1001);
		e1_1001.setDigest(null != maps && maps.containsKey("0020_0001_1001_01") ? maps.get("0020_0001_1001_01").getDigest() : "");
		e1_1001.setDrAmount(amount);
		e1_1001.setCrAmount(BigDecimal.ZERO);
		e1_1001.setSeq(seq);
		document.setDrAmount(document.getDrAmount().add(amount));
		b_1001 = this.accountBookService.incrCredit(b_1001, amount);
		seq++;

		// 贷 应收投资结算款 1301
		DocumentEntry e2_1301 = new DocumentEntry();
		e2_1301.setOid(StringUtil.uuid());
		e2_1301.setRelative(relative);
		e2_1301.setTicket(ticket);
		e2_1301.setDocument(document);
		e2_1301.setBook(b_1301);
		e2_1301.setDigest(null != maps && maps.containsKey("0020_0001_1301_02") ? maps.get("0020_0001_1301_02").getDigest() : "");
		e2_1301.setDrAmount(BigDecimal.ZERO);
		e2_1301.setCrAmount(amount);
		e2_1301.setSeq(seq);
		document.setCrAmount(document.getCrAmount().add(amount));
		b_1301 = this.accountBookService.decrCredit(b_1301, amount);
		seq++;

		document = this.documentDao.save(document);
		this.documentEntryService.save(e1_1001);
		this.documentEntryService.save(e2_1301);

		return document;

	}

	// outlay 出款
	@Transactional
	public Document outlay(String relative, String ticket, BigDecimal amount) {
		DocTemplate template = this.docTemplateService.safeGet("0020_JSZF_0002_JSCK");
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
		document.setType(Document.TYPE_SETTLEMENT);
		document.setRelative(relative);
		document.setTicket(ticket);
		document.setAcctSn(this.documentSnService.genSn(date));
		document.setAcctDate(date);
		document.setInvoiceNum(0);
		document.setDrAmount(BigDecimal.ZERO);
		document.setCrAmount(BigDecimal.ZERO);
		document.setCreateTime(now);
		document.setUpdateTime(now);

		AccountBook b_2001 = this.accountBookService.safeGet(relative, this.accountService.get("2001"));
		AccountBook b_1001 = this.accountBookService.safeGet(relative, this.accountService.get("1001"));

		// 借 应付投资结算款 2001
		DocumentEntry e1_2001 = new DocumentEntry();
		e1_2001.setOid(StringUtil.uuid());
		e1_2001.setRelative(relative);
		e1_2001.setTicket(ticket);
		e1_2001.setDocument(document);
		e1_2001.setBook(b_2001);
		e1_2001.setDigest(null != maps && maps.containsKey("0020_0002_2001_01") ? maps.get("0020_0002_2001_01").getDigest() : "");
		e1_2001.setDrAmount(amount);
		e1_2001.setCrAmount(BigDecimal.ZERO);
		e1_2001.setSeq(seq);
		document.setDrAmount(document.getDrAmount().add(amount));
		b_2001 = this.accountBookService.decrCredit(b_2001, amount);
		seq++;

		// 贷 银行存款 1001
		DocumentEntry e2_1001 = new DocumentEntry();
		e2_1001.setOid(StringUtil.uuid());
		e2_1001.setRelative(relative);
		e2_1001.setTicket(ticket);
		e2_1001.setDocument(document);
		e2_1001.setBook(b_1001);
		e2_1001.setDigest(null != maps && maps.containsKey("0020_0002_1001_02") ? maps.get("0020_0002_1001_02").getDigest() : "");
		e2_1001.setDrAmount(BigDecimal.ZERO);
		e2_1001.setCrAmount(amount);
		e2_1001.setSeq(seq);
		document.setCrAmount(document.getCrAmount().add(amount));
		b_1001 = this.accountBookService.decrCredit(b_1001, amount);
		seq++;

		document = this.documentDao.save(document);
		this.documentEntryService.save(e1_2001);
		this.documentEntryService.save(e2_1001);

		return document;

	}

}

package com.guohuai.ams.acct.books.document;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
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
 * 会计凭证 - 估值校准
 * 
 * @author Arthur
 *
 */
@Service
public class EstimateCorrectDocumentService {

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

	// 投资标的资产增值
	@Transactional
	public Document targetIncrement(String relative, String ticket, BigDecimal amount) {

		DocTemplate template = this.docTemplateService.safeGet("0060_GZJZ_0001_TARGETZZ");
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
		document.setType(Document.TYPE_ESTIMATE_CORRECT_TARGET);
		document.setRelative(relative);
		document.setTicket(ticket);
		document.setAcctSn(this.documentSnService.genSn(date));
		document.setAcctDate(date);
		document.setInvoiceNum(0);
		document.setDrAmount(BigDecimal.ZERO);
		document.setCrAmount(BigDecimal.ZERO);
		document.setCreateTime(now);
		document.setUpdateTime(now);

		AccountBook b_110101 = this.accountBookService.safeGet(relative, this.accountService.get("110101"));
		AccountBook b_2201 = this.accountBookService.safeGet(relative, this.accountService.get("2201"));
		AccountBook b_1201 = this.accountBookService.safeGet(relative, this.accountService.get("1201"));

		BigDecimal wipe = b_1201.getBalance().compareTo(amount) > 0 ? amount : b_1201.getBalance();
		BigDecimal incr = amount.subtract(wipe);

		List<DocumentEntry> entries = new ArrayList<DocumentEntry>();

		if (incr.compareTo(BigDecimal.ZERO) > 0) {
			// 借 交易性金融资产 投资标的 110101
			DocumentEntry e1_110101 = new DocumentEntry();
			e1_110101.setOid(StringUtil.uuid());
			e1_110101.setRelative(relative);
			e1_110101.setTicket(ticket);
			e1_110101.setDocument(document);
			e1_110101.setBook(b_110101);
			e1_110101.setDigest(null != maps && maps.containsKey("0060_0001_110101_01") ? maps.get("0060_0001_110101_01").getDigest() : "");
			e1_110101.setDrAmount(incr);
			e1_110101.setCrAmount(BigDecimal.ZERO);
			e1_110101.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(incr));
			b_110101 = this.accountBookService.incrCredit(b_110101, incr);
			seq++;
			entries.add(e1_110101);

			// 贷 未分配收益 2201
			DocumentEntry e2_2201 = new DocumentEntry();
			e2_2201.setOid(StringUtil.uuid());
			e2_2201.setRelative(relative);
			e2_2201.setTicket(ticket);
			e2_2201.setDocument(document);
			e2_2201.setBook(b_2201);
			e2_2201.setDigest(null != maps && maps.containsKey("0060_0001_2201_02") ? maps.get("0060_0001_2201_02").getDigest() : "");
			e2_2201.setDrAmount(BigDecimal.ZERO);
			e2_2201.setCrAmount(incr);
			e2_2201.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(incr));
			b_2201 = this.accountBookService.incrCredit(b_2201, incr);
			seq++;
			entries.add(e2_2201);
		}

		// 应收投资收益多头寸
		if (wipe.compareTo(BigDecimal.ZERO) > 0) {
			// 借 交易性金融资产 投资标的 110101
			DocumentEntry e3_110101 = new DocumentEntry();
			e3_110101.setOid(StringUtil.uuid());
			e3_110101.setRelative(relative);
			e3_110101.setTicket(ticket);
			e3_110101.setDocument(document);
			e3_110101.setBook(b_110101);
			e3_110101.setDigest(null != maps && maps.containsKey("0060_0001_110101_03") ? maps.get("0060_0001_110101_03").getDigest() : "");
			e3_110101.setDrAmount(wipe);
			e3_110101.setCrAmount(BigDecimal.ZERO);
			e3_110101.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(wipe));
			b_110101 = this.accountBookService.incrCredit(b_110101, wipe);
			seq++;
			entries.add(e3_110101);

			// 贷 应收投资收益 1201
			DocumentEntry e4_1201 = new DocumentEntry();
			e4_1201.setOid(StringUtil.uuid());
			e4_1201.setRelative(relative);
			e4_1201.setTicket(ticket);
			e4_1201.setDocument(document);
			e4_1201.setBook(b_1201);
			e4_1201.setDigest(null != maps && maps.containsKey("0060_0001_1201_04") ? maps.get("0060_0001_1201_04").getDigest() : "");
			e4_1201.setDrAmount(BigDecimal.ZERO);
			e4_1201.setCrAmount(wipe);
			e4_1201.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(wipe));
			b_1201 = this.accountBookService.decrCredit(b_1201, wipe);
			seq++;
			entries.add(e4_1201);
		}

		document = this.documentDao.save(document);
		for (DocumentEntry e : entries) {
			this.documentEntryService.save(e);
		}

		return document;
	}

	// 投资标的资产减值
	@Transactional
	public Document targetDecrement(String relative, String ticket, BigDecimal amount) {

		DocTemplate template = this.docTemplateService.safeGet("0060_GZJZ_0002_TARGETJZ");
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
		document.setType(Document.TYPE_ESTIMATE_CORRECT_TARGET);
		document.setRelative(relative);
		document.setTicket(ticket);
		document.setAcctSn(this.documentSnService.genSn(date));
		document.setAcctDate(date);
		document.setInvoiceNum(0);
		document.setDrAmount(BigDecimal.ZERO);
		document.setCrAmount(BigDecimal.ZERO);
		document.setCreateTime(now);
		document.setUpdateTime(now);

		AccountBook b_110101 = this.accountBookService.safeGet(relative, this.accountService.get("110101"));
		AccountBook b_2201 = this.accountBookService.safeGet(relative, this.accountService.get("2201"));
		AccountBook b_1201 = this.accountBookService.safeGet(relative, this.accountService.get("1201"));

		BigDecimal decr = b_2201.getBalance().compareTo(amount) > 0 ? amount : b_2201.getBalance();
		BigDecimal wipe = amount.subtract(decr);

		List<DocumentEntry> entries = new ArrayList<DocumentEntry>();

		if (decr.compareTo(BigDecimal.ZERO) > 0) {
			// 借 未分配收益 2201
			DocumentEntry e1_2201 = new DocumentEntry();
			e1_2201.setOid(StringUtil.uuid());
			e1_2201.setRelative(relative);
			e1_2201.setTicket(ticket);
			e1_2201.setDocument(document);
			e1_2201.setBook(b_2201);
			e1_2201.setDigest(null != maps && maps.containsKey("0060_0002_2201_01") ? maps.get("0060_0002_2201_01").getDigest() : "");
			e1_2201.setDrAmount(decr);
			e1_2201.setCrAmount(BigDecimal.ZERO);
			e1_2201.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(decr));
			b_2201 = this.accountBookService.decrCredit(b_2201, decr);
			seq++;
			entries.add(e1_2201);

			// 贷 交易性金融资产 投资标的 110101
			DocumentEntry e2_110101 = new DocumentEntry();
			e2_110101.setOid(StringUtil.uuid());
			e2_110101.setRelative(relative);
			e2_110101.setTicket(ticket);
			e2_110101.setDocument(document);
			e2_110101.setBook(b_110101);
			e2_110101.setDigest(null != maps && maps.containsKey("0060_0002_110101_02") ? maps.get("0060_0002_110101_02").getDigest() : "");
			e2_110101.setDrAmount(BigDecimal.ZERO);
			e2_110101.setCrAmount(decr);
			e2_110101.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(decr));
			b_110101 = this.accountBookService.decrCredit(b_110101, decr);
			seq++;
			entries.add(e2_110101);
		}

		// 未分配收益缺头寸
		if (wipe.compareTo(BigDecimal.ZERO) > 0) {
			// 借 应收投资收益 1201
			DocumentEntry e3_1201 = new DocumentEntry();
			e3_1201.setOid(StringUtil.uuid());
			e3_1201.setRelative(relative);
			e3_1201.setTicket(ticket);
			e3_1201.setDocument(document);
			e3_1201.setBook(b_1201);
			e3_1201.setDigest(null != maps && maps.containsKey("0060_0002_1201_03") ? maps.get("0060_0002_1201_03").getDigest() : "");
			e3_1201.setDrAmount(wipe);
			e3_1201.setCrAmount(BigDecimal.ZERO);
			e3_1201.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(wipe));
			b_1201 = this.accountBookService.incrCredit(b_1201, wipe);
			seq++;
			entries.add(e3_1201);

			// 贷 交易性金融资产 投资标的 110101
			DocumentEntry e4_110101 = new DocumentEntry();
			e4_110101.setOid(StringUtil.uuid());
			e4_110101.setRelative(relative);
			e4_110101.setTicket(ticket);
			e4_110101.setDocument(document);
			e4_110101.setBook(b_110101);
			e4_110101.setDigest(null != maps && maps.containsKey("0060_0002_110101_04") ? maps.get("0060_0002_110101_04").getDigest() : "");
			e4_110101.setDrAmount(BigDecimal.ZERO);
			e4_110101.setCrAmount(wipe);
			e4_110101.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(wipe));
			b_110101 = this.accountBookService.decrCredit(b_110101, wipe);
			seq++;
			entries.add(e4_110101);
		}

		document = this.documentDao.save(document);
		for (DocumentEntry e : entries) {
			this.documentEntryService.save(e);
		}

		return document;
	}

	// 现金管理工具增值
	@Transactional
	public Document cashtoolIncrement(String relative, String ticket, BigDecimal amount) {

		DocTemplate template = this.docTemplateService.safeGet("0060_GZJZ_0003_CASHTOOLZZ");
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
		document.setType(Document.TYPE_ESTIMATE_CORRECT_CASHTOOL);
		document.setRelative(relative);
		document.setTicket(ticket);
		document.setAcctSn(this.documentSnService.genSn(date));
		document.setAcctDate(date);
		document.setInvoiceNum(0);
		document.setDrAmount(BigDecimal.ZERO);
		document.setCrAmount(BigDecimal.ZERO);
		document.setCreateTime(now);
		document.setUpdateTime(now);

		AccountBook b_110151 = this.accountBookService.safeGet(relative, this.accountService.get("110151"));
		AccountBook b_2201 = this.accountBookService.safeGet(relative, this.accountService.get("2201"));
		AccountBook b_1201 = this.accountBookService.safeGet(relative, this.accountService.get("1201"));

		BigDecimal wipe = b_1201.getBalance().compareTo(amount) > 0 ? amount : b_1201.getBalance();
		BigDecimal incr = amount.subtract(wipe);

		List<DocumentEntry> entries = new ArrayList<DocumentEntry>();

		if (incr.compareTo(BigDecimal.ZERO) > 0) {
			// 借 交易性金融资产 现金管理工具 110151
			DocumentEntry e1_110151 = new DocumentEntry();
			e1_110151.setOid(StringUtil.uuid());
			e1_110151.setRelative(relative);
			e1_110151.setTicket(ticket);
			e1_110151.setDocument(document);
			e1_110151.setBook(b_110151);
			e1_110151.setDigest(null != maps && maps.containsKey("0060_0003_110151_01") ? maps.get("0060_0003_110151_01").getDigest() : "");
			e1_110151.setDrAmount(incr);
			e1_110151.setCrAmount(BigDecimal.ZERO);
			e1_110151.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(incr));
			b_110151 = this.accountBookService.incrCredit(b_110151, incr);
			seq++;
			entries.add(e1_110151);

			// 贷 未分配收益 2201
			DocumentEntry e2_2201 = new DocumentEntry();
			e2_2201.setOid(StringUtil.uuid());
			e2_2201.setRelative(relative);
			e2_2201.setTicket(ticket);
			e2_2201.setDocument(document);
			e2_2201.setBook(b_2201);
			e2_2201.setDigest(null != maps && maps.containsKey("0060_0003_2201_02") ? maps.get("0060_0003_2201_02").getDigest() : "");
			e2_2201.setDrAmount(BigDecimal.ZERO);
			e2_2201.setCrAmount(incr);
			e2_2201.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(incr));
			b_2201 = this.accountBookService.incrCredit(b_2201, incr);
			seq++;
			entries.add(e2_2201);
		}

		// 应收投资收益多头寸
		if (wipe.compareTo(BigDecimal.ZERO) > 0) {
			// 借 交易性金融资产 现金管理工具 110151
			DocumentEntry e3_110151 = new DocumentEntry();
			e3_110151.setOid(StringUtil.uuid());
			e3_110151.setRelative(relative);
			e3_110151.setTicket(ticket);
			e3_110151.setDocument(document);
			e3_110151.setBook(b_110151);
			e3_110151.setDigest(null != maps && maps.containsKey("0060_0003_110151_03") ? maps.get("0060_0003_110151_03").getDigest() : "");
			e3_110151.setDrAmount(wipe);
			e3_110151.setCrAmount(BigDecimal.ZERO);
			e3_110151.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(wipe));
			b_110151 = this.accountBookService.incrCredit(b_110151, wipe);
			seq++;
			entries.add(e3_110151);

			// 贷 应收投资收益 1201
			DocumentEntry e4_1201 = new DocumentEntry();
			e4_1201.setOid(StringUtil.uuid());
			e4_1201.setRelative(relative);
			e4_1201.setTicket(ticket);
			e4_1201.setDocument(document);
			e4_1201.setBook(b_1201);
			e4_1201.setDigest(null != maps && maps.containsKey("0060_0003_1201_04") ? maps.get("0060_0003_1201_04").getDigest() : "");
			e4_1201.setDrAmount(BigDecimal.ZERO);
			e4_1201.setCrAmount(wipe);
			e4_1201.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(wipe));
			b_1201 = this.accountBookService.decrCredit(b_1201, wipe);
			seq++;
			entries.add(e4_1201);
		}

		document = this.documentDao.save(document);
		for (DocumentEntry e : entries) {
			this.documentEntryService.save(e);
		}

		return document;
	}

	// 现金管理工具减值
	@Transactional
	public Document cashtoolDecrement(String relative, String ticket, BigDecimal amount) {

		DocTemplate template = this.docTemplateService.safeGet("0060_GZJZ_0004_CASHTOOLJZ");
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
		document.setType(Document.TYPE_ESTIMATE_CORRECT_CASHTOOL);
		document.setRelative(relative);
		document.setTicket(ticket);
		document.setAcctSn(this.documentSnService.genSn(date));
		document.setAcctDate(date);
		document.setInvoiceNum(0);
		document.setDrAmount(BigDecimal.ZERO);
		document.setCrAmount(BigDecimal.ZERO);
		document.setCreateTime(now);
		document.setUpdateTime(now);

		AccountBook b_110151 = this.accountBookService.safeGet(relative, this.accountService.get("110151"));
		AccountBook b_2201 = this.accountBookService.safeGet(relative, this.accountService.get("2201"));
		AccountBook b_1201 = this.accountBookService.safeGet(relative, this.accountService.get("1201"));

		BigDecimal decr = b_2201.getBalance().compareTo(amount) > 0 ? amount : b_2201.getBalance();
		BigDecimal wipe = amount.subtract(decr);

		List<DocumentEntry> entries = new ArrayList<DocumentEntry>();

		if (decr.compareTo(BigDecimal.ZERO) > 0) {
			// 借 未分配收益 2201
			DocumentEntry e1_2201 = new DocumentEntry();
			e1_2201.setOid(StringUtil.uuid());
			e1_2201.setRelative(relative);
			e1_2201.setTicket(ticket);
			e1_2201.setDocument(document);
			e1_2201.setBook(b_2201);
			e1_2201.setDigest(null != maps && maps.containsKey("0060_0004_2201_01") ? maps.get("0060_0004_2201_01").getDigest() : "");
			e1_2201.setDrAmount(decr);
			e1_2201.setCrAmount(BigDecimal.ZERO);
			e1_2201.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(decr));
			b_2201 = this.accountBookService.decrCredit(b_2201, decr);
			seq++;
			entries.add(e1_2201);

			// 贷 交易性金融资产 现金管理工具 110151
			DocumentEntry e2_110151 = new DocumentEntry();
			e2_110151.setOid(StringUtil.uuid());
			e2_110151.setRelative(relative);
			e2_110151.setTicket(ticket);
			e2_110151.setDocument(document);
			e2_110151.setBook(b_110151);
			e2_110151.setDigest(null != maps && maps.containsKey("0060_0004_110151_02") ? maps.get("0060_0004_110151_02").getDigest() : "");
			e2_110151.setDrAmount(BigDecimal.ZERO);
			e2_110151.setCrAmount(decr);
			e2_110151.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(decr));
			b_110151 = this.accountBookService.decrCredit(b_110151, decr);
			seq++;
			entries.add(e2_110151);
		}

		// 未分配收益缺头寸
		if (wipe.compareTo(BigDecimal.ZERO) > 0) {
			// 借 应收投资收益 1201
			DocumentEntry e3_1201 = new DocumentEntry();
			e3_1201.setOid(StringUtil.uuid());
			e3_1201.setRelative(relative);
			e3_1201.setTicket(ticket);
			e3_1201.setDocument(document);
			e3_1201.setBook(b_1201);
			e3_1201.setDigest(null != maps && maps.containsKey("0060_0004_1201_03") ? maps.get("0060_0004_1201_03").getDigest() : "");
			e3_1201.setDrAmount(wipe);
			e3_1201.setCrAmount(BigDecimal.ZERO);
			e3_1201.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(wipe));
			b_1201 = this.accountBookService.incrCredit(b_1201, wipe);
			seq++;
			entries.add(e3_1201);

			// 贷 交易性金融资产 现金管理工具 110151
			DocumentEntry e4_110151 = new DocumentEntry();
			e4_110151.setOid(StringUtil.uuid());
			e4_110151.setRelative(relative);
			e4_110151.setTicket(ticket);
			e4_110151.setDocument(document);
			e4_110151.setBook(b_110151);
			e4_110151.setDigest(null != maps && maps.containsKey("0060_0004_110151_04") ? maps.get("0060_0004_110151_04").getDigest() : "");
			e4_110151.setDrAmount(BigDecimal.ZERO);
			e4_110151.setCrAmount(wipe);
			e4_110151.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(wipe));
			b_110151 = this.accountBookService.decrCredit(b_110151, wipe);
			seq++;
			entries.add(e4_110151);
		}

		document = this.documentDao.save(document);
		for (DocumentEntry e : entries) {
			this.documentEntryService.save(e);
		}

		return document;
	}

}

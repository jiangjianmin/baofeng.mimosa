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

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 凭证业务 - 资产池收益确认
 * 
 * @author Arthur
 */
@Service
public class IncomeDocumentService {

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

	public enum IncomeType {
		DEPOSIT, // 银行存款
		TARGET, // 投资标的
		CASHTOOL // 现金管理类工具
	}

	/**
	 * 收益确认
	 */
	@Transactional
	public List<Document> incomeConfirm(String relative, List<Income> incomes) {
		// 这里要接收所有收益确认, 并最终进行应收投资收益处理

		List<Document> docs = new ArrayList<Document>();

		BigDecimal sigma = BigDecimal.ZERO;

		for (Income income : incomes) {
			if (income.getType().equals(IncomeType.DEPOSIT)) {
				docs.add(this.depositConfirm(relative, income.getTicket(), income.getIncome()));
				sigma = sigma.add(income.getIncome());
			}
			if (income.getType().equals(IncomeType.TARGET)) {
				docs.add(this.targetConfirm(relative, income.getTicket(), income.getIncome()));
				sigma = sigma.add(income.getIncome());
			}
			if (income.getType().equals(IncomeType.CASHTOOL)) {
				docs.add(this.cashtoolConfirm(relative, income.getTicket(), income.getIncome()));
				sigma = sigma.add(income.getIncome());
			}
		}

		AccountBook b_1201 = this.accountBookService.safeGet(relative, this.accountService.get("1201"));

		// 有应收投资收益头寸, 需要勾销
		if (b_1201.getBalance().compareTo(BigDecimal.ZERO) > 0) {
			AccountBook b_2201 = this.accountBookService.safeGet(relative, this.accountService.get("2201"));

			DocTemplate template = this.docTemplateService.safeGet("0050_SYQR_0004_YSTZSYGX");
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
			document.setType(Document.TYPE_INCOME_RECEIVE);
			document.setRelative(relative);
			document.setTicket("");
			document.setAcctSn(this.documentSnService.genSn(date));
			document.setAcctDate(date);
			document.setInvoiceNum(0);
			document.setDrAmount(BigDecimal.ZERO);
			document.setCrAmount(BigDecimal.ZERO);
			document.setCreateTime(now);
			document.setUpdateTime(now);

			BigDecimal wipe = sigma.compareTo(b_1201.getBalance()) > 0 ? b_1201.getBalance() : sigma;

			// 借 未分配收益
			DocumentEntry e1_2201 = new DocumentEntry();
			e1_2201.setOid(StringUtil.uuid());
			e1_2201.setRelative(relative);
			e1_2201.setTicket("");
			e1_2201.setDocument(document);
			e1_2201.setBook(b_2201);
			e1_2201.setDigest(null != maps && maps.containsKey("0050_0004_2201_01") ? maps.get("0050_0004_2201_01").getDigest() : "");
			e1_2201.setDrAmount(wipe);
			e1_2201.setCrAmount(BigDecimal.ZERO);
			e1_2201.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(wipe));
			b_2201 = this.accountBookService.decrCredit(b_2201, wipe);
			seq++;

			// 贷 应收投资收益
			DocumentEntry e1_1201 = new DocumentEntry();
			e1_1201.setOid(StringUtil.uuid());
			e1_1201.setRelative(relative);
			e1_1201.setTicket("");
			e1_1201.setDocument(document);
			e1_1201.setBook(b_1201);
			e1_1201.setDigest(null != maps && maps.containsKey("0050_0004_1201_02") ? maps.get("0050_0004_1201_02").getDigest() : "");
			e1_1201.setDrAmount(BigDecimal.ZERO);
			e1_1201.setCrAmount(wipe);
			e1_1201.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(wipe));
			b_1201 = this.accountBookService.decrCredit(b_1201, wipe);
			seq++;

			document = this.documentDao.save(document);
			this.documentEntryService.save(e1_2201);
			this.documentEntryService.save(e1_1201);
			docs.add(document);
		}

		return docs;
	}

	@Transactional
	private Document depositConfirm(String relative, String ticket, BigDecimal income) {
		DocTemplate template = this.docTemplateService.safeGet("0050_SYQR_0003_YHCKLXQR");
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
		document.setType(Document.TYPE_INCOME_DEPOSIT);
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
		AccountBook b_2201 = this.accountBookService.safeGet(relative, this.accountService.get("2201"));

		// 借 银行存款
		DocumentEntry e1_1001 = new DocumentEntry();
		e1_1001.setOid(StringUtil.uuid());
		e1_1001.setRelative(relative);
		e1_1001.setTicket(ticket);
		e1_1001.setDocument(document);
		e1_1001.setBook(b_1001);
		e1_1001.setDigest(null != maps && maps.containsKey("0050_0003_1001_01") ? maps.get("0050_0003_1001_01").getDigest() : "");
		e1_1001.setDrAmount(income);
		e1_1001.setCrAmount(BigDecimal.ZERO);
		e1_1001.setSeq(seq);
		document.setDrAmount(document.getDrAmount().add(income));
		b_1001 = this.accountBookService.incrCredit(b_1001, income);
		seq++;

		// 贷 未分配收益
		DocumentEntry e2_2201 = new DocumentEntry();
		e2_2201.setOid(StringUtil.uuid());
		e2_2201.setRelative(relative);
		e2_2201.setTicket(ticket);
		e2_2201.setDocument(document);
		e2_2201.setBook(b_2201);
		e2_2201.setDigest(null != maps && maps.containsKey("0050_0003_2201_02") ? maps.get("0050_0003_2201_02").getDigest() : "");
		e2_2201.setDrAmount(BigDecimal.ZERO);
		e2_2201.setCrAmount(income);
		e2_2201.setSeq(seq);
		document.setCrAmount(document.getCrAmount().add(income));
		b_2201 = this.accountBookService.incrCredit(b_2201, income);
		seq++;

		document = this.documentDao.save(document);
		this.documentEntryService.save(e1_1001);
		this.documentEntryService.save(e2_2201);

		return document;
	}

	@Transactional
	private Document targetConfirm(String relative, String ticket, BigDecimal income) {
		DocTemplate template = this.docTemplateService.safeGet("0050_SYQR_0001_XTZCSYQR");
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
		document.setType(Document.TYPE_INCOME_TARGET);
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

		// 借 交易性金融资产:信托资产
		DocumentEntry e1_110101 = new DocumentEntry();
		e1_110101.setOid(StringUtil.uuid());
		e1_110101.setRelative(relative);
		e1_110101.setTicket(ticket);
		e1_110101.setDocument(document);
		e1_110101.setBook(b_110101);
		e1_110101.setDigest(null != maps && maps.containsKey("0050_0001_110101_01") ? maps.get("0050_0001_110101_01").getDigest() : "");
		e1_110101.setDrAmount(income);
		e1_110101.setCrAmount(BigDecimal.ZERO);
		e1_110101.setSeq(seq);
		document.setDrAmount(document.getDrAmount().add(income));
		b_110101 = this.accountBookService.incrCredit(b_110101, income);
		seq++;

		// 贷 未分配收益
		DocumentEntry e2_2201 = new DocumentEntry();
		e2_2201.setOid(StringUtil.uuid());
		e2_2201.setRelative(relative);
		e2_2201.setTicket(ticket);
		e2_2201.setDocument(document);
		e2_2201.setBook(b_2201);
		e2_2201.setDigest(null != maps && maps.containsKey("0050_0001_2201_02") ? maps.get("0050_0001_2201_02").getDigest() : "");
		e2_2201.setDrAmount(BigDecimal.ZERO);
		e2_2201.setCrAmount(income);
		e2_2201.setSeq(seq);
		document.setCrAmount(document.getCrAmount().add(income));
		b_2201 = this.accountBookService.incrCredit(b_2201, income);
		seq++;

		document = this.documentDao.save(document);
		this.documentEntryService.save(e1_110101);
		this.documentEntryService.save(e2_2201);

		return document;
	}

	@Transactional
	private Document cashtoolConfirm(String relative, String ticket, BigDecimal income) {
		DocTemplate template = this.docTemplateService.safeGet("0050_SYQR_0002_HBJJSYQR");
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
		document.setType(Document.TYPE_INCOME_CASHTOOL);
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

		// 借 交易性金融资产:信托资产
		DocumentEntry e1_110151 = new DocumentEntry();
		e1_110151.setOid(StringUtil.uuid());
		e1_110151.setRelative(relative);
		e1_110151.setTicket(ticket);
		e1_110151.setDocument(document);
		e1_110151.setBook(b_110151);
		e1_110151.setDigest(null != maps && maps.containsKey("0050_0002_110151_01") ? maps.get("0050_0002_110151_01").getDigest() : "");
		e1_110151.setDrAmount(income);
		e1_110151.setCrAmount(BigDecimal.ZERO);
		e1_110151.setSeq(seq);
		document.setDrAmount(document.getDrAmount().add(income));
		b_110151 = this.accountBookService.incrCredit(b_110151, income);
		seq++;

		// 贷 未分配收益
		DocumentEntry e2_2201 = new DocumentEntry();
		e2_2201.setOid(StringUtil.uuid());
		e2_2201.setRelative(relative);
		e2_2201.setTicket(ticket);
		e2_2201.setDocument(document);
		e2_2201.setBook(b_2201);
		e2_2201.setDigest(null != maps && maps.containsKey("0050_0002_2201_02") ? maps.get("0050_0002_2201_02").getDigest() : "");
		e2_2201.setDrAmount(BigDecimal.ZERO);
		e2_2201.setCrAmount(income);
		e2_2201.setSeq(seq);
		document.setCrAmount(document.getCrAmount().add(income));
		b_2201 = this.accountBookService.incrCredit(b_2201, income);
		seq++;

		document = this.documentDao.save(document);
		this.documentEntryService.save(e1_110151);
		this.documentEntryService.save(e2_2201);

		return document;
	}

	@Data
	@AllArgsConstructor
	public static class Income {
		private String ticket;
		private BigDecimal income;
		private IncomeType type;
	}

}

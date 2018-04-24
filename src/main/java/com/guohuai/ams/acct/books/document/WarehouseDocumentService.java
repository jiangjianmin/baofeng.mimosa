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
 * 凭证业务 - 资产池调仓
 * 
 * @author Arthur
 */
@Service
public class WarehouseDocumentService {

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

	/**
	 * 现金管理工具 申购
	 */
	@Transactional
	public Document cashtoolPurchase(String relative, String ticket, BigDecimal amount) {
		DocTemplate template = this.docTemplateService.safeGet("0030_NBTC_0001_HBJJSG");
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
		document.setType(Document.TYPE_CASHTOOL_WAREHOUSE);
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
		AccountBook b_1001 = this.accountBookService.safeGet(relative, this.accountService.get("1001"));

		// 借 交易性金融资产_货币基金
		DocumentEntry e1_110151 = new DocumentEntry();
		e1_110151.setOid(StringUtil.uuid());
		e1_110151.setRelative(relative);
		e1_110151.setTicket(ticket);
		e1_110151.setDocument(document);
		e1_110151.setBook(b_110151);
		e1_110151.setDigest(null != maps && maps.containsKey("0030_0001_110151_01") ? maps.get("0030_0001_110151_01").getDigest() : "");
		e1_110151.setDrAmount(amount);
		e1_110151.setCrAmount(BigDecimal.ZERO);
		e1_110151.setSeq(seq);
		document.setDrAmount(document.getDrAmount().add(amount));
		b_110151 = this.accountBookService.incrCredit(b_110151, amount);
		seq++;

		// 贷 银行存款
		DocumentEntry e2_1001 = new DocumentEntry();
		e2_1001.setOid(StringUtil.uuid());
		e2_1001.setRelative(relative);
		e2_1001.setTicket(ticket);
		e2_1001.setDocument(document);
		e2_1001.setBook(b_1001);
		e2_1001.setDigest(null != maps && maps.containsKey("0030_0001_1001_02") ? maps.get("0030_0001_1001_02").getDigest() : "");
		e2_1001.setDrAmount(BigDecimal.ZERO);
		e2_1001.setCrAmount(amount);
		e2_1001.setSeq(seq);
		document.setCrAmount(document.getCrAmount().add(amount));
		b_1001 = this.accountBookService.decrCredit(b_1001, amount);
		seq++;

		document = this.documentDao.save(document);
		this.documentEntryService.save(e1_110151);
		this.documentEntryService.save(e2_1001);

		return document;

	}

	/**
	 * 现金管理工具 赎回
	 */
	@Transactional
	public Document cashtoolRedemption(String relative, String ticket, BigDecimal amount) {

		DocTemplate template = this.docTemplateService.safeGet("0030_NBTC_0002_HBJJSH");
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
		document.setType(Document.TYPE_CASHTOOL_WAREHOUSE);
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
		AccountBook b_110151 = this.accountBookService.safeGet(relative, this.accountService.get("110151"));

		// 借 银行存款
		DocumentEntry e1_1001 = new DocumentEntry();
		e1_1001.setOid(StringUtil.uuid());
		e1_1001.setRelative(relative);
		e1_1001.setTicket(ticket);
		e1_1001.setDocument(document);
		e1_1001.setBook(b_1001);
		e1_1001.setDigest(null != maps && maps.containsKey("0030_0002_1001_01") ? maps.get("0030_0002_1001_01").getDigest() : "");
		e1_1001.setDrAmount(amount);
		e1_1001.setCrAmount(BigDecimal.ZERO);
		e1_1001.setSeq(seq);
		document.setDrAmount(document.getDrAmount().add(amount));
		b_1001 = this.accountBookService.incrCredit(b_1001, amount);
		seq++;

		// 贷 交易性金融资产_货币基金
		DocumentEntry e2_110151 = new DocumentEntry();
		e2_110151.setOid(StringUtil.uuid());
		e2_110151.setRelative(relative);
		e2_110151.setTicket(ticket);
		e2_110151.setDocument(document);
		e2_110151.setBook(b_110151);
		e2_110151.setDigest(null != maps && maps.containsKey("0030_0002_110151_02") ? maps.get("0030_0002_110151_02").getDigest() : "");
		e2_110151.setDrAmount(BigDecimal.ZERO);
		e2_110151.setCrAmount(amount);
		e2_110151.setSeq(seq);
		document.setCrAmount(document.getCrAmount().add(amount));
		b_110151 = this.accountBookService.decrCredit(b_110151, amount);
		seq++;

		document = this.documentDao.save(document);
		this.documentEntryService.save(e1_1001);
		this.documentEntryService.save(e2_110151);

		return document;

	}

	/**
	 * 信托计划 申购
	 */
	@Transactional
	public Document targetPurchase(String relative, String ticket, BigDecimal amount) {

		DocTemplate template = this.docTemplateService.safeGet("0030_NBTC_0003_XTZCSG");
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
		document.setType(Document.TYPE_TARGET_PURCHASE);
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
		AccountBook b_1001 = this.accountBookService.safeGet(relative, this.accountService.get("1001"));

		// 借 交易性金融资产_类别X
		DocumentEntry e1_110101 = new DocumentEntry();
		e1_110101.setOid(StringUtil.uuid());
		e1_110101.setRelative(relative);
		e1_110101.setTicket(ticket);
		e1_110101.setDocument(document);
		e1_110101.setBook(b_110101);
		e1_110101.setDigest(null != maps && maps.containsKey("0030_0003_110101_01") ? maps.get("0030_0003_110101_01").getDigest() : "");
		e1_110101.setDrAmount(amount);
		e1_110101.setCrAmount(BigDecimal.ZERO);
		e1_110101.setSeq(seq);
		document.setDrAmount(document.getDrAmount().add(amount));
		b_110101 = this.accountBookService.incrCredit(b_110101, amount);
		seq++;

		// 贷 银行存款
		DocumentEntry e2_1001 = new DocumentEntry();
		e2_1001.setOid(StringUtil.uuid());
		e2_1001.setRelative(relative);
		e2_1001.setTicket(ticket);
		e2_1001.setDocument(document);
		e2_1001.setBook(b_1001);
		e2_1001.setDigest(null != maps && maps.containsKey("0030_0003_1001_02") ? maps.get("0030_0003_1001_02").getDigest() : "");
		e2_1001.setDrAmount(BigDecimal.ZERO);
		e2_1001.setCrAmount(amount);
		e2_1001.setSeq(seq);
		document.setCrAmount(document.getCrAmount().add(amount));
		b_1001 = this.accountBookService.decrCredit(b_1001, amount);
		seq++;

		document = this.documentDao.save(document);
		this.documentEntryService.save(e1_110101);
		this.documentEntryService.save(e2_1001);

		return document;

	}

	/**
	 * 信托计划 本息兑付
	 * 申购本金 = 申购本金 - 转出本金
	 * 利息收益 = 页面录入
	 * 本金 = 页面录入
	 * 确认收益 = 定时计算
	 * principal 申购本金
	 * returns 返还本金(仅利息收益事件, 该参数传0)
	 * income 确认收益
	 * realized 实现收益
	 */
	public Document targetRedemption(String relative, String ticket, BigDecimal principal, BigDecimal returns, BigDecimal income, BigDecimal realized) {

		DocTemplate template = this.docTemplateService.safeGet("0030_NBTC_0004_XTZCBXDF");
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
		document.setType(Document.TYPE_TARGET_REDEMPTION);
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
		AccountBook b_1001 = this.accountBookService.safeGet(relative, this.accountService.get("1001"));
		AccountBook b_1201 = this.accountBookService.safeGet(relative, this.accountService.get("1201"));

		List<DocumentEntry> entries = new ArrayList<DocumentEntry>();

		// 有实现收益
		if (realized.compareTo(BigDecimal.ZERO) == 1) {

			if (realized.compareTo(income) == 1) {
				// 实现收益 > 确认收益

				BigDecimal wipe = realized.subtract(income);

				// if 应收投资收益 > 0
				if (b_1201.getBalance().compareTo(BigDecimal.ZERO) > 0) {

					// 应收投资收益
					BigDecimal ar = b_1201.getBalance().compareTo(wipe) > 0 ? wipe : b_1201.getBalance();
					// 计算剩余收益
					wipe = wipe.subtract(ar);

					// 借 交易性金融资产 110101
					DocumentEntry e1_110101 = new DocumentEntry();
					e1_110101.setOid(StringUtil.uuid());
					e1_110101.setRelative(relative);
					e1_110101.setTicket(ticket);
					e1_110101.setDocument(document);
					e1_110101.setBook(b_110101);
					e1_110101.setDigest(null != maps && maps.containsKey("0030_0004_110101_01") ? maps.get("0030_0004_110101_01").getDigest() : "");
					e1_110101.setDrAmount(ar);
					e1_110101.setCrAmount(BigDecimal.ZERO);
					e1_110101.setSeq(seq);
					document.setDrAmount(document.getDrAmount().add(ar));
					b_110101 = this.accountBookService.incrCredit(b_110101, income);
					seq++;
					entries.add(e1_110101);

					// 贷 应收投资收益 1201
					DocumentEntry e2_1201 = new DocumentEntry();
					e2_1201.setOid(StringUtil.uuid());
					e2_1201.setRelative(relative);
					e2_1201.setTicket(ticket);
					e2_1201.setDocument(document);
					e2_1201.setBook(b_1201);
					e2_1201.setDigest(null != maps && maps.containsKey("0030_0004_1201_02") ? maps.get("0030_0004_1201_02").getDigest() : "");
					e2_1201.setDrAmount(BigDecimal.ZERO);
					e2_1201.setCrAmount(ar);
					e2_1201.setSeq(seq);
					document.setCrAmount(document.getCrAmount().add(ar));
					b_1201 = this.accountBookService.decrCredit(b_2201, ar);
					seq++;
					entries.add(e2_1201);
				}

				// 收益仍有头寸
				if (wipe.compareTo(BigDecimal.ZERO) > 0) {
					// 借 交易性金融资产 110101
					DocumentEntry e3_110101 = new DocumentEntry();
					e3_110101.setOid(StringUtil.uuid());
					e3_110101.setRelative(relative);
					e3_110101.setTicket(ticket);
					e3_110101.setDocument(document);
					e3_110101.setBook(b_110101);
					e3_110101.setDigest(null != maps && maps.containsKey("0030_0004_110101_03") ? maps.get("0030_0004_110101_03").getDigest() : "");
					e3_110101.setDrAmount(wipe);
					e3_110101.setCrAmount(BigDecimal.ZERO);
					e3_110101.setSeq(seq);
					document.setDrAmount(document.getDrAmount().add(wipe));
					b_110101 = this.accountBookService.incrCredit(b_110101, wipe);
					seq++;
					entries.add(e3_110101);

					// 贷 未分配收益 2201
					DocumentEntry e4_2201 = new DocumentEntry();
					e4_2201.setOid(StringUtil.uuid());
					e4_2201.setRelative(relative);
					e4_2201.setTicket(ticket);
					e4_2201.setDocument(document);
					e4_2201.setBook(b_2201);
					e4_2201.setDigest(null != maps && maps.containsKey("0030_0004_2201_04") ? maps.get("0030_0004_2201_04").getDigest() : "");
					e4_2201.setDrAmount(BigDecimal.ZERO);
					e4_2201.setCrAmount(wipe);
					e4_2201.setSeq(seq);
					document.setCrAmount(document.getCrAmount().add(wipe));
					b_2201 = this.accountBookService.incrCredit(b_2201, wipe);
					seq++;
					entries.add(e4_2201);

					// 现金头寸也要调整 收益部分
					// 借 银行存款 1001
					DocumentEntry e5_1001 = new DocumentEntry();
					e5_1001.setOid(StringUtil.uuid());
					e5_1001.setRelative(relative);
					e5_1001.setTicket(ticket);
					e5_1001.setDocument(document);
					e5_1001.setBook(b_1001);
					e5_1001.setDigest(null != maps && maps.containsKey("0030_0004_1001_05") ? maps.get("0030_0004_1001_05").getDigest() : "");
					e5_1001.setDrAmount(wipe);
					e5_1001.setCrAmount(BigDecimal.ZERO);
					e5_1001.setSeq(seq);
					document.setDrAmount(document.getDrAmount().add(wipe));
					b_1001 = this.accountBookService.incrCredit(b_1001, wipe);
					seq++;
					entries.add(e5_1001);

					// 贷 交易性金融资产 110101
					DocumentEntry e6_110101 = new DocumentEntry();
					e6_110101.setOid(StringUtil.uuid());
					e6_110101.setRelative(relative);
					e6_110101.setTicket(ticket);
					e6_110101.setDocument(document);
					e6_110101.setBook(b_110101);
					e6_110101.setDigest(null != maps && maps.containsKey("0030_0004_110101_06") ? maps.get("0030_0004_110101_06").getDigest() : "");
					e6_110101.setDrAmount(BigDecimal.ZERO);
					e6_110101.setCrAmount(wipe);
					e6_110101.setSeq(seq);
					document.setCrAmount(document.getCrAmount().add(wipe));
					b_110101 = this.accountBookService.decrCredit(b_110101, wipe);
					seq++;
					entries.add(e6_110101);

				}

			} else if (realized.compareTo(income) == -1) {
				// 实现收益 < 确认收益

				BigDecimal wipe = income.subtract(realized);

				// 未分配收益 < 确认收益 - 实现收益
				// 需要记应收投资收益
				if (b_2201.getBalance().compareTo(wipe) < 0) {
					// 计算应收投资收益
					BigDecimal ar = wipe.subtract(b_2201.getBalance());
					// 计算剩余应付收益
					wipe = wipe.subtract(ar);

					// 借 应收投资收益 1201
					DocumentEntry e7_1201 = new DocumentEntry();
					e7_1201.setOid(StringUtil.uuid());
					e7_1201.setRelative(relative);
					e7_1201.setTicket(ticket);
					e7_1201.setDocument(document);
					e7_1201.setBook(b_1201);
					e7_1201.setDigest(null != maps && maps.containsKey("0030_0004_1201_07") ? maps.get("0030_0004_1201_07").getDigest() : "");
					e7_1201.setDrAmount(ar);
					e7_1201.setCrAmount(BigDecimal.ZERO);
					e7_1201.setSeq(seq);
					document.setDrAmount(document.getDrAmount().add(ar));
					b_1201 = this.accountBookService.incrCredit(b_1201, ar);
					seq++;
					entries.add(e7_1201);

					// 贷 交易性金融资产 110101
					DocumentEntry e8_110101 = new DocumentEntry();
					e8_110101.setOid(StringUtil.uuid());
					e8_110101.setRelative(relative);
					e8_110101.setTicket(ticket);
					e8_110101.setDocument(document);
					e8_110101.setBook(b_110101);
					e8_110101.setDigest(null != maps && maps.containsKey("0030_0004_110101_08") ? maps.get("0030_0004_110101_08").getDigest() : "");
					e8_110101.setDrAmount(BigDecimal.ZERO);
					e8_110101.setCrAmount(ar);
					e8_110101.setSeq(seq);
					document.setCrAmount(document.getCrAmount().add(ar));
					b_110101 = this.accountBookService.decrCredit(b_110101, ar);
					seq++;
					entries.add(e8_110101);
				}

				if (wipe.compareTo(BigDecimal.ZERO) > 0) {
					// 借 未分配收益 2201
					DocumentEntry e9_2201 = new DocumentEntry();
					e9_2201.setOid(StringUtil.uuid());
					e9_2201.setRelative(relative);
					e9_2201.setTicket(ticket);
					e9_2201.setDocument(document);
					e9_2201.setBook(b_2201);
					e9_2201.setDigest(null != maps && maps.containsKey("0030_0004_2201_09") ? maps.get("0030_0004_2201_09").getDigest() : "");
					e9_2201.setDrAmount(wipe);
					e9_2201.setCrAmount(BigDecimal.ZERO);
					e9_2201.setSeq(seq);
					document.setDrAmount(document.getDrAmount().add(wipe));
					b_2201 = this.accountBookService.decrCredit(b_2201, wipe);
					seq++;
					entries.add(e9_2201);

					// 贷 交易性金融资产 110101
					DocumentEntry e10_110101 = new DocumentEntry();
					e10_110101.setOid(StringUtil.uuid());
					e10_110101.setRelative(relative);
					e10_110101.setTicket(ticket);
					e10_110101.setDocument(document);
					e10_110101.setBook(b_110101);
					e10_110101.setDigest(null != maps && maps.containsKey("0030_0004_110101_10") ? maps.get("0030_0004_110101_10").getDigest() : "");
					e10_110101.setDrAmount(BigDecimal.ZERO);
					e10_110101.setCrAmount(wipe);
					e10_110101.setSeq(seq);
					document.setCrAmount(document.getCrAmount().add(wipe));
					b_110101 = this.accountBookService.decrCredit(b_110101, wipe);
					seq++;
					entries.add(e10_110101);
				}

			} else {
				// 没有收益, 不需要记录会计分录
			}

		}

		// 有返还本金
		if (returns.compareTo(BigDecimal.ZERO) == 1) {

			// 本金未如数返还, 记亏损部分
			if (returns.compareTo(principal) == -1) {
				BigDecimal wipe = principal.subtract(returns);
				// 借 应收投资收益 1201
				DocumentEntry e11_1201 = new DocumentEntry();
				e11_1201.setOid(StringUtil.uuid());
				e11_1201.setRelative(relative);
				e11_1201.setTicket(ticket);
				e11_1201.setDocument(document);
				e11_1201.setBook(b_1201);
				e11_1201.setDigest(null != maps && maps.containsKey("0030_0004_1201_11") ? maps.get("0030_0004_1201_11").getDigest() : "");
				e11_1201.setDrAmount(wipe);
				e11_1201.setCrAmount(BigDecimal.ZERO);
				e11_1201.setSeq(seq);
				document.setDrAmount(document.getDrAmount().add(wipe));
				b_1201 = this.accountBookService.incrCredit(b_1201, wipe);
				seq++;
				entries.add(e11_1201);

				// 贷 交易性金融资产 110101
				DocumentEntry e12_110101 = new DocumentEntry();
				e12_110101.setOid(StringUtil.uuid());
				e12_110101.setRelative(relative);
				e12_110101.setTicket(ticket);
				e12_110101.setDocument(document);
				e12_110101.setBook(b_110101);
				e12_110101.setDigest(null != maps && maps.containsKey("0030_0004_110101_12") ? maps.get("0030_0004_110101_12").getDigest() : "");
				e12_110101.setDrAmount(BigDecimal.ZERO);
				e12_110101.setCrAmount(wipe);
				e12_110101.setSeq(seq);
				document.setCrAmount(document.getCrAmount().add(wipe));
				b_110101 = this.accountBookService.decrCredit(b_110101, wipe);
				seq++;
				entries.add(e12_110101);
			}

			// 本金返还
			// 借 银行存款 1001
			DocumentEntry e13_1001 = new DocumentEntry();
			e13_1001.setOid(StringUtil.uuid());
			e13_1001.setRelative(relative);
			e13_1001.setTicket(ticket);
			e13_1001.setDocument(document);
			e13_1001.setBook(b_1001);
			e13_1001.setDigest(null != maps && maps.containsKey("0030_0004_1001_13") ? maps.get("0030_0004_1001_13").getDigest() : "");
			e13_1001.setDrAmount(returns);
			e13_1001.setCrAmount(BigDecimal.ZERO);
			e13_1001.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(returns));
			b_1001 = this.accountBookService.incrCredit(b_1001, returns);
			seq++;
			entries.add(e13_1001);

			// 贷 交易性金融资产 110101
			DocumentEntry e14_110101 = new DocumentEntry();
			e14_110101.setOid(StringUtil.uuid());
			e14_110101.setRelative(relative);
			e14_110101.setTicket(ticket);
			e14_110101.setDocument(document);
			e14_110101.setBook(b_110101);
			e14_110101.setDigest(null != maps && maps.containsKey("0030_0004_110101_14") ? maps.get("0030_0004_110101_14").getDigest() : "");
			e14_110101.setDrAmount(BigDecimal.ZERO);
			e14_110101.setCrAmount(returns);
			e14_110101.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(returns));
			b_110101 = this.accountBookService.decrCredit(b_110101, returns);
			seq++;
			entries.add(e14_110101);

		}

		document = this.documentDao.save(document);
		for (DocumentEntry e : entries) {
			this.documentEntryService.save(e);
		}

		return document;

	}

	/**
	 * 信托计划 溢价转让
	 * principal 本金
	 * estimate 估值
	 * revenue 交收额
	 */
	@Transactional
	public Document targetPremiumsTransOut(String relative, String ticket, BigDecimal principal, BigDecimal estimate, BigDecimal revenue) {

		DocTemplate template = this.docTemplateService.safeGet("0030_NBTC_0005_XTZCYJZR");
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
		document.setType(Document.TYPE_TARGET_TRANSFER);
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
		AccountBook b_1001 = this.accountBookService.safeGet(relative, this.accountService.get("1001"));
		AccountBook b_1201 = this.accountBookService.safeGet(relative, this.accountService.get("1201"));

		List<DocumentEntry> entries = new ArrayList<DocumentEntry>();

		// 有溢价, 调整未分配收益
		BigDecimal premiums = revenue.subtract(estimate);
		if (premiums.compareTo(BigDecimal.ZERO) > 0) {

			// 应收投资收益仍有头寸, 进行勾销冲账
			BigDecimal wipe = BigDecimal.ZERO;
			if (b_1201.getBalance().compareTo(BigDecimal.ZERO) > 0) {
				wipe = b_1201.getBalance().compareTo(premiums) > 0 ? premiums : b_1201.getBalance();
			}
			BigDecimal income = premiums.subtract(wipe);

			// 勾销之后仍有收益, 记未分配收益
			if (income.compareTo(BigDecimal.ZERO) > 0) {
				// 借 交易性金融资产_类别X
				DocumentEntry e1_110101 = new DocumentEntry();
				e1_110101.setOid(StringUtil.uuid());
				e1_110101.setRelative(relative);
				e1_110101.setTicket(ticket);
				e1_110101.setDocument(document);
				e1_110101.setBook(b_110101);
				e1_110101.setDigest(null != maps && maps.containsKey("0030_0005_110101_01") ? maps.get("0030_0005_110101_01").getDigest() : "");
				e1_110101.setDrAmount(income);
				e1_110101.setCrAmount(BigDecimal.ZERO);
				e1_110101.setSeq(seq);
				document.setDrAmount(document.getDrAmount().add(income));
				b_110101 = this.accountBookService.incrCredit(b_110101, income);
				seq++;
				entries.add(e1_110101);

				// 贷 未分配收益
				DocumentEntry e2_2201 = new DocumentEntry();
				e2_2201.setOid(StringUtil.uuid());
				e2_2201.setRelative(relative);
				e2_2201.setTicket(ticket);
				e2_2201.setDocument(document);
				e2_2201.setBook(b_2201);
				e2_2201.setDigest(null != maps && maps.containsKey("0030_0005_2201_02") ? maps.get("0030_0005_2201_02").getDigest() : "");
				e2_2201.setDrAmount(BigDecimal.ZERO);
				e2_2201.setCrAmount(income);
				e2_2201.setSeq(seq);
				document.setCrAmount(document.getCrAmount().add(income));
				b_2201 = this.accountBookService.incrCredit(b_2201, income);
				seq++;
				entries.add(e2_2201);
			}

			// 需要勾销应收投资收益头寸
			if (wipe.compareTo(BigDecimal.ZERO) > 0) {
				// 借 交易性金融资产_类别X
				DocumentEntry e3_110101 = new DocumentEntry();
				e3_110101.setOid(StringUtil.uuid());
				e3_110101.setRelative(relative);
				e3_110101.setTicket(ticket);
				e3_110101.setDocument(document);
				e3_110101.setBook(b_110101);
				e3_110101.setDigest(null != maps && maps.containsKey("0030_0005_110101_03") ? maps.get("0030_0005_110101_03").getDigest() : "");
				e3_110101.setDrAmount(wipe);
				e3_110101.setCrAmount(BigDecimal.ZERO);
				e3_110101.setSeq(seq);
				document.setDrAmount(document.getDrAmount().add(wipe));
				b_110101 = this.accountBookService.incrCredit(b_110101, wipe);
				seq++;
				entries.add(e3_110101);

				// 贷 应收投资收益
				DocumentEntry e4_1201 = new DocumentEntry();
				e4_1201.setOid(StringUtil.uuid());
				e4_1201.setRelative(relative);
				e4_1201.setTicket(ticket);
				e4_1201.setDocument(document);
				e4_1201.setBook(b_1201);
				e4_1201.setDigest(null != maps && maps.containsKey("0030_0005_1201_04") ? maps.get("0030_0005_1201_04").getDigest() : "");
				e4_1201.setDrAmount(BigDecimal.ZERO);
				e4_1201.setCrAmount(wipe);
				e4_1201.setSeq(seq);
				document.setCrAmount(document.getCrAmount().add(wipe));
				b_1201 = this.accountBookService.decrCredit(b_1201, wipe);
				seq++;
				entries.add(e4_1201);
			}
		}

		// 借 银行存款
		DocumentEntry e5_1001 = new DocumentEntry();
		e5_1001.setOid(StringUtil.uuid());
		e5_1001.setRelative(relative);
		e5_1001.setTicket(ticket);
		e5_1001.setDocument(document);
		e5_1001.setBook(b_1001);
		e5_1001.setDigest(null != maps && maps.containsKey("0030_0005_1001_05") ? maps.get("0030_0005_1001_05").getDigest() : "");
		e5_1001.setDrAmount(revenue);
		e5_1001.setCrAmount(BigDecimal.ZERO);
		e5_1001.setSeq(seq);
		document.setDrAmount(document.getDrAmount().add(revenue));
		b_1001 = this.accountBookService.incrCredit(b_1001, revenue);
		seq++;
		entries.add(e5_1001);

		// 贷 交易性金融资产_类别X
		DocumentEntry e6_110101 = new DocumentEntry();
		e6_110101.setOid(StringUtil.uuid());
		e6_110101.setRelative(relative);
		e6_110101.setTicket(ticket);
		e6_110101.setDocument(document);
		e6_110101.setBook(b_110101);
		e6_110101.setDigest(null != maps && maps.containsKey("0030_0005_110101_06") ? maps.get("0030_0005_110101_06").getDigest() : "");
		e6_110101.setDrAmount(BigDecimal.ZERO);
		e6_110101.setCrAmount(revenue);
		e6_110101.setSeq(seq);
		document.setCrAmount(document.getCrAmount().add(revenue));
		b_110101 = this.accountBookService.decrCredit(b_110101, revenue);
		seq++;
		entries.add(e6_110101);

		document = this.documentDao.save(document);
		for (DocumentEntry e : entries) {
			this.documentEntryService.save(e);
		}

		return document;
	}

	/**
	 * 信托计划 折价转让
	 * principal 本金
	 * estimate 估值
	 * revenue 交收额
	 */
	@Transactional
	public Document targetDiscountsTransOut(String relative, String ticket, BigDecimal principal, BigDecimal estimate, BigDecimal revenue) {

		DocTemplate template = this.docTemplateService.safeGet("0030_NBTC_0006_XTZCZJZR");
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
		document.setType(Document.TYPE_TARGET_TRANSFER);
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
		AccountBook b_1001 = this.accountBookService.safeGet(relative, this.accountService.get("1001"));
		AccountBook b_1201 = this.accountBookService.safeGet(relative, this.accountService.get("1201"));

		List<DocumentEntry> entries = new ArrayList<DocumentEntry>();

		BigDecimal discounts = estimate.subtract(revenue);

		BigDecimal wipe = BigDecimal.ZERO;

		if (discounts.compareTo(b_2201.getBalance()) > 0) {
			wipe = discounts.subtract(b_2201.getBalance());
		}

		BigDecimal income = discounts.subtract(wipe);

		// 勾销之后仍有收益, 记未分配收益
		if (income.compareTo(BigDecimal.ZERO) > 0) {
			// 借 未分配收益
			DocumentEntry e1_2201 = new DocumentEntry();
			e1_2201.setOid(StringUtil.uuid());
			e1_2201.setRelative(relative);
			e1_2201.setTicket(ticket);
			e1_2201.setDocument(document);
			e1_2201.setBook(b_2201);
			e1_2201.setDigest(null != maps && maps.containsKey("0030_0006_2201_01") ? maps.get("0030_0006_2201_01").getDigest() : "");
			e1_2201.setDrAmount(income);
			e1_2201.setCrAmount(BigDecimal.ZERO);
			e1_2201.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(income));
			b_2201 = this.accountBookService.decrCredit(b_2201, income);
			seq++;
			entries.add(e1_2201);

			// 贷 交易性金融资产_类别X
			DocumentEntry e2_110101 = new DocumentEntry();
			e2_110101.setOid(StringUtil.uuid());
			e2_110101.setRelative(relative);
			e2_110101.setTicket(ticket);
			e2_110101.setDocument(document);
			e2_110101.setBook(b_110101);
			e2_110101.setDigest(null != maps && maps.containsKey("0030_0006_110101_02") ? maps.get("0030_0006_110101_02").getDigest() : "");
			e2_110101.setDrAmount(BigDecimal.ZERO);
			e2_110101.setCrAmount(income);
			e2_110101.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(income));
			b_110101 = this.accountBookService.decrCredit(b_110101, income);
			seq++;
			entries.add(e2_110101);
		}

		// 需要调整应收投资收益头寸
		if (wipe.compareTo(BigDecimal.ZERO) > 0) {
			// 借 应收投资收益
			DocumentEntry e3_1201 = new DocumentEntry();
			e3_1201.setOid(StringUtil.uuid());
			e3_1201.setRelative(relative);
			e3_1201.setTicket(ticket);
			e3_1201.setDocument(document);
			e3_1201.setBook(b_1201);
			e3_1201.setDigest(null != maps && maps.containsKey("0030_0006_1201_03") ? maps.get("0030_0006_1201_03").getDigest() : "");
			e3_1201.setDrAmount(wipe);
			e3_1201.setCrAmount(BigDecimal.ZERO);
			e3_1201.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(wipe));
			b_1201 = this.accountBookService.incrCredit(b_1201, wipe);
			seq++;
			entries.add(e3_1201);

			// 贷 交易性金融资产_类别X
			DocumentEntry e4_110101 = new DocumentEntry();
			e4_110101.setOid(StringUtil.uuid());
			e4_110101.setRelative(relative);
			e4_110101.setTicket(ticket);
			e4_110101.setDocument(document);
			e4_110101.setBook(b_110101);
			e4_110101.setDigest(null != maps && maps.containsKey("0030_0006_110101_04") ? maps.get("0030_0006_110101_04").getDigest() : "");
			e4_110101.setDrAmount(BigDecimal.ZERO);
			e4_110101.setCrAmount(wipe);
			e4_110101.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(wipe));
			b_110101 = this.accountBookService.decrCredit(b_110101, wipe);
			seq++;
			entries.add(e4_110101);
		}

		// 借 银行存款
		DocumentEntry e5_1001 = new DocumentEntry();
		e5_1001.setOid(StringUtil.uuid());
		e5_1001.setRelative(relative);
		e5_1001.setTicket(ticket);
		e5_1001.setDocument(document);
		e5_1001.setBook(b_1001);
		e5_1001.setDigest(null != maps && maps.containsKey("0030_0006_1001_05") ? maps.get("0030_0006_1001_05").getDigest() : "");
		e5_1001.setDrAmount(revenue);
		e5_1001.setCrAmount(BigDecimal.ZERO);
		e5_1001.setSeq(seq);
		document.setDrAmount(document.getDrAmount().add(revenue));
		b_1001 = this.accountBookService.incrCredit(b_1001, revenue);
		seq++;
		entries.add(e5_1001);

		// 贷 交易性金融资产_类别X
		DocumentEntry e6_110101 = new DocumentEntry();
		e6_110101.setOid(StringUtil.uuid());
		e6_110101.setRelative(relative);
		e6_110101.setTicket(ticket);
		e6_110101.setDocument(document);
		e6_110101.setBook(b_110101);
		e6_110101.setDigest(null != maps && maps.containsKey("0030_0006_110101_06") ? maps.get("0030_0006_110101_06").getDigest() : "");
		e6_110101.setDrAmount(BigDecimal.ZERO);
		e6_110101.setCrAmount(revenue);
		e6_110101.setSeq(seq);
		document.setCrAmount(document.getCrAmount().add(revenue));
		b_110101 = this.accountBookService.decrCredit(b_110101, revenue);
		seq++;
		entries.add(e6_110101);

		document = this.documentDao.save(document);
		for (DocumentEntry e : entries) {
			this.documentEntryService.save(e);
		}

		return document;
	}

	/**
	 * 信托计划 转入
	 */
	public Document targetTransIn(String relative, String ticket, BigDecimal amount) {

		DocTemplate template = this.docTemplateService.safeGet("0030_NBTC_0007_XTZCZR");
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
		document.setType(Document.TYPE_TARGET_PURCHASE);
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
		AccountBook b_1001 = this.accountBookService.safeGet(relative, this.accountService.get("1001"));

		// 借 交易性金融资产_类别X
		DocumentEntry e1_110101 = new DocumentEntry();
		e1_110101.setOid(StringUtil.uuid());
		e1_110101.setRelative(relative);
		e1_110101.setTicket(ticket);
		e1_110101.setDocument(document);
		e1_110101.setBook(b_110101);
		e1_110101.setDigest(null != maps && maps.containsKey("0030_0007_110101_01") ? maps.get("0030_0007_110101_01").getDigest() : "");
		e1_110101.setDrAmount(amount);
		e1_110101.setCrAmount(BigDecimal.ZERO);
		e1_110101.setSeq(seq);
		document.setDrAmount(document.getDrAmount().add(amount));
		b_110101 = this.accountBookService.incrCredit(b_110101, amount);
		seq++;

		// 贷 银行存款
		DocumentEntry e2_1001 = new DocumentEntry();
		e2_1001.setOid(StringUtil.uuid());
		e2_1001.setRelative(relative);
		e2_1001.setTicket(ticket);
		e2_1001.setDocument(document);
		e2_1001.setBook(b_1001);
		e2_1001.setDigest(null != maps && maps.containsKey("0030_0007_1001_02") ? maps.get("0030_0007_1001_02").getDigest() : "");
		e2_1001.setDrAmount(BigDecimal.ZERO);
		e2_1001.setCrAmount(amount);
		e2_1001.setSeq(seq);
		document.setCrAmount(document.getCrAmount().add(amount));
		b_1001 = this.accountBookService.decrCredit(b_1001, amount);
		seq++;

		document = this.documentDao.save(document);
		this.documentEntryService.save(e1_110101);
		this.documentEntryService.save(e2_1001);

		return document;

	}

}

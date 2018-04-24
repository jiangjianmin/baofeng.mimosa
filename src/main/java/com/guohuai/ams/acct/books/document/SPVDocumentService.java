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
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.StringUtil;

/**
 * 
 * 凭证业务 - SPV 申购/赎回
 * 
 * @author Arthur
 *
 */
@Service
public class SPVDocumentService {

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
	 * 
	 * @param relative
	 *            资产池oid
	 * @param ticket
	 *            订单oid
	 * @param market
	 *            申购确认市值
	 * @param saleable
	 *            可售份额
	 * @param chargefee
	 *            手续费/应付费金
	 * @return 会计凭证
	 */
	@Transactional
	public Document purchase(String relative, String ticket, BigDecimal market, BigDecimal saleable, BigDecimal chargefee) {

		// market = saleaable + chargefee;
		if (market.compareTo(saleable.add(chargefee)) != 0) {
			throw new AMPException("会计分录试算不平衡.");
		}

		DocTemplate template = this.docTemplateService.safeGet("0031_SPVT_0001_SG");
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
		document.setType(Document.TYPE_SPV_TRADE);
		document.setRelative(relative);
		document.setTicket(ticket);
		document.setAcctSn(this.documentSnService.genSn(date));
		document.setAcctDate(date);
		document.setInvoiceNum(0);
		document.setDrAmount(BigDecimal.ZERO);
		document.setCrAmount(BigDecimal.ZERO);
		document.setCreateTime(now);
		document.setUpdateTime(now);

		AccountBook b_1111 = this.accountBookService.safeGet(relative, this.accountService.get("1111"));
		AccountBook b_300102 = this.accountBookService.safeGet(relative, this.accountService.get("300102"));
		AccountBook b_2301 = this.accountBookService.safeGet(relative, this.accountService.get("2301"));
		AccountBook b_1401 = this.accountBookService.safeGet(relative, this.accountService.get("1401"));

		List<DocumentEntry> entries = new ArrayList<DocumentEntry>();

		// 若可售份额的增加 < SPV申购确认的市值增加
		if (saleable.compareTo(market) <= 0) {
			if (saleable.compareTo(BigDecimal.ZERO) > 0) {
				// 借： 投资资产
				DocumentEntry e1_1111 = new DocumentEntry();
				e1_1111.setOid(StringUtil.uuid());
				e1_1111.setRelative(relative);
				e1_1111.setTicket(ticket);
				e1_1111.setDocument(document);
				e1_1111.setBook(b_1111);
				e1_1111.setDigest(null != maps && maps.containsKey("0031_0001_1111_01") ? maps.get("0031_0001_1111_01").getDigest() : "");
				e1_1111.setDrAmount(saleable);
				e1_1111.setCrAmount(BigDecimal.ZERO);
				e1_1111.setSeq(seq);
				document.setDrAmount(document.getDrAmount().add(saleable));
				b_1111 = this.accountBookService.incrCredit(b_1111, saleable);
				seq++;
				entries.add(e1_1111);

				// 贷： 所有者权益_SPV
				DocumentEntry e2_300102 = new DocumentEntry();
				e2_300102.setOid(StringUtil.uuid());
				e2_300102.setRelative(relative);
				e2_300102.setTicket(ticket);
				e2_300102.setDocument(document);
				e2_300102.setBook(b_300102);
				e2_300102.setDigest(null != maps && maps.containsKey("0031_0001_300102_02") ? maps.get("0031_0001_300102_02").getDigest() : "");
				e2_300102.setDrAmount(BigDecimal.ZERO);
				e2_300102.setCrAmount(saleable);
				e2_300102.setSeq(seq);
				document.setCrAmount(document.getCrAmount().add(saleable));
				b_300102 = this.accountBookService.incrCredit(b_300102, saleable);
				seq++;
				entries.add(e2_300102);
			}

			// 如果有spv垫资
			if (chargefee.compareTo(BigDecimal.ZERO) > 0) {

				BigDecimal wipe = b_1401.getBalance().compareTo(chargefee) > 0 ? chargefee : b_1401.getBalance();
				BigDecimal advance = chargefee.subtract(wipe);

				// 还有剩余垫资
				if (advance.compareTo(BigDecimal.ZERO) > 0) {
					// 借： 投资资产
					DocumentEntry e3_1111 = new DocumentEntry();
					e3_1111.setOid(StringUtil.uuid());
					e3_1111.setRelative(relative);
					e3_1111.setTicket(ticket);
					e3_1111.setDocument(document);
					e3_1111.setBook(b_1111);
					e3_1111.setDigest(null != maps && maps.containsKey("0031_0001_1111_03") ? maps.get("0031_0001_1111_03").getDigest() : "");
					e3_1111.setDrAmount(advance);
					e3_1111.setCrAmount(BigDecimal.ZERO);
					e3_1111.setSeq(seq);
					document.setDrAmount(document.getDrAmount().add(advance));
					b_1111 = this.accountBookService.incrCredit(b_1111, advance);
					seq++;
					entries.add(e3_1111);

					// 贷： 应付费金
					DocumentEntry e4_2301 = new DocumentEntry();
					e4_2301.setOid(StringUtil.uuid());
					e4_2301.setRelative(relative);
					e4_2301.setTicket(ticket);
					e4_2301.setDocument(document);
					e4_2301.setBook(b_2301);
					e4_2301.setDigest(null != maps && maps.containsKey("0031_0001_2301_04") ? maps.get("0031_0001_2301_04").getDigest() : "");
					e4_2301.setDrAmount(BigDecimal.ZERO);
					e4_2301.setCrAmount(advance);
					e4_2301.setSeq(seq);
					document.setCrAmount(document.getCrAmount().add(advance));
					b_2301 = this.accountBookService.incrCredit(b_2301, advance);
					seq++;
					entries.add(e4_2301);
				}

				// 需要对预付费金冲账
				if (wipe.compareTo(BigDecimal.ZERO) > 0) {
					// 借： 投资资产
					DocumentEntry e5_1111 = new DocumentEntry();
					e5_1111.setOid(StringUtil.uuid());
					e5_1111.setRelative(relative);
					e5_1111.setTicket(ticket);
					e5_1111.setDocument(document);
					e5_1111.setBook(b_1111);
					e5_1111.setDigest(null != maps && maps.containsKey("0031_0001_1111_05") ? maps.get("0031_0001_1111_05").getDigest() : "");
					e5_1111.setDrAmount(wipe);
					e5_1111.setCrAmount(BigDecimal.ZERO);
					e5_1111.setSeq(seq);
					document.setDrAmount(document.getDrAmount().add(wipe));
					b_1111 = this.accountBookService.incrCredit(b_1111, wipe);
					seq++;
					entries.add(e5_1111);

					// 贷： 预付费金
					DocumentEntry e6_1401 = new DocumentEntry();
					e6_1401.setOid(StringUtil.uuid());
					e6_1401.setRelative(relative);
					e6_1401.setTicket(ticket);
					e6_1401.setDocument(document);
					e6_1401.setBook(b_1401);
					e6_1401.setDigest(null != maps && maps.containsKey("0031_0001_1401_06") ? maps.get("0031_0001_1401_06").getDigest() : "");
					e6_1401.setDrAmount(BigDecimal.ZERO);
					e6_1401.setCrAmount(wipe);
					e6_1401.setSeq(seq);
					document.setCrAmount(document.getCrAmount().add(wipe));
					b_1401 = this.accountBookService.decrCredit(b_1401, wipe);
					seq++;
					entries.add(e6_1401);
				}

			}

		}

		// 若可售份额的增加 > SPV申购确认的市值增加
		if (saleable.compareTo(market) > 0) {

			// 借： 投资资产
			DocumentEntry e7_1111 = new DocumentEntry();
			e7_1111.setOid(StringUtil.uuid());
			e7_1111.setRelative(relative);
			e7_1111.setTicket(ticket);
			e7_1111.setDocument(document);
			e7_1111.setBook(b_1111);
			e7_1111.setDigest(null != maps && maps.containsKey("0031_0001_1111_07") ? maps.get("0031_0001_1111_07").getDigest() : "");
			e7_1111.setDrAmount(market);
			e7_1111.setCrAmount(BigDecimal.ZERO);
			e7_1111.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(market));
			b_1111 = this.accountBookService.incrCredit(b_1111, market);
			seq++;
			entries.add(e7_1111);

			// 贷： 所有者权益_SPV
			DocumentEntry e8_300102 = new DocumentEntry();
			e8_300102.setOid(StringUtil.uuid());
			e8_300102.setRelative(relative);
			e8_300102.setTicket(ticket);
			e8_300102.setDocument(document);
			e8_300102.setBook(b_300102);
			e8_300102.setDigest(null != maps && maps.containsKey("0031_0001_300102_08") ? maps.get("0031_0001_300102_08").getDigest() : "");
			e8_300102.setDrAmount(BigDecimal.ZERO);
			e8_300102.setCrAmount(market);
			e8_300102.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(market));
			b_300102 = this.accountBookService.incrCredit(b_300102, market);
			seq++;
			entries.add(e8_300102);

			BigDecimal fee = chargefee.abs();
			BigDecimal wipe = b_2301.getBalance().compareTo(fee) > 0 ? fee : b_2301.getBalance();
			BigDecimal retain = fee.subtract(wipe);

			// 有应付费金头寸
			if (wipe.compareTo(BigDecimal.ZERO) > 0) {

				// 借： 应付费金
				DocumentEntry e9_2301 = new DocumentEntry();
				e9_2301.setOid(StringUtil.uuid());
				e9_2301.setRelative(relative);
				e9_2301.setTicket(ticket);
				e9_2301.setDocument(document);
				e9_2301.setBook(b_2301);
				e9_2301.setDigest(null != maps && maps.containsKey("0031_0001_2301_09") ? maps.get("0031_0001_2301_09").getDigest() : "");
				e9_2301.setDrAmount(wipe);
				e9_2301.setCrAmount(BigDecimal.ZERO);
				e9_2301.setSeq(seq);
				document.setDrAmount(document.getDrAmount().add(wipe));
				b_2301 = this.accountBookService.decrCredit(b_2301, wipe);
				seq++;
				entries.add(e9_2301);

				// 贷：投资者权益_SPV
				DocumentEntry e10_300102 = new DocumentEntry();
				e10_300102.setOid(StringUtil.uuid());
				e10_300102.setRelative(relative);
				e10_300102.setTicket(ticket);
				e10_300102.setDocument(document);
				e10_300102.setBook(b_300102);
				e10_300102.setDigest(null != maps && maps.containsKey("0031_0001_300102_10") ? maps.get("0031_0001_300102_10").getDigest() : "");
				e10_300102.setDrAmount(BigDecimal.ZERO);
				e10_300102.setCrAmount(wipe);
				e10_300102.setSeq(seq);
				document.setCrAmount(document.getCrAmount().add(wipe));
				b_300102 = this.accountBookService.incrCredit(b_300102, wipe);
				seq++;
				entries.add(e10_300102);

			}

			// 应付费金缺头寸, 用预付费金冲账
			if (retain.compareTo(BigDecimal.ZERO) > 0) {

				// 借： 预付费金
				DocumentEntry e11_1401 = new DocumentEntry();
				e11_1401.setOid(StringUtil.uuid());
				e11_1401.setRelative(relative);
				e11_1401.setTicket(ticket);
				e11_1401.setDocument(document);
				e11_1401.setBook(b_1401);
				e11_1401.setDigest(null != maps && maps.containsKey("0031_0001_1401_11") ? maps.get("0031_0001_1401_11").getDigest() : "");
				e11_1401.setDrAmount(retain);
				e11_1401.setCrAmount(BigDecimal.ZERO);
				e11_1401.setSeq(seq);
				document.setDrAmount(document.getDrAmount().add(retain));
				b_1401 = this.accountBookService.incrCredit(b_1401, retain);
				seq++;
				entries.add(e11_1401);

				// 贷：投资者权益_SPV
				DocumentEntry e12_300102 = new DocumentEntry();
				e12_300102.setOid(StringUtil.uuid());
				e12_300102.setRelative(relative);
				e12_300102.setTicket(ticket);
				e12_300102.setDocument(document);
				e12_300102.setBook(b_300102);
				e12_300102.setDigest(null != maps && maps.containsKey("0031_0001_300102_12") ? maps.get("0031_0001_300102_12").getDigest() : "");
				e12_300102.setDrAmount(BigDecimal.ZERO);
				e12_300102.setCrAmount(retain);
				e12_300102.setSeq(seq);
				document.setCrAmount(document.getCrAmount().add(retain));
				b_300102 = this.accountBookService.incrCredit(b_300102, retain);
				seq++;
				entries.add(e12_300102);

			}

		}

		document = this.documentDao.save(document);
		for (DocumentEntry e : entries) {
			this.documentEntryService.save(e);
		}

		return document;
	}

	/**
	 * 
	 * @param relative
	 *            资产池oid
	 * @param ticket
	 *            订单oid
	 * @param market
	 *            申购确认市值
	 * @param saleable
	 *            可售份额
	 * @param chargefee
	 *            手续费/应付费金
	 * @return 会计凭证
	 */
	@Transactional
	public Document redemption(String relative, String ticket, BigDecimal market, BigDecimal saleable, BigDecimal chargefee) {

		// market = saleaable + chargefee;
		if (market.compareTo(saleable.add(chargefee)) != 0) {
			throw new AMPException("会计分录试算不平衡.");
		}

		DocTemplate template = this.docTemplateService.safeGet("0031_SPVT_0002_SH");
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
		document.setType(Document.TYPE_SPV_TRADE);
		document.setRelative(relative);
		document.setTicket(ticket);
		document.setAcctSn(this.documentSnService.genSn(date));
		document.setAcctDate(date);
		document.setInvoiceNum(0);
		document.setDrAmount(BigDecimal.ZERO);
		document.setCrAmount(BigDecimal.ZERO);
		document.setCreateTime(now);
		document.setUpdateTime(now);

		AccountBook b_1111 = this.accountBookService.safeGet(relative, this.accountService.get("1111"));
		AccountBook b_300102 = this.accountBookService.safeGet(relative, this.accountService.get("300102"));
		AccountBook b_2301 = this.accountBookService.safeGet(relative, this.accountService.get("2301"));
		AccountBook b_1401 = this.accountBookService.safeGet(relative, this.accountService.get("1401"));

		List<DocumentEntry> entries = new ArrayList<DocumentEntry>();

		// 若可售份额的减少 < SPV申购确认的市值增加
		if (saleable.compareTo(market) <= 0) {
			if (saleable.compareTo(BigDecimal.ZERO) > 0) {
				// 借： 所有者权益_SPV
				DocumentEntry e1_300102 = new DocumentEntry();
				e1_300102.setOid(StringUtil.uuid());
				e1_300102.setRelative(relative);
				e1_300102.setTicket(ticket);
				e1_300102.setDocument(document);
				e1_300102.setBook(b_300102);
				e1_300102.setDigest(null != maps && maps.containsKey("0031_0002_300102_01") ? maps.get("0031_0002_300102_01").getDigest() : "");
				e1_300102.setDrAmount(saleable);
				e1_300102.setCrAmount(BigDecimal.ZERO);
				e1_300102.setSeq(seq);
				document.setDrAmount(document.getDrAmount().add(saleable));
				b_300102 = this.accountBookService.decrCredit(b_300102, saleable);
				seq++;
				entries.add(e1_300102);

				// 贷： 投资资产
				DocumentEntry e2_1111 = new DocumentEntry();
				e2_1111.setOid(StringUtil.uuid());
				e2_1111.setRelative(relative);
				e2_1111.setTicket(ticket);
				e2_1111.setDocument(document);
				e2_1111.setBook(b_1111);
				e2_1111.setDigest(null != maps && maps.containsKey("0031_0002_1111_02") ? maps.get("0031_0002_1111_02").getDigest() : "");
				e2_1111.setDrAmount(BigDecimal.ZERO);
				e2_1111.setCrAmount(saleable);
				e2_1111.setSeq(seq);
				document.setCrAmount(document.getCrAmount().add(saleable));
				b_1111 = this.accountBookService.decrCredit(b_1111, saleable);
				seq++;
				entries.add(e2_1111);

			}

			// 如果有spv提留
			if (chargefee.compareTo(BigDecimal.ZERO) > 0) {

				BigDecimal wipe = b_2301.getBalance().compareTo(chargefee) > 0 ? chargefee : b_2301.getBalance();
				BigDecimal retain = chargefee.subtract(wipe);

				// 还有剩余垫资
				if (wipe.compareTo(BigDecimal.ZERO) > 0) {
					// 借：应付费金
					DocumentEntry e3_2301 = new DocumentEntry();
					e3_2301.setOid(StringUtil.uuid());
					e3_2301.setRelative(relative);
					e3_2301.setTicket(ticket);
					e3_2301.setDocument(document);
					e3_2301.setBook(b_2301);
					e3_2301.setDigest(null != maps && maps.containsKey("0031_0002_2301_03") ? maps.get("0031_0002_2301_03").getDigest() : "");
					e3_2301.setDrAmount(wipe);
					e3_2301.setCrAmount(BigDecimal.ZERO);
					e3_2301.setSeq(seq);
					document.setDrAmount(document.getDrAmount().add(wipe));
					b_2301 = this.accountBookService.decrCredit(b_2301, wipe);
					seq++;
					entries.add(e3_2301);

					// 贷：投资资产
					DocumentEntry e4_1111 = new DocumentEntry();
					e4_1111.setOid(StringUtil.uuid());
					e4_1111.setRelative(relative);
					e4_1111.setTicket(ticket);
					e4_1111.setDocument(document);
					e4_1111.setBook(b_1111);
					e4_1111.setDigest(null != maps && maps.containsKey("0031_0002_1111_04") ? maps.get("0031_0002_1111_04").getDigest() : "");
					e4_1111.setDrAmount(BigDecimal.ZERO);
					e4_1111.setCrAmount(wipe);
					e4_1111.setSeq(seq);
					document.setCrAmount(document.getCrAmount().add(wipe));
					b_1111 = this.accountBookService.decrCredit(b_1111, wipe);
					seq++;
					entries.add(e4_1111);
				}

				// 应付费金缺头寸, 用预付费金冲账
				if (retain.compareTo(BigDecimal.ZERO) > 0) {
					// 借： 预付费金
					DocumentEntry e5_1401 = new DocumentEntry();
					e5_1401.setOid(StringUtil.uuid());
					e5_1401.setRelative(relative);
					e5_1401.setTicket(ticket);
					e5_1401.setDocument(document);
					e5_1401.setBook(b_1401);
					e5_1401.setDigest(null != maps && maps.containsKey("0031_0002_1401_05") ? maps.get("0031_0002_1401_05").getDigest() : "");
					e5_1401.setDrAmount(retain);
					e5_1401.setCrAmount(BigDecimal.ZERO);
					e5_1401.setSeq(seq);
					document.setDrAmount(document.getDrAmount().add(retain));
					b_1401 = this.accountBookService.incrCredit(b_1401, retain);
					seq++;
					entries.add(e5_1401);

					// 贷： 投资资产
					DocumentEntry e6_1111 = new DocumentEntry();
					e6_1111.setOid(StringUtil.uuid());
					e6_1111.setRelative(relative);
					e6_1111.setTicket(ticket);
					e6_1111.setDocument(document);
					e6_1111.setBook(b_1111);
					e6_1111.setDigest(null != maps && maps.containsKey("0031_0002_1111_06") ? maps.get("0031_0002_1111_06").getDigest() : "");
					e6_1111.setDrAmount(BigDecimal.ZERO);
					e6_1111.setCrAmount(retain);
					e6_1111.setSeq(seq);
					document.setCrAmount(document.getCrAmount().add(retain));
					b_1111 = this.accountBookService.decrCredit(b_1111, retain);
					seq++;
					entries.add(e6_1111);
				}

			}
		}

		// 若可售份额的减少 > SPV申购确认的市值增加
		if (saleable.compareTo(market) > 0) {

			// 借：所有者权益_SPV
			DocumentEntry e7_300102 = new DocumentEntry();
			e7_300102.setOid(StringUtil.uuid());
			e7_300102.setRelative(relative);
			e7_300102.setTicket(ticket);
			e7_300102.setDocument(document);
			e7_300102.setBook(b_300102);
			e7_300102.setDigest(null != maps && maps.containsKey("0031_0002_300102_07") ? maps.get("0031_0002_300102_07").getDigest() : "");
			e7_300102.setDrAmount(market);
			e7_300102.setCrAmount(BigDecimal.ZERO);
			e7_300102.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(market));
			b_300102 = this.accountBookService.decrCredit(b_300102, market);
			seq++;
			entries.add(e7_300102);

			// 贷：投资资产
			DocumentEntry e8_1111 = new DocumentEntry();
			e8_1111.setOid(StringUtil.uuid());
			e8_1111.setRelative(relative);
			e8_1111.setTicket(ticket);
			e8_1111.setDocument(document);
			e8_1111.setBook(b_1111);
			e8_1111.setDigest(null != maps && maps.containsKey("0031_0002_1111_08") ? maps.get("0031_0002_1111_08").getDigest() : "");
			e8_1111.setDrAmount(BigDecimal.ZERO);
			e8_1111.setCrAmount(market);
			e8_1111.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(market));
			b_1111 = this.accountBookService.decrCredit(b_1111, market);
			seq++;
			entries.add(e8_1111);

			BigDecimal fee = chargefee.abs();
			BigDecimal wipe = b_1401.getBalance().compareTo(fee) > 0 ? fee : b_1401.getBalance();
			BigDecimal advance = fee.subtract(wipe);

			// 调整应付费金
			if (advance.compareTo(BigDecimal.ZERO) > 0) {
				// 借：所有者权益_SPV
				DocumentEntry e9_300102 = new DocumentEntry();
				e9_300102.setOid(StringUtil.uuid());
				e9_300102.setRelative(relative);
				e9_300102.setTicket(ticket);
				e9_300102.setDocument(document);
				e9_300102.setBook(b_300102);
				e9_300102.setDigest(null != maps && maps.containsKey("0031_0002_300102_09") ? maps.get("0031_0002_300102_09").getDigest() : "");
				e9_300102.setDrAmount(advance);
				e9_300102.setCrAmount(BigDecimal.ZERO);
				e9_300102.setSeq(seq);
				document.setDrAmount(document.getDrAmount().add(advance));
				b_300102 = this.accountBookService.decrCredit(b_300102, advance);
				seq++;
				entries.add(e9_300102);

				// 贷：应付费金
				DocumentEntry e10_2301 = new DocumentEntry();
				e10_2301.setOid(StringUtil.uuid());
				e10_2301.setRelative(relative);
				e10_2301.setTicket(ticket);
				e10_2301.setDocument(document);
				e10_2301.setBook(b_2301);
				e10_2301.setDigest(null != maps && maps.containsKey("0031_0002_2301_10") ? maps.get("0031_0002_2301_10").getDigest() : "");
				e10_2301.setDrAmount(BigDecimal.ZERO);
				e10_2301.setCrAmount(advance);
				e10_2301.setSeq(seq);
				document.setCrAmount(document.getCrAmount().add(advance));
				b_2301 = this.accountBookService.incrCredit(b_2301, advance);
				seq++;
				entries.add(e10_2301);
			}

			// 有预付费金头寸, 进行冲账
			if (wipe.compareTo(BigDecimal.ZERO) > 0) {
				// 借：所有者权益_SPV
				DocumentEntry e11_300102 = new DocumentEntry();
				e11_300102.setOid(StringUtil.uuid());
				e11_300102.setRelative(relative);
				e11_300102.setTicket(ticket);
				e11_300102.setDocument(document);
				e11_300102.setBook(b_300102);
				e11_300102.setDigest(null != maps && maps.containsKey("0031_0002_300102_11") ? maps.get("0031_0002_300102_11").getDigest() : "");
				e11_300102.setDrAmount(wipe);
				e11_300102.setCrAmount(BigDecimal.ZERO);
				e11_300102.setSeq(seq);
				document.setDrAmount(document.getDrAmount().add(wipe));
				b_300102 = this.accountBookService.decrCredit(b_300102, wipe);
				seq++;
				entries.add(e11_300102);

				// 贷：预付费金
				DocumentEntry e12_1401 = new DocumentEntry();
				e12_1401.setOid(StringUtil.uuid());
				e12_1401.setRelative(relative);
				e12_1401.setTicket(ticket);
				e12_1401.setDocument(document);
				e12_1401.setBook(b_1401);
				e12_1401.setDigest(null != maps && maps.containsKey("0031_0002_1401_12") ? maps.get("0031_0002_1401_12").getDigest() : "");
				e12_1401.setDrAmount(BigDecimal.ZERO);
				e12_1401.setCrAmount(wipe);
				e12_1401.setSeq(seq);
				document.setCrAmount(document.getCrAmount().add(wipe));
				b_1401 = this.accountBookService.decrCredit(b_1401, wipe);
				seq++;
				entries.add(e12_1401);
			}

		}

		document = this.documentDao.save(document);
		for (DocumentEntry e : entries) {
			this.documentEntryService.save(e);
		}

		return document;
	}

	@Transactional
	private Document incrIncome(String relative, String ticket, BigDecimal income) {

		if (income.compareTo(BigDecimal.ZERO) <= 0) {
			throw new AMPException("收益参数错误.");
		}

		DocTemplate template = this.docTemplateService.safeGet("0051_SPVI_0001_SYZZ");
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
		document.setType(Document.TYPE_SPV_INCOME_CONFIRM);
		document.setRelative(relative);
		document.setTicket(ticket);
		document.setAcctSn(this.documentSnService.genSn(date));
		document.setAcctDate(date);
		document.setInvoiceNum(0);
		document.setDrAmount(BigDecimal.ZERO);
		document.setCrAmount(BigDecimal.ZERO);
		document.setCreateTime(now);
		document.setUpdateTime(now);

		AccountBook b_1111 = this.accountBookService.safeGet(relative, this.accountService.get("1111"));
		AccountBook b_2201 = this.accountBookService.safeGet(relative, this.accountService.get("2201"));
		AccountBook b_1201 = this.accountBookService.safeGet(relative, this.accountService.get("1201"));

		List<DocumentEntry> entries = new ArrayList<DocumentEntry>();

		BigDecimal wipe = b_1201.getBalance().compareTo(income) > 0 ? income : b_1201.getBalance();
		BigDecimal incr = income.subtract(wipe);

		// 如果勾销完仍有收益
		if (incr.compareTo(BigDecimal.ZERO) > 0) {
			// 借：投资资产
			DocumentEntry e1_1111 = new DocumentEntry();
			e1_1111.setOid(StringUtil.uuid());
			e1_1111.setRelative(relative);
			e1_1111.setTicket(ticket);
			e1_1111.setDocument(document);
			e1_1111.setBook(b_1111);
			e1_1111.setDigest(null != maps && maps.containsKey("0051_0001_1111_01") ? maps.get("0051_0001_1111_01").getDigest() : "");
			e1_1111.setDrAmount(incr);
			e1_1111.setCrAmount(BigDecimal.ZERO);
			e1_1111.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(incr));
			b_1111 = this.accountBookService.incrCredit(b_1111, incr);
			seq++;
			entries.add(e1_1111);

			// 贷：未分配收益
			DocumentEntry e2_2201 = new DocumentEntry();
			e2_2201.setOid(StringUtil.uuid());
			e2_2201.setRelative(relative);
			e2_2201.setTicket(ticket);
			e2_2201.setDocument(document);
			e2_2201.setBook(b_2201);
			e2_2201.setDigest(null != maps && maps.containsKey("0051_0001_2201_02") ? maps.get("0051_0001_2201_02").getDigest() : "");
			e2_2201.setDrAmount(BigDecimal.ZERO);
			e2_2201.setCrAmount(incr);
			e2_2201.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(incr));
			b_2201 = this.accountBookService.incrCredit(b_2201, incr);
			seq++;
			entries.add(e2_2201);

		}

		// 如果需要进行未分配收益账户冲账
		if (wipe.compareTo(BigDecimal.ZERO) > 0) {
			// 借：投资资产
			DocumentEntry e1_1111 = new DocumentEntry();
			e1_1111.setOid(StringUtil.uuid());
			e1_1111.setRelative(relative);
			e1_1111.setTicket(ticket);
			e1_1111.setDocument(document);
			e1_1111.setBook(b_1111);
			e1_1111.setDigest(null != maps && maps.containsKey("0051_0001_1111_03") ? maps.get("0051_0001_1111_03").getDigest() : "");
			e1_1111.setDrAmount(wipe);
			e1_1111.setCrAmount(BigDecimal.ZERO);
			e1_1111.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(wipe));
			b_1111 = this.accountBookService.incrCredit(b_1111, wipe);
			seq++;
			entries.add(e1_1111);

			// 贷：应收投资收益
			DocumentEntry e2_1201 = new DocumentEntry();
			e2_1201.setOid(StringUtil.uuid());
			e2_1201.setRelative(relative);
			e2_1201.setTicket(ticket);
			e2_1201.setDocument(document);
			e2_1201.setBook(b_1201);
			e2_1201.setDigest(null != maps && maps.containsKey("0051_0001_1201_04") ? maps.get("0051_0001_1201_04").getDigest() : "");
			e2_1201.setDrAmount(BigDecimal.ZERO);
			e2_1201.setCrAmount(wipe);
			e2_1201.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(wipe));
			b_1201 = this.accountBookService.decrCredit(b_1201, wipe);
			seq++;
			entries.add(e2_1201);
		}

		document = this.documentDao.save(document);
		for (DocumentEntry e : entries) {
			this.documentEntryService.save(e);
		}

		return document;
	}

	@Transactional
	private Document decrIncome(String relative, String ticket, BigDecimal income) {

		if (income.compareTo(BigDecimal.ZERO) <= 0) {
			throw new AMPException("收益参数错误.");
		}

		DocTemplate template = this.docTemplateService.safeGet("0051_SPVI_0002_SYJZ");
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
		document.setType(Document.TYPE_SPV_INCOME_CONFIRM);
		document.setRelative(relative);
		document.setTicket(ticket);
		document.setAcctSn(this.documentSnService.genSn(date));
		document.setAcctDate(date);
		document.setInvoiceNum(0);
		document.setDrAmount(BigDecimal.ZERO);
		document.setCrAmount(BigDecimal.ZERO);
		document.setCreateTime(now);
		document.setUpdateTime(now);

		AccountBook b_1111 = this.accountBookService.safeGet(relative, this.accountService.get("1111"));
		AccountBook b_2201 = this.accountBookService.safeGet(relative, this.accountService.get("2201"));
		AccountBook b_1201 = this.accountBookService.safeGet(relative, this.accountService.get("1201"));

		List<DocumentEntry> entries = new ArrayList<DocumentEntry>();

		BigDecimal decr = b_2201.getBalance().compareTo(income) > 0 ? income : b_2201.getBalance();
		BigDecimal wipe = income.subtract(decr);

		// 如果勾销完仍有收益
		if (decr.compareTo(BigDecimal.ZERO) > 0) {
			// 借：未分配收益
			DocumentEntry e1_2201 = new DocumentEntry();
			e1_2201.setOid(StringUtil.uuid());
			e1_2201.setRelative(relative);
			e1_2201.setTicket(ticket);
			e1_2201.setDocument(document);
			e1_2201.setBook(b_2201);
			e1_2201.setDigest(null != maps && maps.containsKey("0051_0002_2201_01") ? maps.get("0051_0002_2201_01").getDigest() : "");
			e1_2201.setDrAmount(decr);
			e1_2201.setCrAmount(BigDecimal.ZERO);
			e1_2201.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(decr));
			b_2201 = this.accountBookService.decrCredit(b_2201, decr);
			seq++;
			entries.add(e1_2201);

			// 贷：投资资产
			DocumentEntry e2_1111 = new DocumentEntry();
			e2_1111.setOid(StringUtil.uuid());
			e2_1111.setRelative(relative);
			e2_1111.setTicket(ticket);
			e2_1111.setDocument(document);
			e2_1111.setBook(b_1111);
			e2_1111.setDigest(null != maps && maps.containsKey("0051_0002_1111_02") ? maps.get("0051_0002_1111_02").getDigest() : "");
			e2_1111.setDrAmount(BigDecimal.ZERO);
			e2_1111.setCrAmount(decr);
			e2_1111.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(decr));
			b_1111 = this.accountBookService.decrCredit(b_1111, decr);
			seq++;
			entries.add(e2_1111);

		}

		// 未分配收益缺头寸
		if (wipe.compareTo(BigDecimal.ZERO) > 0) {
			// 借：应收投资收益
			DocumentEntry e1_1201 = new DocumentEntry();
			e1_1201.setOid(StringUtil.uuid());
			e1_1201.setRelative(relative);
			e1_1201.setTicket(ticket);
			e1_1201.setDocument(document);
			e1_1201.setBook(b_1201);
			e1_1201.setDigest(null != maps && maps.containsKey("0051_0002_1201_03") ? maps.get("0051_0002_1201_03").getDigest() : "");
			e1_1201.setDrAmount(wipe);
			e1_1201.setCrAmount(BigDecimal.ZERO);
			e1_1201.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(wipe));
			b_1201 = this.accountBookService.incrCredit(b_1201, wipe);
			seq++;
			entries.add(e1_1201);

			// 贷：投资资产
			DocumentEntry e2_1111 = new DocumentEntry();
			e2_1111.setOid(StringUtil.uuid());
			e2_1111.setRelative(relative);
			e2_1111.setTicket(ticket);
			e2_1111.setDocument(document);
			e2_1111.setBook(b_1111);
			e2_1111.setDigest(null != maps && maps.containsKey("0051_0002_1111_04") ? maps.get("0051_0002_1111_04").getDigest() : "");
			e2_1111.setDrAmount(BigDecimal.ZERO);
			e2_1111.setCrAmount(wipe);
			e2_1111.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(wipe));
			b_1111 = this.accountBookService.decrCredit(b_1111, wipe);
			seq++;
			entries.add(e2_1111);
		}

		document = this.documentDao.save(document);
		for (DocumentEntry e : entries) {
			this.documentEntryService.save(e);
		}

		return document;
	}

	/**
	 * 资管计划收益确认
	 * 
	 * @param relative
	 *            资产池oid
	 * @param ticket
	 *            原始凭证oid
	 * @param income
	 *            收益: 增值为正数, 减值为负数
	 * @return 凭证对象
	 */
	@Transactional
	public Document incomeConfirm(String relative, String ticket, BigDecimal income) {

		if (income.compareTo(BigDecimal.ZERO) > 0) {
			return this.incrIncome(relative, ticket, income);
		}

		if (income.compareTo(BigDecimal.ZERO) < 0) {
			return this.decrIncome(relative, ticket, income.abs());
		}

		throw new AMPException("收益参数错误.");

	}

	/**
	 * 收益发放
	 * 
	 * @param relative
	 *            资产池oid
	 * @param ticket
	 *            原始凭证oid
	 * @param amount
	 *            发放金额
	 * @return 凭证对象
	 */
	@Transactional
	public Document incomeAllocate(String relative, String ticket, BigDecimal amount) {

		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new AMPException("收益发放参数错误.");
		}

		DocTemplate template = this.docTemplateService.safeGet("0071_SPVA_0001_SYFF");
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
		document.setType(Document.TYPE_SPV_INCOME_ALLOCATE);
		document.setRelative(relative);
		document.setTicket(ticket);
		document.setAcctSn(this.documentSnService.genSn(date));
		document.setAcctDate(date);
		document.setInvoiceNum(0);
		document.setDrAmount(BigDecimal.ZERO);
		document.setCrAmount(BigDecimal.ZERO);
		document.setCreateTime(now);
		document.setUpdateTime(now);

		AccountBook b_300101 = this.accountBookService.safeGet(relative, this.accountService.get("300101"));
		AccountBook b_2201 = this.accountBookService.safeGet(relative, this.accountService.get("2201"));
		AccountBook b_1201 = this.accountBookService.safeGet(relative, this.accountService.get("1201"));

		List<DocumentEntry> entries = new ArrayList<DocumentEntry>();

		BigDecimal allocate = b_2201.getBalance().compareTo(amount) > 0 ? amount : b_2201.getBalance();
		BigDecimal wipe = amount.subtract(allocate);

		// 未分配收益有头寸
		if (allocate.compareTo(BigDecimal.ZERO) > 0) {
			// 借：未分配收益
			DocumentEntry e1_2201 = new DocumentEntry();
			e1_2201.setOid(StringUtil.uuid());
			e1_2201.setRelative(relative);
			e1_2201.setTicket(ticket);
			e1_2201.setDocument(document);
			e1_2201.setBook(b_2201);
			e1_2201.setDigest(null != maps && maps.containsKey("0071_0001_2201_01") ? maps.get("0071_0001_2201_01").getDigest() : "");
			e1_2201.setDrAmount(allocate);
			e1_2201.setCrAmount(BigDecimal.ZERO);
			e1_2201.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(allocate));
			b_2201 = this.accountBookService.decrCredit(b_2201, allocate);
			seq++;
			entries.add(e1_2201);

			// 贷：所有者权益_投资者
			DocumentEntry e2_300101 = new DocumentEntry();
			e2_300101.setOid(StringUtil.uuid());
			e2_300101.setRelative(relative);
			e2_300101.setTicket(ticket);
			e2_300101.setDocument(document);
			e2_300101.setBook(b_300101);
			e2_300101.setDigest(null != maps && maps.containsKey("0071_0001_300101_02") ? maps.get("0071_0001_300101_02").getDigest() : "");
			e2_300101.setDrAmount(BigDecimal.ZERO);
			e2_300101.setCrAmount(allocate);
			e2_300101.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(allocate));
			b_300101 = this.accountBookService.incrCredit(b_300101, allocate);
			seq++;
			entries.add(e2_300101);
		}

		// 未分配收益缺头寸, 需要应收投资收益勾销
		if (wipe.compareTo(BigDecimal.ZERO) > 0) {
			// 借：应收投资收益
			DocumentEntry e3_1201 = new DocumentEntry();
			e3_1201.setOid(StringUtil.uuid());
			e3_1201.setRelative(relative);
			e3_1201.setTicket(ticket);
			e3_1201.setDocument(document);
			e3_1201.setBook(b_1201);
			e3_1201.setDigest(null != maps && maps.containsKey("0071_0001_1201_03") ? maps.get("0071_0001_1201_03").getDigest() : "");
			e3_1201.setDrAmount(wipe);
			e3_1201.setCrAmount(BigDecimal.ZERO);
			e3_1201.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(wipe));
			b_1201 = this.accountBookService.incrCredit(b_1201, wipe);
			seq++;
			entries.add(e3_1201);

			// 贷：所有者权益_投资者
			DocumentEntry e4_300101 = new DocumentEntry();
			e4_300101.setOid(StringUtil.uuid());
			e4_300101.setRelative(relative);
			e4_300101.setTicket(ticket);
			e4_300101.setDocument(document);
			e4_300101.setBook(b_300101);
			e4_300101.setDigest(null != maps && maps.containsKey("0071_0001_300101_04") ? maps.get("0071_0001_300101_04").getDigest() : "");
			e4_300101.setDrAmount(BigDecimal.ZERO);
			e4_300101.setCrAmount(wipe);
			e4_300101.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(wipe));
			b_300101 = this.accountBookService.incrCredit(b_300101, wipe);
			seq++;
			entries.add(e4_300101);
		}

		document = this.documentDao.save(document);
		for (DocumentEntry e : entries) {
			this.documentEntryService.save(e);
		}

		return document;
	}

	/**
	 * 买卖单确认
	 * @param relative spv id
	 * @param ticket 份额确认事件oid
	 * @param amount 份额确认金额, 入款为正数, 出款为负数
	 * @return
	 */
	@Transactional
	public Document tradeConfirm(String relative, String ticket, BigDecimal amount) {

		if (amount.compareTo(BigDecimal.ZERO) > 0) {
			return this.rollInTradeConfirm(relative, ticket, amount);
		} else if (amount.compareTo(BigDecimal.ZERO) < 0) {
			return this.rollOutTradeConfirm(relative, ticket, amount.negate());
		} else {
			return null;
		}

	}

	/**
	 * 买卖单入款事件: 申购单 > 赎回单, SPV应转移份额到投资者
	 * 
	 * @param relative
	 * @param ticket
	 * @param amount
	 * @return
	 */
	@Transactional
	private Document rollInTradeConfirm(String relative, String ticket, BigDecimal amount) {

		DocTemplate template = this.docTemplateService.safeGet("0100_FEQR_0001_FEQRRK");
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
		document.setType(Document.TYPE_SPV_TRADE_CONFIRM);
		document.setRelative(relative);
		document.setTicket(ticket);
		document.setAcctSn(this.documentSnService.genSn(date));
		document.setAcctDate(date);
		document.setInvoiceNum(0);
		document.setDrAmount(BigDecimal.ZERO);
		document.setCrAmount(BigDecimal.ZERO);
		document.setCreateTime(now);
		document.setUpdateTime(now);

		AccountBook b_300101 = this.accountBookService.safeGet(relative, this.accountService.get("300101"));
		AccountBook b_300102 = this.accountBookService.safeGet(relative, this.accountService.get("300102"));

		// 借 投资者所有者权益
		DocumentEntry e1_300101 = new DocumentEntry();
		e1_300101.setOid(StringUtil.uuid());
		e1_300101.setRelative(relative);
		e1_300101.setTicket(ticket);
		e1_300101.setDocument(document);
		e1_300101.setBook(b_300101);
		e1_300101.setDigest(null != maps && maps.containsKey("0100_0001_300101_01") ? maps.get("0100_0001_300101_01").getDigest() : "");
		e1_300101.setDrAmount(amount);
		e1_300101.setCrAmount(BigDecimal.ZERO);
		e1_300101.setSeq(seq);
		document.setDrAmount(document.getDrAmount().add(amount));
		b_300101 = this.accountBookService.incrCredit(b_300101, amount);
		seq++;

		// 贷 SPV所有者权益
		DocumentEntry e2_300102 = new DocumentEntry();
		e2_300102.setOid(StringUtil.uuid());
		e2_300102.setRelative(relative);
		e2_300102.setTicket(ticket);
		e2_300102.setDocument(document);
		e2_300102.setBook(b_300102);
		e2_300102.setDigest(null != maps && maps.containsKey("0100_0001_300102_02") ? maps.get("0100_0001_300102_02").getDigest() : "");
		e2_300102.setDrAmount(BigDecimal.ZERO);
		e2_300102.setCrAmount(amount);
		e2_300102.setSeq(seq);
		document.setCrAmount(document.getCrAmount().add(amount));
		b_300102 = this.accountBookService.decrCredit(b_300102, amount);
		seq++;

		document = this.documentDao.save(document);

		this.documentEntryService.save(e1_300101);
		this.documentEntryService.save(e2_300102);

		return document;
	}

	/**
	 * 买卖单出款事件: 申购单 < 赎回单, 投资者应转移份额到SPV
	 * 
	 * @param relative
	 * @param ticket
	 * @param amount
	 * @return
	 */
	@Transactional
	private Document rollOutTradeConfirm(String relative, String ticket, BigDecimal amount) {

		DocTemplate template = this.docTemplateService.safeGet("0100_FEQR_0002_FEQRCK");
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
		document.setType(Document.TYPE_SPV_TRADE_CONFIRM);
		document.setRelative(relative);
		document.setTicket(ticket);
		document.setAcctSn(this.documentSnService.genSn(date));
		document.setAcctDate(date);
		document.setInvoiceNum(0);
		document.setDrAmount(BigDecimal.ZERO);
		document.setCrAmount(BigDecimal.ZERO);
		document.setCreateTime(now);
		document.setUpdateTime(now);

		AccountBook b_300101 = this.accountBookService.safeGet(relative, this.accountService.get("300101"));
		AccountBook b_300102 = this.accountBookService.safeGet(relative, this.accountService.get("300102"));

		// 借 SPV所有者权益
		DocumentEntry e1_300102 = new DocumentEntry();
		e1_300102.setOid(StringUtil.uuid());
		e1_300102.setRelative(relative);
		e1_300102.setTicket(ticket);
		e1_300102.setDocument(document);
		e1_300102.setBook(b_300102);
		e1_300102.setDigest(null != maps && maps.containsKey("0100_0002_300102_01") ? maps.get("0100_0002_300102_01").getDigest() : "");
		e1_300102.setDrAmount(amount);
		e1_300102.setCrAmount(BigDecimal.ZERO);
		e1_300102.setSeq(seq);
		document.setDrAmount(document.getDrAmount().add(amount));
		b_300102 = this.accountBookService.incrCredit(b_300102, amount);
		seq++;

		// 贷 投资者所有者权益
		DocumentEntry e2_300101 = new DocumentEntry();
		e2_300101.setOid(StringUtil.uuid());
		e2_300101.setRelative(relative);
		e2_300101.setTicket(ticket);
		e2_300101.setDocument(document);
		e2_300101.setBook(b_300101);
		e2_300101.setDigest(null != maps && maps.containsKey("0100_0002_300101_02") ? maps.get("0100_0002_300101_02").getDigest() : "");
		e2_300101.setDrAmount(BigDecimal.ZERO);
		e2_300101.setCrAmount(amount);
		e2_300101.setSeq(seq);
		document.setCrAmount(document.getCrAmount().add(amount));
		b_300101 = this.accountBookService.decrCredit(b_300101, amount);
		seq++;

		document = this.documentDao.save(document);

		this.documentEntryService.save(e1_300102);
		this.documentEntryService.save(e2_300101);

		return document;
	}

}

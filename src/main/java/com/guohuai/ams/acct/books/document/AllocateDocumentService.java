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
 * 凭证业务 - 收益分配
 * 
 * @author Arthur
 *
 */
@Service
public class AllocateDocumentService {

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
	 * 收益分配确认
	 * 
	 * @param amount
	 *            金额
	 * @return 会计凭证
	 */
	@Transactional
	public Document allocate(String relative, String ticket, BigDecimal amount) {

		DocTemplate template = this.docTemplateService.safeGet("0070_SYFP_0001_SYFPQR");
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
		document.setType(Document.TYPE_ASSETPOOL_ALLOCATE);
		document.setRelative(relative);
		document.setTicket(ticket);
		document.setAcctSn(this.documentSnService.genSn(date));
		document.setAcctDate(date);
		document.setInvoiceNum(0);
		document.setDrAmount(BigDecimal.ZERO);
		document.setCrAmount(BigDecimal.ZERO);
		document.setCreateTime(now);
		document.setUpdateTime(now);

		AccountBook b_2201 = this.accountBookService.safeGet(relative, this.accountService.get("2201"));
		AccountBook b_3001 = this.accountBookService.safeGet(relative, this.accountService.get("3001"));
		AccountBook b_1201 = this.accountBookService.safeGet(relative, this.accountService.get("1201"));

		BigDecimal allocate = b_2201.getBalance().compareTo(amount) >= 0 ? amount : b_2201.getBalance();
		BigDecimal wipe = b_2201.getBalance().compareTo(amount) >= 0 ? BigDecimal.ZERO : amount.subtract(b_2201.getBalance());

		List<DocumentEntry> entries = new ArrayList<DocumentEntry>();

		// 借 未分配收益 2201
		DocumentEntry e1_2201 = new DocumentEntry();
		e1_2201.setOid(StringUtil.uuid());
		e1_2201.setRelative(relative);
		e1_2201.setTicket(ticket);
		e1_2201.setDocument(document);
		e1_2201.setBook(b_2201);
		e1_2201.setDigest(null != maps && maps.containsKey("0070_0001_2201_01") ? maps.get("0070_0001_2201_01").getDigest() : "");
		e1_2201.setDrAmount(allocate);
		e1_2201.setCrAmount(BigDecimal.ZERO);
		e1_2201.setSeq(seq);
		document.setDrAmount(document.getDrAmount().add(allocate));
		b_2201 = this.accountBookService.decrCredit(b_2201, allocate);
		seq++;
		entries.add(e1_2201);

		// 贷 所有者权益 3001
		DocumentEntry e2_3001 = new DocumentEntry();
		e2_3001.setOid(StringUtil.uuid());
		e2_3001.setRelative(relative);
		e2_3001.setTicket(ticket);
		e2_3001.setDocument(document);
		e2_3001.setBook(b_3001);
		e2_3001.setDigest(null != maps && maps.containsKey("0070_0001_3001_02") ? maps.get("0070_0001_3001_02").getDigest() : "");
		e2_3001.setDrAmount(BigDecimal.ZERO);
		e2_3001.setCrAmount(allocate);
		e2_3001.setSeq(seq);
		document.setCrAmount(document.getCrAmount().add(allocate));
		b_3001 = this.accountBookService.incrCredit(b_3001, allocate);
		seq++;
		entries.add(e2_3001);

		// 未分配收益缺头寸
		if (wipe.compareTo(BigDecimal.ZERO) > 0) {
			// 借 应收投资收益
			DocumentEntry e3_1201 = new DocumentEntry();
			e3_1201.setOid(StringUtil.uuid());
			e3_1201.setRelative(relative);
			e3_1201.setTicket(ticket);
			e3_1201.setDocument(document);
			e3_1201.setBook(b_1201);
			e3_1201.setDigest(null != maps && maps.containsKey("0070_0001_1201_03") ? maps.get("0070_0001_1201_03").getDigest() : "");
			e3_1201.setDrAmount(wipe);
			e3_1201.setCrAmount(BigDecimal.ZERO);
			e3_1201.setSeq(seq);
			document.setDrAmount(document.getDrAmount().add(wipe));
			b_1201 = this.accountBookService.incrCredit(b_1201, wipe);
			seq++;
			entries.add(e3_1201);

			// 贷 所有者权益
			DocumentEntry e4_3001 = new DocumentEntry();
			e4_3001.setOid(StringUtil.uuid());
			e4_3001.setRelative(relative);
			e4_3001.setTicket(ticket);
			e4_3001.setDocument(document);
			e4_3001.setBook(b_3001);
			e4_3001.setDigest(null != maps && maps.containsKey("0070_0001_3001_04") ? maps.get("0070_0001_3001_04").getDigest() : "");
			e4_3001.setDrAmount(BigDecimal.ZERO);
			e4_3001.setCrAmount(wipe);
			e4_3001.setSeq(seq);
			document.setCrAmount(document.getCrAmount().add(wipe));
			b_3001 = this.accountBookService.incrCredit(b_3001, wipe);
			seq++;
			entries.add(e4_3001);
		}

		document = this.documentDao.save(document);
		for (DocumentEntry e : entries) {
			this.documentEntryService.save(e);
		}

		return document;
	}

}

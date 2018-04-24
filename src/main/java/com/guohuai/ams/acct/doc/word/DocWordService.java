package com.guohuai.ams.acct.doc.word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocWordService {

	@Autowired
	private DocWordDao docWordDao;

	private DocWord dftWord;

	private Map<String, DocWord> wordMap = new HashMap<String, DocWord>();

	private List<DocWord> wordList = new ArrayList<DocWord>();

	@PostConstruct
	public void init() {

		List<DocWord> words = this.docWordDao.findAll();
		if (null != words && words.size() > 0) {
			for (DocWord word : words) {
				this.wordList.add(word);
				this.wordMap.put(word.getOid(), word);
				if (word.getDft().equals(DocWord.DFT_Yes)) {
					this.dftWord = word;
				}
			}

			if (null == this.dftWord) {
				this.dftWord = words.get(0);
			}

		}

	}

	public DocWord get(String oid) {
		return this.wordMap.get(oid);
	}

	public DocWord get() {
		return this.dftWord;
	}
}

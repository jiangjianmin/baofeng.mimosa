package com.guohuai.file;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guohuai.component.exception.AMPException;
import com.guohuai.component.util.StringUtil;
import com.guohuai.component.web.view.Packer;
import com.guohuai.component.web.view.Response;

@Service
public class FileService extends Packer<File> {

	@Autowired
	private FileDao fileDao;

	@Transactional
	public List<File> save(List<SaveFileForm> forms, String fkey, String cate, String operator) {
		List<File> files = this.fileDao.findByFkey(fkey);
		Map<String, File> map = new HashMap<String, File>();
		if (null != files && files.size() > 0) {
			for (File file : files) {
				map.put(file.getOid(), file);
			}
		}

		long current = System.currentTimeMillis();
		String fversion = String.valueOf(current);
		Timestamp now = new Timestamp(current);

		List<File> result = new ArrayList<File>();
		if (null != forms && forms.size() > 0) {
			for (SaveFileForm form : forms) {
				if (!StringUtil.isEmpty(form.getOid()) && map.containsKey(form.getOid())) {
					// 已经有的, 就不用动了
					File file = map.get(form.getOid());
					result.add(file);
					map.remove(form.getOid());
				} else {
					// 存储新的
					File.FileBuilder fb = File.builder().oid(StringUtil.uuid()).fkey(fkey);
					fb.name(form.getName()).furl(form.getFurl()).size(form.getSize()).sizeh(this.getSizeh(form.getSize()));
					fb.cate(cate).state(File.STATE_Valid).fversion(fversion);
					fb.operator(operator).updateTime(now).createTime(now);
					File file = fb.build();
					file = this.fileDao.save(file);
					result.add(file);
				}
			}
		}

		// 逻辑删除已经过期的
		if (map.size() > 0) {
			for (String key : map.keySet()) {
				File file = map.get(key);
				file.setState(File.STATE_Invalid);
				file.setOperator(operator);
				file.setUpdateTime(now);
				this.fileDao.save(file);
			}
		}

		return result;
	}

	@Transactional
	public List<File> merge(List<SaveFileForm> forms, String fkey, String cate, String operator) {
		List<File> files = this.fileDao.findByFkey(fkey);

		Set<String> set = new HashSet<String>();

		if (null != files && files.size() > 0) {
			for (File file : files) {
				set.add(file.getOid());
			}
		}

		long current = System.currentTimeMillis();
		String fversion = String.valueOf(current);
		Timestamp now = new Timestamp(current);

		List<File> result = new ArrayList<File>();
		if (null != forms && forms.size() > 0) {
			for (SaveFileForm form : forms) {
				if (!StringUtil.isEmpty(form.getOid()) && set.contains(form.getOid())) {

				} else {
					// 存储新的
					File.FileBuilder fb = File.builder().oid(StringUtil.uuid()).fkey(fkey);
					fb.name(form.getName()).furl(form.getFurl()).size(form.getSize()).sizeh(this.getSizeh(form.getSize()));
					fb.cate(cate).state(File.STATE_Valid).fversion(fversion);
					fb.operator(operator).updateTime(now).createTime(now);
					File file = fb.build();
					file = this.fileDao.save(file);
					result.add(file);
				}
			}
		}

		return result;
	}

	@Transactional
	public List<File> list(String fkey) {
		List<File> list = this.fileDao.findByFkey(fkey);
		return list;
	}

	@Transactional
	public List<File> list(String fkey, int state) {
		List<File> list = this.fileDao.findByFkeyAndState(fkey, state);
		return list;
	}

	@Transactional
	public List<File> list(String[] fkey, int state) {
		List<File> list = this.fileDao.findByFkeyInAndState(fkey, state);
		return list;
	}

	@Transactional
	public File get(String oid) {
		File file = this.fileDao.findOne(oid);
		if (null == file) {
			throw AMPException.getException("");// .getException(12001);
		}
		return file;
	}

	@Transactional
	public void delete(String oid, String operator) {
		File file = this.get(oid);
		this.delete(file, operator);
	}

	@Transactional
	public void delete(File file, String operator) {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		file.setState(File.STATE_Invalid);
		file.setOperator(operator);
		file.setUpdateTime(now);
		this.fileDao.save(file);
	}

	@Transactional
	public void batchDelete(String fkey, String operator) {
		List<File> list = this.list(fkey);
		if (null != list && list.size() > 0) {
			for (File f : list) {
				this.delete(f, operator);
			}
		}
	}

	private String getSizeh(long size) {
		if (size < 1024L) {
			// 不到1K的, 按字节
			return size + "B";
		} else if (size >= 1024L && size < 1048576L) {
			// 超过1K, 不到1M的, 转化为K, 保留2位小数
			return new BigDecimal(size).divide(new BigDecimal(1024L), 2, RoundingMode.HALF_UP).toString() + "K";
		} else if (size >= 1048576L && size < 1073741824L) {
			// 超过1M, 不到1G的, 转化为M, 保留2位小数
			return new BigDecimal(size).divide(new BigDecimal(1048576L), 2, RoundingMode.HALF_UP).toString() + "M";
		} else {
			// 超过1G的, 按G转化
			return new BigDecimal(size).divide(new BigDecimal(1073741824L), 2, RoundingMode.HALF_UP).toString() + "G";
		}
	}

	@Override
	public Response doPack(File t) {
		Response r = new Response(false).with("oid", t).with("name", t.getName()).with("furl", t.getFurl()).with("size", t.getSize()).with("sizeh", t.getSizeh());
		return r;
	}
}

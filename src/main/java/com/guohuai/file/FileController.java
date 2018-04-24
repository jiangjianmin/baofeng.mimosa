package com.guohuai.file;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.guohuai.basic.common.StringUtil;
import com.guohuai.basic.component.ext.web.BaseController;
import com.guohuai.component.codec.Digests;
import com.guohuai.component.exception.AMPException;
import com.guohuai.component.web.view.Response;
import com.guohuai.file.FileDownloadReq.DownloadFile;
import com.guohuai.file.FileDownloadReq.DownloadFkey;

import lombok.Data;

@RestController
@RequestMapping(value = "/mimosa/file", produces = "application/json;charset=utf-8")
public class FileController extends BaseController {

	@Autowired
	private FileService fileService;

	@Autowired
	private RedisTemplate<String, String> redis;

	@RequestMapping(value = "/list", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<Response> list(@RequestParam String fkey, @RequestParam(defaultValue = "1") int state) {
		List<File> list = this.fileService.list(fkey, state);
		Response r = new Response().with("total", list.size()).with("rows", this.fileService.doPack(list));
		return new ResponseEntity<Response>(r, HttpStatus.OK);
	}

	@RequestMapping(value = "/pkg", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<FileDLResp> download(@RequestBody FileDownloadReq form) throws NoSuchAlgorithmException {

		List<DLFile> files = new ArrayList<DLFile>();
		Map<String, Integer> upname = new HashMap<String, Integer>();

		if (null != form.getFkeys() && form.getFkeys().size() > 0) {
			Map<String, String> cates = new HashMap<String, String>();
			for (DownloadFkey fkey : form.getFkeys()) {
				if (!StringUtil.isEmpty(fkey.getFkey())) {
					if (cates.containsKey(fkey.getFkey()) && !StringUtil.isEmpty(cates.get(fkey.getFkey()))) {
						continue;
					} else {
						cates.put(fkey.getFkey(), fkey.getCate());
					}
				}
			}
			List<File> result = this.fileService.list(cates.keySet().toArray(new String[0]), File.STATE_Valid);
			if (null != result && result.size() > 0) {
				for (File f : result) {
					DLFile dl = new DLFile();
					dl.setSize(f.getSize());
					dl.setUrl((f.getFurl().indexOf(0) == '/' ? "" : "/") + f.getFurl());
					String name = f.getName();
					if (StringUtil.isEmpty(name)) {
						name = this.linkToName(f.getFurl());
					}
					name = name.replace(' ', '_');
					if (cates.containsKey(f.getFkey()) && !StringUtil.isEmpty(cates.get(f.getFkey()))) {
						name = cates.get(f.getFkey()) + "_" + name;
					}
					if (upname.containsKey(name)) {
						upname.put(name, upname.get(name) + 1);
						name = this.upName(name, upname.get(name));
					} else {
						upname.put(name, 1);
					}
					dl.setName(name);
					files.add(dl);
				}
			}
		}

		if (null != form.getFiles() && form.getFiles().size() > 0) {
			for (DownloadFile f : form.getFiles()) {
				DLFile dl = new DLFile();
				dl.setSize(f.getSize());
				dl.setUrl((f.getFurl().indexOf(0) == '/' ? "" : "/") + f.getFurl());
				String name = f.getName();
				if (StringUtil.isEmpty(name)) {
					name = this.linkToName(f.getFurl());
				}
				name = name.replace(' ', '_');
				if (upname.containsKey(name)) {
					upname.put(name, upname.get(name) + 1);
					name = this.upName(name, upname.get(name));
				} else {
					upname.put(name, 1);
				}
				dl.setName(name);
				files.add(dl);
			}
		}

		if (files.size() > 0) {

			StringBuffer buffer = new StringBuffer();
			for (DLFile file : files) {
				buffer.append(file.toString());
			}

			final String value = buffer.toString();

			byte[] bytes = value.getBytes();
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(bytes);
			byte[] cipher = md.digest();
			final String key = Digests.encodeBase64(cipher);

			this.redis.execute(new RedisCallback<Void>() {

				@Override
				public Void doInRedis(RedisConnection connection) throws DataAccessException {
					connection.setEx(("AMP:ZIP:" + key).getBytes(Charset.forName("utf-8")), 300, value.getBytes(Charset.forName("utf-8")));
					return null;
				}
			});
			return new ResponseEntity<FileDLResp>(new FileDLResp(key), HttpStatus.OK);
		} else {

			throw new AMPException("No file found.");
		}
	}

	@RequestMapping(value = "/dl", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody ResponseEntity<String> download(@RequestParam final String key, @RequestParam(required = false) String filename, @RequestParam(defaultValue = "false") boolean develop)
			throws UnsupportedEncodingException {
		if (StringUtil.isEmpty(key)) {
			throw new AMPException("Param error.");
		}

		if (StringUtil.isEmpty(filename)) {
			filename = "国槐金融";
		} else {
			filename = filename.trim();
		}

		filename = URLEncoder.encode(filename.replaceAll("[\\\\/:*?\"<>|]", "_"), "utf-8");

		String value = this.redis.execute(new RedisCallback<String>() {

			@Override
			public String doInRedis(RedisConnection connection) throws DataAccessException {
				byte[] bytes = connection.get(("AMP:ZIP:" + key.replace(' ', '+')).getBytes(Charset.forName("utf-8")));
				if (null != bytes && bytes.length > 0) {
					return new String(bytes, Charset.forName("utf-8"));
				}
				return null;
			}
		});

		if (StringUtil.isEmpty(value)) {
			throw new AMPException("No key found or empty file list.");
		}

		if (!develop) {
			super.response.setHeader("X-Archive-Files", "zip");
			super.response.setHeader("Content-Disposition", String.format("attachment; filename=%s.zip", filename));
		}
		return new ResponseEntity<String>(value, HttpStatus.OK);
	}

	private String linkToName(String url) {
		if (null == url || url.trim().equals(""))
			return "UNNAMED";
		System.out.println(url.lastIndexOf("/"));
		if (url.lastIndexOf("/") != -1) {
			return url.substring(url.lastIndexOf("/") + 1);
		}
		return url;
	}

	private String upName(String name, int sn) {
		String file = "", suffix = "";
		if (name.lastIndexOf(".") != -1) {
			file = name.substring(0, name.lastIndexOf("."));
			suffix = name.substring(name.lastIndexOf("."));
		} else {
			file = name;
		}

		return file + "_" + sn + suffix;
	}

	@Data
	private class DLFile {
		private long size;
		private String url;
		private String name;

		@Override
		public String toString() {
			return String.format("- %s %s %s\n", this.size, this.url, this.name);
		}
	}

}

package com.guohuai.file;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FileDao extends JpaRepository<File, String>, JpaSpecificationExecutor<File> {

	public List<File> findByFkey(String fkey);

	public List<File> findByFkeyAndState(String fkey, int state);

	public List<File> findByFkeyInAndState(String[] fkey, int state);

}

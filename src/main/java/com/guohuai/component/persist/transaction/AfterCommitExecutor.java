package com.guohuai.component.persist.transaction;

import java.util.concurrent.Executor;

import org.springframework.stereotype.Service;
@Service
public interface AfterCommitExecutor extends Executor {
}
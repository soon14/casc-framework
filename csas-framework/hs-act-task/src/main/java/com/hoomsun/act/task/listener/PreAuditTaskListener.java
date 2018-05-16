/**
 * Copyright www.hoomsun.com 红上金融信息服务（上海）有限公司
 */
package com.hoomsun.act.task.listener;

import org.activiti.engine.HistoryService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.history.HistoricTaskInstance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 作者：ywzou <br>
 * 创建时间：2018年1月18日 <br>
 * 描述：初审任务监听 穿件后 create
 */
@Component("preAuditTaskListener")
public class PreAuditTaskListener implements TaskListener {
	private static final long serialVersionUID = -2094172289893862063L;

	@Autowired
	private HistoryService historyService;

	@Override
	public void notify(DelegateTask task) {

		String applyId = task.getExecution().getProcessBusinessKey();
		if (StringUtils.isNotBlank(applyId)) {
			StringBuilder sql = new StringBuilder("select ASSIGNEE_ from ( ");
			sql.append("SELECT hi.ASSIGNEE_,hi.END_TIME_ END_TIME_ FROM ACT_HI_TASKINST hi ");
			sql.append("LEFT JOIN ACT_HI_PROCINST hp on hi.PROC_INST_ID_ = hp.ID_ ");
			sql.append("WHERE hp.BUSINESS_KEY_=#{applyId} and hi.ASSIGNEE_ is not null and hi.END_TIME_ is not null ");
			sql.append("and hp.ID_=#{procInstId} and hi.TASK_DEF_KEY_='preAudit' ");
			sql.append("order by END_TIME_ desc ) where rownum=1");
			HistoricTaskInstance historicTaskInstance = historyService.createNativeHistoricTaskInstanceQuery().sql(sql.toString()).parameter("applyId", applyId).parameter("procInstId", task.getProcessInstanceId()).singleResult();

			if (historicTaskInstance != null) {
				String assignee = historicTaskInstance.getAssignee();
				if (!StringUtils.isBlank(assignee)) {
					task.setAssignee(assignee);
				}
			}
		}
	}

}
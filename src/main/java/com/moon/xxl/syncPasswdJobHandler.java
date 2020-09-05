package com.moon.xxl;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.apache.ibatis.session.SqlSession;



import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class syncPasswdJobHandler extends IJobHandler {

	@Override
	public ReturnT<String> execute(String param) throws Exception {
		SqlSession session=DBUtil.getSqlSessionFactory().openSession();
		List<HashMap> results=session.selectList("com.moon.mybatis.sms.ipMapping",1);
		results.forEach(x->x.forEach((k, v) -> System.out.println("key:value = " + k + ":" + v)));
		session.close();
		return SUCCESS;
	}
}

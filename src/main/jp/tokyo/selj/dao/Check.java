/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package jp.tokyo.selj.dao;

import java.io.Serializable;
import java.sql.Timestamp;

import org.seasar.dao.annotation.tiger.Bean;

@Bean(table = "checktbl")
public class Check implements Serializable {
    private static final long serialVersionUID = 1L;

    private long	outputId;
    private Timestamp checkDate=new Timestamp(System.currentTimeMillis());
    private String   userName;
    private String remark;
    private int   versionNo;
    
    
	public Timestamp getCheckDate() {
		return checkDate;
	}
	public void setCheckDate(Timestamp checkDate) {
		this.checkDate = checkDate;
	}
	public long getOutputId() {
		return outputId;
	}
	public void setOutputId(long outputId) {
		this.outputId = outputId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getVersionNo() {
		return versionNo;
	}
	public void setVersionNo(int versionNo) {
		this.versionNo = versionNo;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}

    
}
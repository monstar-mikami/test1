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

import jp.tokyo.selj.common.SysPreference;

import org.seasar.dao.annotation.tiger.Bean;

@Bean(table = "docStr")
public class DocStr implements Serializable {

    private long oyaDocId;
    private long koDocId;
    private int SEQ;
    private Timestamp newDate=new Timestamp(System.currentTimeMillis());
    private String userName;
//    private Youken �e�v��;
//    private Youken �q�v��;

    public DocStr() {
		userName = SysPreference.getDefaultUserName();
    }
    public DocStr(long parentId, long childId) {
    	this();
    	oyaDocId = parentId;
    	koDocId = childId;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(oyaDocId).append(", ");
        buf.append(koDocId).append(", ");
        buf.append(SEQ).append(", ");
        buf.append(newDate).append(", ");
        buf.append(userName);
        return buf.toString();
    }

    public int hashCode() {
        return (int) this.getOyaDocId();
    }

	public int getSEQ() {
		return SEQ;
	}

	public void setSEQ(int seq) {
		this.SEQ = seq;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Timestamp getNewDate() {
		return newDate;
	}

	public void setNewDate(Timestamp newDate) {
		this.newDate = newDate;
	}

	public long getKoDocId() {
		return koDocId;
	}

	public void setKoDocId(long koDocId) {
		this.koDocId = koDocId;
	}

	public long getOyaDocId() {
		return oyaDocId;
	}

	public void setOyaDocId(long oyaDocId) {
		this.oyaDocId = oyaDocId;
	}

//	public Youken get�q�v��() {
//		return �q�v��;
//	}
//
//    @Relation(relationNo = 0, relationKey="�q�v��ID:�v��ID")
//	public void set�q�v��(Youken �q�v��) {
//		this.�q�v�� = �q�v��;
//	}
//
//	public Youken get�e�v��() {
//		return �e�v��;
//	}
//
//    @Relation(relationNo = 1, relationKey="�e�v��ID:�v��ID")
//	public void set�e�v��(Youken �e�v��) {
//		this.�e�v�� = �e�v��;
//	}

}
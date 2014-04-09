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
package jp.tokyo.selj.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.TransactionManager;

import jp.tokyo.selj.dao.SortType;
import jp.tokyo.selj.dao.SortTypeDao;
import jp.tokyo.selj.dao.User;
import jp.tokyo.selj.dao.UserDao;
import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.dao.DocDao;
import jp.tokyo.selj.dao.DocStr;
import jp.tokyo.selj.dao.DocStrDao;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.log.Logger;

public class Migrate {

    private static Logger logger = Logger.getLogger(Migrate.class);
    static Map<Long, Long> idMap = new HashMap<Long, Long>();

    public static void main(String[] args) throws Exception{
        S2Container container = S2ContainerFactory.create("jp/tokyo/selj/dao/selDao.dicon");
        container.init();
    	TransactionManager trn = (TransactionManager)container.getComponent(TransactionManager.class);
        try {
        	trn.begin();
        	//�v��
        	migrateYouken(container);
        	
        	//�v���\��
        	migrateYoukenKouzou(container);
        	
        	//�\�[�g���
//        	migrateSortType(container);

        	//��Ǝ�
        	migrateUser(container);

        	
            trn.commit();
            System.out.println("@@@@ finish @@@@");
        }catch(Exception e){
        	trn.rollback();
        	e.printStackTrace();
        } finally {
            container.destroy();
        }

    }
    
	private static Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        Connection con = DriverManager.getConnection("jdbc:odbc:selmig");
		return con;
	}

	static void migrateYouken(S2Container container) throws Exception{
    	DocDao dao = (DocDao) container
        .getComponent(DocDao.class);
        Connection con = getConnection();
		try{
			Statement st = con.createStatement();
			dao.deleteAll();
			ResultSet rs = st.executeQuery("select * from �v��");
			while(rs.next()){
				Doc out = new Doc();
				Number n = (Number)rs.getObject("�v��ID");
				long oldId = n.longValue();
				out.setDocId( oldId ); 
				n = (Number)rs.getObject("�v�����ID");
				out.setDocTypeId( (n==null)? 0:n.intValue() );
				out.setDocTitle(rs.getString("�v���^�C�g��"));
				out.setDocCont(rs.getString("�v�����e"));
				out.setNewDate(rs.getTimestamp("�쐬��"));
				out.setUserName(rs.getString("�쐬�Җ�"));
				n = (Number)rs.getObject("�q�m�[�h����");
				out.setSortTypeId( (n==null)? 0:n.intValue() );
				dao.insert(out);
				logger.info(out);
				idMap.put(oldId, out.getDocId());
			}
		}finally{
			con.close();
		}
    }
    
	
    static void migrateYoukenKouzou(S2Container container) throws Exception{
    	DocStrDao dao = (DocStrDao) container
    		.getComponent(DocStrDao.class);
        Connection con = getConnection();
		try{
			Statement st = con.createStatement();
			dao.deleteAll();
			ResultSet rs = st.executeQuery("select * from �v���\��");
			while(rs.next()){
				DocStr out = new DocStr();
				Number n = (Number)rs.getObject("�e�v��ID");
				logger.debug("oldId="+n.longValue());
				out.setOyaDocId( idMap.get(n.longValue()) ); 

				n = (Number)rs.getObject("�q�v��ID");
				out.setKoDocId( idMap.get(n.longValue()) ); 
				
				n = (Number)rs.getObject("SEQ");
				out.setSEQ( n.intValue() ); 

				out.setNewDate(rs.getTimestamp("�쐬��"));
				out.setUserName(rs.getString("�쐬��"));

				logger.info(out);
				dao.insert(out);
			}
		}finally{
			con.close();
		}
    }

    static void migrateSortType(S2Container container) throws Exception{
    	SortTypeDao dao = (SortTypeDao) container
    		.getComponent(SortTypeDao.class);
        Connection con = getConnection();
		try{
			Statement st = con.createStatement();
			dao.deleteAll();
			ResultSet rs = st.executeQuery("select * from �\�[�g���");
			while(rs.next()){
				SortType out = new SortType();
				Number n = (Number)rs.getObject("�\�[�g���ID");
				out.setSortTypeID( n.intValue() ); 
				out.setSortTypeName(rs.getString("�\�[�g��ʖ���"));
				out.setOrderSent(rs.getString("Order��"));
				logger.info(out);
				dao.insert(out);
			}
		}finally{
			con.close();
		}
    }

    static void migrateUser(S2Container container) throws Exception{
    	UserDao dao = (UserDao) container
    		.getComponent(UserDao.class);
        Connection con = getConnection();
		try{
			Statement st = con.createStatement();
			dao.deleteAll();
			ResultSet rs = st.executeQuery("select * from ��Ǝ�");
			while(rs.next()){
				User out = new User();
				out.setUserName(rs.getString("��ƎҖ�"));
				out.setOrg(rs.getString("����"));
				logger.info(out);
				dao.insert(out);
			}
		}finally{
			con.close();
		}
    }
    
}
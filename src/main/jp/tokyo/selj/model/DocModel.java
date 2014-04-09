package jp.tokyo.selj.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.transaction.TransactionManager;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.dao.DocDao;
import jp.tokyo.selj.dao.DocStr;
import jp.tokyo.selj.dao.DocStrDao;
import jp.tokyo.selj.dao.DocStrSearchCondition;
import jp.tokyo.selj.dao.SelJDaoContainer;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;

public class DocModel extends DefaultTreeModel{
	Logger log = Logger.getLogger(this.getClass());
	
	public static class DoNothingException extends RuntimeException{
		DocNode node_;
		DoNothingException(DocNode nd){
			node_ = nd;
		}
		public DocNode getDocNode(){
			return node_;
		}
	}
	
	
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;
	DocDao docDao_ = null;
	DocStrDao docStrDao_ = null;
	TransactionManager trn_ = null;
	Comparator docComparator_ =
		new Comparator<Doc>(){
			public int compare(Doc o1, Doc o2) {
				return (int)(o1.getDocId() - o2.getDocId());
			}
		};

	static final String ROOT_TITLE = "�i�V�K�f�[�^�j";
	
	public static interface NodeProcessor{
		/** @return true-���s�Afalse-��~ */
		public boolean process(DocNode node);
	}
	public static interface DocProcessor{
		/** @return true-���s�Afalse-��~ */
		public boolean process(Doc doc, List<Doc> parents);
	}
	public DocModel(){
		super(null);
		docDao_ = (DocDao) daoCont_.getComponent(DocDao.class);
		docStrDao_ = (DocStrDao) daoCont_.getComponent(DocStrDao.class);
    	trn_ = (TransactionManager)daoCont_.getComponent(TransactionManager.class);
    }
	public DocStr getDocStr(long parent, long child){
		return docStrDao_.find(parent, child);
	}	
	public void initializeReverseModel(DocNode curNode) {
		DocNode node = new DocNode(curNode.getDoc());	//�f��Node���쐬���ɂႠ����
		
		//�e�v����S�Ēǉ�
//		DocNode top = addAllParentDocFromDb(node);
		
		//�q�v����ǉ�
		DocNode top = addParentDocFromDb(node);
		
		//���v����ǉ�
		top = addJijiDocFromDb(top);
		
		setRoot(top);
		reload(top);
		
	}
/*
	//0.9.04�ȑO�ɋt�c���[�\���Ɏg�p���Ă���
	public DocNode addAllParentDocFromDb(DocNode node){
		log.trace("start");
		if(node == null){
			throw new IllegalArgumentException("node is null.");
		}
		
		DocDao docDao = (DocDao) daoCont_.getComponent(DocDao.class);
		DocStrDao docStrDao = (DocStrDao) daoCont_.getComponent(DocStrDao.class);

		Doc doc = node.getDoc();
		List<DocStr> parents = docStrDao.findOyaDocStr(
					new DocStrSearchCondition(doc.getDocId(),
							(doc.getSortType()==null)? null:doc.getSortType().getOrderSent())
				);

//		if(parents.isEmpty()){
//			return node;
//		}
//		long[] ids = new long[parents.size()];
//		for(int i=0;i < parents.size() ; i++){
//			ids[i] = parents.get(i).getOyaDocId();
//		}
//		List<Doc> oyas = docDao.findByDocIds(ids);
//		for(int i=0;i < oyas.size() ; i++){
//			DocNode newDocNode = new DocNode(oyas.get(i));
//			newDocNode.resetParentCount(docStrDao_);
//			
//			addAllParentDocFromDb(newDocNode);
//			
//			node.add(newDocNode);
//		}
		
		for(int i=0;i < parents.size() ; i++){
			DocStr docStr = parents.get(i);
			
			//���F�v���\�����擾����q�v���E�e�v���́ASotrType���ǂݍ��܂�Ă��Ȃ��I
			Doc oyaDoc = docDao.findByDocId(docStr.getOyaDocId());
			
			DocNode newDocNode = new DocNode(oyaDoc);
			newDocNode.resetParentCount(docStrDao_);
			
			addAllParentDocFromDb(newDocNode);
			
			node.add(newDocNode);
		}
		log.trace("end");
		return node;
	}
*/
	
	
	public void initialize() {
		//root�v����ǉ�
		Doc root;
		List roots = docDao_.findByDocTypeId(Doc.ROOT_TYPE);
		if( roots.size() <= 0 ){	//���[�g�Ȃ��i�V�K�쐬���j
			//��UDB�ɏ�������ōēǂݍ���
			Doc tempRoot = new Doc();
			tempRoot.setDocTypeId(Doc.ROOT_TYPE);
			tempRoot.setDocTitle(ROOT_TITLE);
			tempRoot.setDocCont("created at "+new Date());
			docDao_.insert(tempRoot);
			roots = docDao_.findByDocTypeId(Doc.ROOT_TYPE);
		}
		root = (Doc)roots.get(0);
		//s2dao�́A�֘A�e�[�u���P���̃����N����join���Ȃ����߁A������Doc�������[�h
		root = docDao_.findByDocId(root.getDocId());
		DocNode node = new DocNode(root);
		
		//�q�v����ǉ�
		DocNode top = addChildDocFromDb(node);
		
		//���v����ǉ�
		top = addMagoDocFromDb(top);
		
		setRoot(top);
	}
	//TreeNode���doc�ƈ�v����m�[�h��S��reload����
	public void docChanged(Doc doc){
		log.trace("start");
		class Reloader implements NodeProcessor{
			Doc doc_;
			int parents_ = -1;
			public Reloader(Doc doc){
				doc_ = doc;
			}
			public boolean process(DocNode targetNode){
				Doc target = targetNode.getDoc();
				if( doc_.getDocId() == target.getDocId()){
					target.copyDoc(doc_);
					//�e�̐��������œ���Ă���
					if( parents_ == -1){
						targetNode.resetParentCount(docStrDao_);
						parents_ = targetNode.getParentCount();
					}else{
						targetNode.setParentCount(parents_);
					}
					nodeChanged(targetNode);
				}
				return true;	//���s
			}
		}
		processAllNode(new Reloader(doc));
		log.trace("end");
	}
	
	public DocNode searchFirstDocNode(long id){
		log.trace("start");
		class Searcher implements NodeProcessor{
			long id_;
			DocNode node_;
			public Searcher(long id){
				id_ = id;
			}
			public boolean process(DocNode node){
				Doc target = node.getDoc();
				if( id_ == target.getDocId()){
					node_ = node;
					return false;	//�I��
				}
				return true;	//���s
			}
		}
		Searcher proc = new Searcher(id);
		processAllNode(proc);
		log.trace("end");
		return proc.node_;
	}
	public List<Long> getPathTo(DocNode startNode, long id){
		List<Long> path = new ArrayList<Long>();
		path = getPathTo(path, startNode.getDoc(), id);
		if( path != null){
			path.add(0, startNode.getDoc().getDocId());
		}else{
			throw new AppException("�J�����g�m�[�h���� id=" + id 
					+" �֎���p�X���A���̃Z�b�V�����ɂ���ĕύX����Ă��܂��B");
		}
		return path;
	}
	public List<Long> getPathTo(List<Long> path, Doc startDoc, long id){
		List<DocStr> children = docStrDao_.findByOyaDocId(startDoc.getDocId());
		if( children != null){
			for(DocStr docStr: children){
				if(docStr.getKoDocId() == id){
					path.add(id);
					return path;
				}
			}
			//������Ȃ������ꍇ�q��������
			for(DocStr docStr: children){
				Doc doc = docDao_.findByDocId(docStr.getKoDocId());
				List<Long> ret = getPathTo( path, doc, id);
				if(ret != null){
					path.add(0, doc.getDocId());
					return path;
				}
			}
		}
		return null;
	}	
	public List<Long> getPathToDocRoot(long id){
		log.trace("start");
		List<Long> path = new ArrayList<Long>();
		path = getPathToDocRoot(path, id);
		
		//�擪��Root�^�C�v����������
		if( path.size() <= 0 ){
			throw new RuntimeException("getPathToDocRoot()�̃G���g����0");
		}else{
			Doc doc = docDao_.findByDocId(path.get(0));
			if( doc == null ){
				throw new AppException("id=" + path.get(0) 
						+" �́A���̃Z�b�V�����ɂ���Ċ��ɍ폜����Ă��܂��B");
			}else{
				if( doc.getDocTypeId() != Doc.ROOT_TYPE){
					throw new AppException("id=" + id 
							+" �֎���p�X���A���̃Z�b�V�����ɂ���ĕύX����Ă��܂��B");
				}
			}
		}
		log.trace("end");
		return path;
	}
	public List<Long> getPathToDocRoot(List<Long> path, long id){
		List<DocStr> parents = docStrDao_.findByKoDocId(id);
		
		if(parents.size() > 0){
			//��Ԑ�Path��I������
			int size = Integer.MAX_VALUE;
			List<Long> miniPath = new ArrayList<Long>();
			for(int i=0; i<parents.size(); i++){
				List<Long> temp = new ArrayList<Long>(path);
				temp = getPathToDocRoot(temp, parents.get(i).getOyaDocId());
				if( size > temp.size() ){
					miniPath.clear();
					miniPath.addAll(temp);
					size = temp.size();
				}
			}
			path.clear();
			path.addAll(miniPath);
		}
		path.add(id);
		return path;
	}	
	
	//root�z���̑S�m�[�h�ɑ΂�Leaf����NodeProcessor���s��
	public void processAllNode(NodeProcessor procNode){
		DocNode node = (DocNode)getRoot();
		processAllNode(node, procNode);
	}
	//�w��node�z���̑S�m�[�h�ɑ΂�Leaf����NodeProcessor���s��
	public boolean processAllNode(DocNode node, NodeProcessor procNode){
		Enumeration children = node.children();
		while(children.hasMoreElements()){
			DocNode child = (DocNode)children.nextElement();
			if( ! processAllNode(child, procNode) ){
				break;
			}
		}
		return procNode.process(node);
	}
	
	//�w��node�z���̑S�m�[�h�ɑ΂��}����NodeProcessor���s��
	public boolean processAllNode2(DocNode node, NodeProcessor procNode){
		if( ! procNode.process(node) ){
			return false;
		}
		boolean ret = true;
		Enumeration children = node.children();
		while(children.hasMoreElements()){
			DocNode child = (DocNode)children.nextElement();
			processAllNode2(child, procNode);
			if( ! ret ){
				break;
			}
		}
		return ret;
	}
	//DB��̎w��doc�z���̑SDoc�ɑ΂��}����DocProcessor���s��
	public void processAllDoc2(Doc startDoc, DocProcessor procDoc, List<Doc> parents){
		log.trace("start");
		if( ! procDoc.process(startDoc, parents) ){
			return;		//�q���͏������Ȃ�
		}

		//get children
		List<DocStr> children = docStrDao_.findDocStr(
			new DocStrSearchCondition(startDoc.getDocId(),
				(startDoc.getSortType()==null)? null:startDoc.getSortType().getOrderSent())
		);
		List<Doc> tempParents = new ArrayList<Doc>(parents); 
		tempParents.add(startDoc);
		for(int i=0;i < children.size() ; i++){
			DocStr docStr = children.get(i);
			
			//���F�v���\�����擾����q�v���E�e�v���́ASotrType���ǂݍ��܂�Ă��Ȃ��I
			Doc doc = docDao_.findByDocId(docStr.getKoDocId());
			processAllDoc2(doc, procDoc, tempParents);
		}
		log.trace("end");
//		return oyaNode;
	}


	public DocNode addMagoDocFromDb(DocNode node){
		log.trace("start");
		Enumeration children =node.children();
		while(children.hasMoreElements()){
			DocNode child = (DocNode)children.nextElement();
			child = addChildDocFromDb(child);
		}
		log.trace("end");
		return node;
	}
	//�t�c���[�p
	public DocNode addJijiDocFromDb(DocNode node){
		log.trace("start");
		Enumeration parents =node.children();
		while(parents.hasMoreElements()){
			DocNode parent = (DocNode)parents.nextElement();
			parent = addParentDocFromDb(parent);
		}
		log.trace("end");
		return node;
	}
	public DocNode addChildDocFromDb(DocNode oyaNode){
		log.trace("start");
//		long t = System.currentTimeMillis();
		
		if(oyaNode == null){
			throw new IllegalArgumentException("node is null.");
		}
		if(oyaNode.getDoc() == null){
			throw new IllegalArgumentException("node.getDoc() is null.");
		}
		if(oyaNode.getChildCount() > 0){	//���łɎq��������ꍇ�́A�ǉ����Ȃ�
			return oyaNode;
		}
		
//		DocDao docDao = (DocDao) daoCont_.getComponent(DocDao.class);
//		DocStrDao docStrDao = (DocStrDao) daoCont_.getComponent(DocStrDao.class);
		Doc oya = oyaNode.getDoc();

//		log.debug("oya="+oya);	
		List<DocStr> children = docStrDao_.findDocStr(
					new DocStrSearchCondition(oya.getDocId(),
							(oya.getSortType()==null)? null:oya.getSortType().getOrderSent())
				);
		
//		if(children.isEmpty()){
//			return oyaNode;
//		}
//		long[] ids = new long[children.size()];
//		for(int i=0;i < children.size() ; i++){
//			ids[i] = children.get(i).getKoDocId();
//		}
//		DocDao docDao = (DocDao) daoCont_.getComponent(DocDao.class);
//		List<Doc> kos = docDao.findByDocIds(ids);
//
//		for(Doc doc: kos){
//			DocNode newDocNode = new DocNode(doc);
//			newDocNode.resetParentCount(docStrDao_);
//			oyaNode.add(newDocNode);
//		}
		
		for(int i=0;i < children.size() ; i++){
			DocStr yk = children.get(i);
			
			//���F�v���\�����擾����q�v���E�e�v���́ASotrType���ǂݍ��܂�Ă��Ȃ��I
			Doc doc = docDao_.findByDocId(yk.getKoDocId());
			DocNode newDocNode = new DocNode(doc);
			newDocNode.resetParentCount(docStrDao_);
			oyaNode.add(newDocNode);
		}
//		log.debug(" time = " + (System.currentTimeMillis() - t));
		log.trace("end");
		return oyaNode;
	}
	//�t�c���[�p
	public DocNode addParentDocFromDb(DocNode koNode){
		log.trace("start");
		
		if(koNode == null){
			throw new IllegalArgumentException("node is null.");
		}
		if(koNode.getDoc() == null){
			throw new IllegalArgumentException("node.getDoc() is null.");
		}
		if(koNode.getChildCount() > 0){	//���łɐe�i���ۂ͎q���j������ꍇ�́A�ǉ����Ȃ�
			return koNode;
		}
		
		Doc ko = koNode.getDoc();

		List<DocStr> parents = docStrDao_.findOyaDocStr(
					new DocStrSearchCondition(ko.getDocId(),
							(ko.getSortType()==null)? null:ko.getSortType().getOrderSent())
				);
		
		for(int i=0;i < parents.size() ; i++){
			DocStr yk = parents.get(i);
			
			//���F�v���\�����擾����q�v���E�e�v���́ASotrType���ǂݍ��܂�Ă��Ȃ��I
			Doc doc = docDao_.findByDocId(yk.getOyaDocId());
			DocNode newDocNode = new DocNode(doc);
			newDocNode.resetParentCount(docStrDao_);
			koNode.add(newDocNode);
		}
		log.trace("end");
		return koNode;
	}
	// expectNode��id�ƈ�v����AexpectNode�ȊO�̃m�[�h��ǂݍ��݂Ȃ���
	void refreshChildrenFromDb(DocNode expectNode){
		long id = expectNode.getDoc().getDocId();
		log.trace("start id="+id);
		class Inserter implements NodeProcessor{
			DocNode expectNode_;
			long oyaNodeId_;
			public Inserter(long oyaNodeId, DocNode expectNode){
				oyaNodeId_ = oyaNodeId;
				expectNode_ = expectNode;
			}
			public boolean process(DocNode node){
				if(expectNode_ == node){
					return true;
				}
				Doc nodeDoc = node.getDoc();
				if( oyaNodeId_ == nodeDoc.getDocId()){
					node.removeAllChildren();
					reload(node);
					//�q�v����ǉ�
					addChildDocFromDb(node);
					//���v����ǉ�
					addMagoDocFromDb(node);
				}
				return true;	//���s
			}
		}
		Inserter proc = new Inserter(id, expectNode);
		processAllNode(proc);
		log.trace("end");
	}
	// id�ƈ�v����A�m�[�h�̐e����count�ɍX�V����
	void refreshParentCount(long id, int count){
		log.trace("start id="+id);
		class ParentCountSetter implements NodeProcessor{
			long targetId_;
			int count_;
			public ParentCountSetter(long id, int count){
				targetId_ = id;
				count_ = count;
			}
			public boolean process(DocNode node){
				Doc nodeDoc = node.getDoc();
				if( targetId_ == nodeDoc.getDocId()){
					node.setParentCount(count_);
				}
				return true;	//���s
			}
		}
		ParentCountSetter proc = new ParentCountSetter(id, count);
		processAllNode(proc);
		log.trace("end");
	}
	// id�ƈ�v����A�m�[�h�̍�Ɛ���count�ɍX�V����
	public void refreshWorkCount(long id, int count){
		log.trace("start id="+id);
		class WorkParentCountSetter implements NodeProcessor{
			long targetId_;
			int count_;
			public WorkParentCountSetter(long id, int count){
				targetId_ = id;
				count_ = count;
			}
			public boolean process(DocNode node){
				Doc nodeDoc = node.getDoc();
				if( targetId_ == nodeDoc.getDocId()){
					nodeDoc.setWorkCount(count_);
				}
				return true;	//���s
			}
		}
		WorkParentCountSetter proc = new WorkParentCountSetter(id, count);
		processAllNode(proc);
		log.trace("end");
	}
		
	public DocNode insertDocFromAnotherProcessTrns(DocNode oyaNode, DocNode node, boolean copyCopyData)
		throws AppException{
		
		log.trace("start");
		boolean roolbackFlag = false;
		try{
			trn_.begin();
			Map<Long,Long> idMap = new HashMap<Long,Long>();
			DocNode newDocNode = null;
//			try{
				newDocNode = insertDocFromAnotherProcess2(idMap, oyaNode, node, copyCopyData);
//			}catch(DoNothingException e){
				//���ɑ��݂���m�[�h�̏ꍇ�́A���̗�O���X���[�����̂Ŗ�������B
//				newDocNode = node;
//			}
			trn_.commit();
			log.trace("end");
			return newDocNode;
		}catch(AppException e){
			roolbackFlag = true;
			throw e;
		}catch(Throwable e){
			roolbackFlag = true;
			throw new RuntimeException(e);
		}finally{
			if(roolbackFlag){
				try {
					trn_.rollback();
				} catch (Exception e1) {
					//�����̗�O�͖���
				}
			}
		}
	}
	DocNode insertDocFromAnotherProcess2(Map<Long,Long> idMap, DocNode oyaNode, DocNode impNode, boolean copyCopyData)
		throws AppException{
		
		Doc impDoc = impNode.getDoc();
		impDoc.setDocTypeId(0);	//���[�g�̉\��������̂ŃN���A
		boolean isInsertedDoc = false;
    	long impDocId = impDoc.getDocId();
    	if( idMap.containsKey(impDocId)){
    		impDoc.setDocId(idMap.get(impDocId));
    		isInsertedDoc = true;
    	}else{
    		if(copyCopyData){
        		impDoc = setExistDocId(impDoc);
    		}else{
    			impDoc.setDocId(-1);
    		}
    	}
    	DocNode newDocNode = impNode;
    	try{
    		newDocNode = insertDoc2(oyaNode, impDoc);
		}catch(DoNothingException e){
			//���ɑ��݂���m�[�h�̏ꍇ�́A���̗�O���X���[�����̂Ŗ�������B
    		if(copyCopyData){
        		//CopyData�o�b�`�ŃR�s�[���ĕҏW��ɍēx�I���W�i��Zeeta�փR�s�[�����ꍇ
    		}else{
    			throw e;
    		}
		}

		idMap.put(impDocId, newDocNode.getDoc().getDocId());
		
		if( isInsertedDoc ){	//��U�ǉ��ς݂�Doc�̏ꍇ�A�q����ǉ�����K�v�͂Ȃ�
			return newDocNode;
		}

		Enumeration<DocNode> children = impNode.children();
    	while(children.hasMoreElements()){
    		DocNode child = children.nextElement();
    		insertDocFromAnotherProcess2(idMap, newDocNode, child, copyCopyData);
    	}
		return newDocNode;
	}
	private Doc setExistDocId(Doc doc) {
		long newId = -1;
		//id�����Ō������Ă��R�s�[���ł��ǉ����Ă���ʂȃm�[�h��hit����\��������̂�
		//���̑��̑�������v���Ă�����̂���������B�������Atext�����́A�X�V����Ă���
		//�\��������̂ŕs��v�ł����܂�Ȃ��Ƃ��Ă���B
		List<Doc> existDocs = docDao_.findSameDoc(doc);
		if(existDocs != null){
			switch(existDocs.size()){
			case 0:
				break;
			case 1:
				newId = existDocs.get(0).getDocId();
//log.debug("impId=" + doc.getDocId() + ", existId="+newId);
//if( doc.getDocId() != newId){
//	log.debug("@@@@@@@@@@@@@@@@@ ���������� doc="+doc);
//}
				break;
			default:
				throw new AppException("<html>text�ȊO���S������Node���������݂��܂��B" +
						"<pre>" +
						" doc="+doc);
			}
		}
		doc.setDocId(newId);
		return doc;
	}

	public DocNode insertDoc(DocNode oyaNode, Doc doc) 
	throws AppException{
		log.trace("start");
		DocNode newDocNode = insertDoc2(oyaNode, doc);
		log.trace("end");
		return newDocNode;
	}
	DocNode insertDoc2(DocNode oyaNode, Doc doc) 
		throws AppException{
			
		boolean isNewNode = false;
		//insert into DB
		if(docDao_.findByDocId(doc.getDocId())== null){
			log.debug("���݂��Ȃ��̂�insert new="+doc);
			if(log.isDebugEnabled()) log.debug("\tnewDoc="+doc);
			doc.check();
			docDao_.insert(doc);
			isNewNode = true;
		}

		//Node link 
		if( !linkDoc(oyaNode, doc, !isNewNode) ){
			//���̃P�[�X�́AoyaNode�z���Ɋ��ɑ��݂���̂�linkDoc���Ȃ������P�[�X
			Enumeration children = oyaNode.children();
			while(children.hasMoreElements()){
				DocNode existNode = (DocNode)children.nextElement();
				if(existNode.getDoc().getDocId() == doc.getDocId()){
					//���̂܂�existNode�����^�������ctrl+X�ňړ��̏ꍇ�A
					//existNode���폜���Ă��܂��o�O���������B
					//���̂��߁A�����ʒu�Ɉړ�����ꍇ�́A��O���X���[����
					//�ʒm���邱�Ƃɂ���B
//					return existNode;
					throw new DoNothingException(existNode);
				}
			}
			//oyaNode���V�����m�[�h�ꍇ�́A������Ȃ��̂ŉ��ō쐬����
		}
		
		//Model�X�V
		DocNode newDocNode = new DocNode(doc);
		insertNodeInto(newDocNode, oyaNode, oyaNode.getChildCount());
		newDocNode.resetParentCount(docStrDao_);
		
//		if(!isNewNode){
			//���̐e�̕\�����X�V
			refreshChildrenFromDb( oyaNode );
			refreshParentCount(
					newDocNode.getDoc().getDocId(),
					newDocNode.getParentCount()
					);
//		}
		return newDocNode;
		
	}
	// true-link���쐬�����Afalse-link���쐬���Ă��Ȃ�
	boolean linkDoc(DocNode oyaNode, Doc doc, boolean needCheck) 
		throws AppException{
		Doc oyaDoc = oyaNode.getDoc();

		if( needCheck ){
			//�ǉ��ł��邩�`�F�b�N����
			switch(canInsert(oyaDoc, doc)){
			case 1:		//���ł�link������
				return false;
			case 0:
				break;
			default:
				log.error("�z�Q�Ɓ@p="+oyaDoc+",c="+doc);
				throw new AppException("�z�Q�ƂɂȂ邽�߃m�[�h�̊֘A���쐬�ł��܂���B");
			}
		}
		
		DocStr yk = new DocStr();
		yk.setOyaDocId(oyaDoc.getDocId());
		yk.setKoDocId(doc.getDocId());
		//SEQ
		yk.setSEQ(docStrDao_.getMaxSeq(oyaDoc.getDocId())+1);
		log.debug("insert doc="+yk);
		
		docStrDao_.insert(yk);
		
		return true;
	}
	// ret = 1:���łɊ֌W������A0:�֌W���Ȃ��̂ō쐬���Ă悢�A-1:�z�Q��
	int canInsert(Doc oyaDoc, Doc doc){
		Set<Long> idSet = new HashSet<Long>();
		
		//oyaDoc�̎q�����W�߂�
		List kouzouList = docStrDao_.findByOyaDocId(oyaDoc.getDocId());
		for(Iterator<DocStr> it = kouzouList.iterator(); it.hasNext();){
			idSet.add(it.next().getKoDocId());
		}
		
		//�����ň�U�`�F�b�N
		if(idSet.contains(doc.getDocId())){
			return 1;
		}
		
		//�e�v���̐�c��S�ďW�߂�
		idSet = collectParent(idSet, oyaDoc.getDocId());

		//�ēx�`�F�b�N
		if(idSet.contains(doc.getDocId())){
			return -1;
		}
		
		return 0;
	}
	Set<Long> collectParent(Set<Long> idSet, long oyaDocId){
		List kouzouList = docStrDao_.findByKoDocId(oyaDocId);
		for(Iterator<DocStr> it = kouzouList.iterator(); it.hasNext();){
			DocStr yk = it.next();
			idSet = collectParent(idSet, yk.getOyaDocId());
		}
		idSet.add(oyaDocId);
		return idSet;
	}
	void removeDocNodeFromParent(long id, long oyaId){
		log.trace("start");
		class Remover implements NodeProcessor{
			long id_;
			long oyaId_;
			public Remover(long id, long oyaId){
				id_ = id;
				oyaId_ = oyaId;
			}
			public boolean process(DocNode node){
				Doc target = node.getDoc();
				if( id_ == target.getDocId()){
					DocNode parent = (DocNode)node.getParent();
					if(oyaId_ == parent.getDoc().getDocId()){
						removeNodeFromParent((MutableTreeNode)node);
					}
				}
				return true;	//���s
			}
		}
		Remover proc = new Remover(id, oyaId);
		processAllNode(proc);
		log.trace("end");
	}

	public void deleteDoc(DocNode removeNode) {
		log.trace("start");
		Doc doc = removeNode.getDoc();
		Doc parent = ((DocNode)removeNode.getParent()).getDoc();
		deleteDoc(parent.getDocId(), doc);

		//���f���̍X�V�iTree�ɓǂݍ��܂�Ă���m�[�h�S�Ă��폜�j
		removeDocNodeFromParent(
				removeNode.getDoc().getDocId(),
				parent.getDocId()
				);
		removeNode.resetParentCount(docStrDao_);
		if(removeNode.getParentCount() > 0){	//�܂��N���̎q���ɂȂ��Ă���
			refreshParentCount(
					removeNode.getDoc().getDocId(),
					removeNode.getParentCount()
					);
		}
		log.trace("end");
	}
	public void deleteDocTrns(DocNode removeNode) {
		log.trace("start");
		try{
			trn_.begin();
			deleteDoc(removeNode);		
			trn_.commit();
			
			log.trace("end");
		}catch(Throwable e){
			try {
				trn_.rollback();
			} catch (Exception e1) {
				//�����̗�O�͖���
			}
			throw new RuntimeException(e);
		}
	}
	void deleteDoc(long parentId, Doc removeDoc){
		long removeId = removeDoc.getDocId();
		//�v���\���̍폜
		docStrDao_.delete(new DocStr(parentId, removeId));
		
		//���̐e�����݂��Ȃ���Ηv�����폜
		int parentCount = docStrDao_.getParentCount(removeId);
		if(parentCount <= 0){
			//�܂��q�����폜
			List<DocStr> children = docStrDao_.findByOyaDocId(removeId);
			for(int i=0; i<children.size(); i++){
				//�����ōēǂݍ��݂���̂́A���������Ȃ��̂��Ǝv���邪�A
				//�폜�̏ꍇ�����Ȃ̂ł悵�Ƃ���B
				Doc koDoc = docDao_.findByDocId(children.get(i).getKoDocId());
				deleteDoc(removeId, koDoc);
			}
			//���g���폜
			docDao_.delete(removeDoc);
		}
	}

	public Doc reloadDoc(DocNode node) {
		log.trace("start");
		if(log.isDebugEnabled()) log.debug("\tDocId="+node.getDoc().getDocId());
		Doc newDoc = docDao_.findByDocId(
					node.getDoc().getDocId()
				);
		if(newDoc != null){
			node.setUserObject(newDoc);	//�V����doc�ɓ���ւ���
			docChanged(newDoc);
		}else{
			//������Ă������B�ǂȂ�����BJTree�ォ������΂������
			node.removeFromParent();
		}
		log.trace("end");
		return newDoc;
	}

	public void updateDoc(Doc doc) {
		log.trace("start");
		doc.check();
		if(log.isDebugEnabled()) log.debug("\t�X�V�O�@doc="+doc);
		docDao_.update(doc);
		if(log.isDebugEnabled()) log.debug("\t�X�V��@doc="+doc);
//		youkenChanged(youken); 
		log.trace("end");
	}

	public List<Doc> getParents(long id) {
		log.trace("start");
		List<DocStr> parents = docStrDao_.findByKoDocId(id);
		List<Doc> ret = new ArrayList<Doc>();
		for(int i=0;i<parents.size();i++){
			long oyaId = parents.get(i).getOyaDocId();
			ret.add(docDao_.findByDocId(oyaId));
		}
		Collections.sort(ret, docComparator_);
		log.trace("end");
		return ret;
	}

	public void renumberSequence(DocNode oyaNode) {
		log.trace("start");
		Enumeration<DocNode> children = oyaNode.children();
		int seq = 0;
		Doc oyaDoc = oyaNode.getDoc();
			
		while(children.hasMoreElements()){
			seq++;
			Doc koDoc = children.nextElement().getDoc();
		    DocStr yk = docStrDao_.find(oyaDoc.getDocId(), koDoc.getDocId());
		    if( yk == null ){
		    	throw new AppException("���̃��x���̃c���[�\�����ύX����Ă��܂��B\n" +
		    			"�e�m�[�h��I������refresh���Ă��������B");
		    }
		    yk.setSEQ(seq);
		    docStrDao_.update(yk);
			log.debug("koDoc="+koDoc);
		}
		
		log.trace("end");
	}

}

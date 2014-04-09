package jp.tokyo.selj.view;

/*
 * 
 * ���ӁI�F���̃I�u�W�F�N�g�́A������JTree�ŋ��L���Ȃ����ƁB
 * �@�@�@�Ƃ������A���̉�ʂ�JTree��TransferHandler���Z�b�g���Ȃ��悤�ɐ݌v���邱�ƁB
 */

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import jp.tokyo.selj.common.AppException;
import jp.tokyo.selj.common.CatString;
import jp.tokyo.selj.common.MessageView;
import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.model.DocModel;
import jp.tokyo.selj.model.DocNode;
import jp.tokyo.selj.view.FrmZeetaMain.ActRefreshSpecific;

import org.apache.log4j.Logger;

public class TreeNodeTransferHandler extends TransferHandler {
	Logger log = Logger.getLogger(this.getClass());
    DataFlavor localFlavor, serialFlavor;
    String localType = DataFlavor.javaJVMLocalObjectMimeType +
                                ";class=jp.tokyo.selj.model.DocModel";
    FrmZeetaMain mainView_;	//�_�C�A���O�̕\���Ɏg�p����
    ActionMap actionMap_;
    enum PassedMethod {NONE, CreTrns, ExpDone_X, ExpDone_C, ImpData, ImpData_ignore}
    PassedMethod passedMethod_ = PassedMethod.NONE;
    Object lastCreatedData_ = null;

    public TreeNodeTransferHandler(FrmZeetaMain mainView, ActionMap actionMap) {
        try {
            localFlavor = new DataFlavor(localType);
        } catch (ClassNotFoundException e) {
       		log.debug("TreeNodeTransferHandler: unable to create data flavor");
        }
        serialFlavor = new DataFlavor(DocNode.class,"DocNode");
        mainView_ = mainView;
        actionMap_ = actionMap;
    }

//	Font planeFont_ = new Font("Dialog", Font.PLAIN, 12);
	private class ImportNodeConfirmationPanel extends JPanel {
		JCheckBox inpFromChildren_;
		JCheckBox inpCopyDataCopy_;
		JLabel messageLabel_;
		JLabel warnig_ = new JLabel(
				"<html>���̃`�F�b�N��On�ɂ����ꍇ��<b>���ӓ_</b><br>" +
				"<pre>" +
				"  1.�V�����m�[�h�́A�ǉ������<br>" +
				"  2.�����̃m�[�h�́A�ė��p����(���̃`�F�b�N��t���Ȃ��ꍇ�́A�ǉ������)<br>" +
				"  3.�����m�[�h�̃^�C�g���ύX�́A���f����Ȃ�<br>" +
				"  4.�����m�[�h�̈ړ��A�폜�͔��f����Ȃ�<br>" +
				"  5.CopyData������Ɍ�Zeeta��Ń^�C�g�����ύX���ꂽ�m�[�h�͊����m�[�h�Ƃ݂Ȃ���Ȃ�<br>" +
				"  6.��x���̋@�\�ŃR�s�[������A�ēxCopyData�����ɓ����m�[�h���R�s�[����ƒǉ��m�[�h�́A2�d�ɓo�^�����<br>" +
				"");

		ImportNodeConfirmationPanel(){
			super();
			initialize();
		}
		void initialize(){
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			messageLabel_ =new JLabel();
//			messageLabel_.setFont(planeFont_);
			add(messageLabel_);
			inpFromChildren_ = new JCheckBox(
					"�q�m�[�h�z������import����"
			);
			add(inpFromChildren_);
			inpCopyDataCopy_ = new JCheckBox(
					"<html>CopyData����Zeeta��̃f�[�^������Zeeta�f�[�^�ɍēx�R�s�[����<br>");
			add(inpCopyDataCopy_);
			inpCopyDataCopy_.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent e) {
					if(inpCopyDataCopy_.isSelected() ){
						add(warnig_);
					}else{
						remove(warnig_);
					}
					SwingUtilities.getWindowAncestor(
							ImportNodeConfirmationPanel.this).pack();
				}});
		}
		void setImportDocName(String name){
			messageLabel_.setText(
			 "<html>" +
			 "����Tree node(<span style=\"color: #CC0000;\"><b>"+ name +"</b></span>)��import���܂��B<br>" +
			 "���A�R�s�[��node�́A�c���[��Ƀ��[�h����Ă��镪����import���܂��B<br>" +
			 "�I��node�z���S�Ă��R�s�[����ꍇ�́A�R�s�[��node��<br>" +
			 "�u�I���m�[�h��S�ēW�J�v�{�^���œW�J���Ă���R�s�[���Ă��������B<br>" +
			 "<br>��낵���ł����H<br>" +
			 "<hr>" +
			 "</html>");
			
		}
	}
	ImportNodeConfirmationPanel confirmImportNodePanel_ = new ImportNodeConfirmationPanel();

	private class ImportTextConfirmationPanel extends JPanel {
		JCheckBox inpImportMemo_;
		JLabel messageLabel_;
		ImportTextConfirmationPanel(){
			super();
			initialize();
		}
		void initialize(){
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			messageLabel_ =new JLabel(
					"<html>" +
					"�e�L�X�g����m�[�h���쐬���܂��B<br>" +
					"�P�s�łP�m�[�h�ATab�̃C���f���g�ŊK�w���쐬���܂��B<br>" +
					"�{������荞�ޏꍇ�A\"�ň͂ނƉ��s���Ă��P�m�[�h�ƂɂȂ�܂��B<br>" +
					"<br>��낵���ł����H<br>" +
					"<hr>" +
					"</html>");
//			messageLabel_.setFont(planeFont_);
			add(messageLabel_);
			inpImportMemo_ = new JCheckBox(
					"�^�C�g�����̌���Tab�ȍ~��{���Ƃ��Ď�荞��"
			);
			inpImportMemo_.setSelected(true);
			add(inpImportMemo_);
		}
	}
	ImportTextConfirmationPanel confirmImportTextPanel_ = new ImportTextConfirmationPanel();

    DocNode createDocNodeFromText(Transferable t) {
		if( JOptionPane.showConfirmDialog(
				mainView_
				,confirmImportTextPanel_
				,""
				,JOptionPane.YES_NO_OPTION
				,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
		}else{
			return null;
		}
		DocNode rootDocNode = new DocNode(new Doc(0, "(imported node)"));
		boolean isExistNode = false;
		try{
			String text = (String)t.getTransferData(DataFlavor.stringFlavor);
			//Node�K�w���쐬
//			String[] lines = text.split(System.getProperty("line.separator"));	�Ȃ����������͑ʖڂ�����
			String[] lines = text.split("\n");
			
			//�{���̃_�u���N�I�[�e�[�V�����Ԃ�A������
			lines = CatString.concatLine(lines);
			
			int preTabSize = -1;
			DocNode preDocNode = rootDocNode;
			for(int i=0; i<lines.length; i++){
				if("".equals(lines[i].trim())){
					continue;	//��s�͖���
				}
				int curTabSize = CatString.countTab(lines[i]); 
				isExistNode = true;
				
				Doc newDoc = makeDoc(i, lines[i]);
				DocNode curDocNode = new DocNode(newDoc);
				if( preDocNode != null){
					int dist = preTabSize - curTabSize;
					if(dist == 0){
						((DocNode)preDocNode.getParent()).add(curDocNode);		//�Z��
					}else if(dist < 0){
						curTabSize = preTabSize + 1;	//���ʂɑ���tab���������狸��
						preDocNode.add(curDocNode);		//�q
					}else{
						for(int j=0; j<dist; j++){
							if(preDocNode.getParent() == null){
								//�P�s�ڂ���tab�������Ă��āA��̍s��tab�Ȃ��s�������
								//���̂悤�ɂȂ�B
								//��j
								//<tab>aaa
								//bbb
								break;
							}
							preDocNode = (DocNode)preDocNode.getParent();
						}
						if(preDocNode == rootDocNode){
							preDocNode.add(curDocNode);		//�d�����Ȃ��̂Ŏq�Ƃ���
						}else{
							((DocNode)preDocNode.getParent()).add(curDocNode);
						}
					}
				}
				preTabSize = curTabSize;
				preDocNode = curDocNode;
			}
			
		}catch(RuntimeException e){
			throw e;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		return (isExistNode)? rootDocNode:null;
    }
    
    
    /*
     * ����������ꍇ�́Anull��Ԃ�
     */
    Doc makeDoc(int no, String importText){
    	importText = importText.trim();
    	Doc doc = null;
    	String memo = null;
    	if(confirmImportTextPanel_.inpImportMemo_.isSelected()){
    		int pos = importText.indexOf('\t');
    		if(pos > 1){
    			memo = importText.substring(pos+1).trim();
    			importText = importText.substring(0, pos).trim();
    		}
    	}
    	doc = new Doc(no+1, importText);	//Id�̓��j�[�N�ł���΂悢
    	if(memo != null){
    		doc.setDocCont(memo);
    	}
    	doc.check();
    	return doc;
    }
    
    
	
    /* �ȉ��̂Q�̃P�[�X�ŌĂяo�����B
     * �Ectrl+V���^�C�v�����ꍇ<br/>
     * �EDnD�Ńh���b�v�����ꍇ
     * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
     */
    public boolean importData(JComponent c, Transferable t) {
    	log.trace("start");
    	
    	boolean isStringData = false;
    	DocNode fromNode = null;
    	if(isStringFlavor(c, t.getTransferDataFlavors())){
    		fromNode = createDocNodeFromText(t);
    		isStringData = true;
    	}else if (!canImport(c, t.getTransferDataFlavors())) {
        	log.debug("import �ł��܂���");
        	passedMethod_ = PassedMethod.NONE;	//�ʂȃf�[�^�������Ă���
            return false;
        }else{
            fromNode = getTransNode(t);
        }

    	if(fromNode == null){
        	passedMethod_ = PassedMethod.NONE;	//�N���b�v�{�[�h�ɂ́A�ʂȃf�[�^�������Ă���
        	return false;
        }

        JTree tree = (JTree)c;	//c�̌^�́AcanImport�Ń`�F�b�N�ς�
        if(tree.getSelectionPath() == null){
        	//�����I������Ă��Ȃ��Ƃ����ɗ���
//        	passedMethod_ = PassedMethod.NONE;
        	return false;
        }
        //�]����Node
        DocNode toNode = (DocNode)tree.getSelectionPath().getLastPathComponent();

        boolean isSameTree = false;
        boolean copyFromChildren = false;
        boolean copyCopyData = false;
        if( isStringData ){
        	copyFromChildren = isStringData;
        }else if (lastCreatedData_ == fromNode) {	//�����c���[���Node
			isSameTree = true;
		}else{
			confirmImportNodePanel_.setImportDocName(fromNode.getDoc().getDocTitle());
			if( JOptionPane.showConfirmDialog(
					mainView_
					,confirmImportNodePanel_
					,""
					,JOptionPane.YES_NO_OPTION
					,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
			}else{
				return false;
			}
			copyFromChildren = confirmImportNodePanel_.inpFromChildren_.isSelected();
			copyCopyData = confirmImportNodePanel_.inpCopyDataCopy_.isSelected();
		}
        log.debug((isSameTree)? "�����c���[���Node":"�قȂ�c���[���Node");
        log.debug("�]������ "+fromNode);
        log.debug("�]����� "+toNode);

        boolean isDnD = false;
        if(passedMethod_ == PassedMethod.CreTrns){	//�����DnD�̂͂�
        	isDnD = true;
        }
        boolean ret;
    	try{
    		mainView_.setCursor(Util.WAIT_CURSOR);
    		if (isSameTree) {
    			try{
	        		paste(tree, fromNode, toNode );
	                if(passedMethod_ == PassedMethod.ExpDone_X){
	        			DocModel youkenModel = (DocModel)((JTree)c).getModel();
	        	 		youkenModel.deleteDocTrns(fromNode);
	                }
    			}catch(DocModel.DoNothingException e){
    				//�����e�Ɉړ������ꍇ�Ȃ̂ŁAdeleteDocTrns���Ȃ�
    				//�������ADnD�̏ꍇ�́A���̌�exportDone���Ăяo����Ă��܂��̂ŁA
    				//������deleteDocTrns����Ă��܂�
    				//������~�߂邽�߂Ɉȉ��̃R�[�h���L�q
    	        	passedMethod_ = PassedMethod.ImpData_ignore;
    			}
                
    	        //ActionType�̏�����
    	        if(passedMethod_ == PassedMethod.CreTrns){	//�����DnD�̂͂�
    	        	passedMethod_ = PassedMethod.ImpData;
    	        }else if(passedMethod_ == PassedMethod.ExpDone_C){
    	//        	passedMethod_ = PassedMethod.NONE;	//�����ŏI���E�E�ɂ���ƘA�����ăy�[�X�g�ł��Ȃ�
    	        }else if(passedMethod_ == PassedMethod.ExpDone_X){
    	        	passedMethod_ = PassedMethod.ExpDone_C;		//���Ƀy�[�X�g���ꂽ�ꍇ�́A���̓���Ƃ���
    	        }else if(passedMethod_ == PassedMethod.ImpData_ignore){
    	        	//DnD�𖳌��ɂ��邽�߂̃}�[�N�Ȃ̂ŉ������Ȃ�
    	        }else{
    	        	RuntimeException e 
    	        		= new RuntimeException("���肦�Ȃ���Ԃ�importData���Ăяo����Ă���");
    	        	log.error(e);
    	        	throw e;
    	        }
                ret = true;
	        }else{
	            pasteFromAnotherProcess(tree, fromNode, toNode, copyFromChildren, copyCopyData);
	        	ret = true;
	            if( !isStringData && copyCopyData){
	            	SwingUtilities.invokeLater(new Runnable(){
	            		public void run(){
	            			JOptionPane.showMessageDialog(
								mainView_
								,"<html>import���������܂����B<br>" +
								 "CopyData����Zeeta�����import�̏ꍇ�A�ɗ͑����i�K�ōēxCopyData���Ă��������B<br>" +
								 "import�����f�[�^�́Aimport���Ƃ́A�ႤID�ɂȂ��Ă���\�������邽�߁A<br>" +
								 "����ȍ~��import�́A�������K�w���쐬����Ȃ��\��������܂��B"
								,""
								,JOptionPane.INFORMATION_MESSAGE);
	            		}
	            	});
	            }
	        }
    	}catch(RuntimeException e){
    		log.error(e);
    		if(isDnD){
    			//�d�����Ȃ��̂Ŏ����ŏo���B
    			MessageView.show(mainView_, e);
    		}
    		throw e;
    	}finally{
    		mainView_.setCursor(Cursor.getDefaultCursor());
    	}
    	
       	
       	log.trace("end. ret="+ret);
        return ret;
    }

	private DocNode getTransNode(Transferable t) {
		DocNode ret = null;
		if(t == null){
			return ret;
		}
		try {
            if (hasLocalArrayListFlavor(t.getTransferDataFlavors())) {
            	ret = (DocNode)t.getTransferData(localFlavor);
            } else if (hasSerialArrayListFlavor(t.getTransferDataFlavors())) {
            	ret = (DocNode)t.getTransferData(serialFlavor);
            }
        } catch (UnsupportedFlavorException ufe) {
        	log.error("importData: unsupported data flavor");
        } catch (IOException ioe) {
        	log.error("importData: I/O exception");
        }
		return ret;
	}
    void paste(JTree jTree, DocNode fromNode, DocNode toNode){
    	log.trace("start");
		
		DocModel docModel = (DocModel)jTree.getModel();
		//�v���\�����쐬
		DocNode newNode = docModel.insertDoc(
					toNode,
					fromNode.getDoc());
		//refresh
		Action act = actionMap_.get(ActRefreshSpecific.class);
		act.putValue(ActRefreshSpecific.REFRESH_NODE, newNode);
		act.actionPerformed(null);
		
		jTree.expandPath(new TreePath(toNode.getPath()));
		
		//�y�[�X�g����Node���A�N�e�B�u�ɂ���
		setSelection(jTree, toNode, newNode.getDoc().getDocId());
		
    	log.trace("end");
    }
    /**
     * node������docId��id�̎q�m�[�h�Ƀt�H�[�J�X���Z�b�g����
     * @param jTree
     * @param toNode
     * @param id
     */
    void setSelection(JTree jTree, DocNode node, long id){
		//�y�[�X�g����Node���A�N�e�B�u�ɂ���
		Enumeration children = node.children();
		while(children.hasMoreElements()){
			DocNode child = (DocNode)children.nextElement();
			if(child.getDoc().getDocId() == id){
				TreePath path = new TreePath(child.getPath());
				jTree.setSelectionPath(path);
				jTree.scrollPathToVisible(path);
				break;
			}
		}
    	
    }
    
    void pasteFromAnotherProcess(JTree jTree, DocNode fromNode, DocNode toNode, 
    		boolean ignoreRoot, boolean copyCopyData){
    	log.trace("start");
    	
    	DocNode newNode = toNode;	//�G���[�����������ꍇ�̂��߂ɂ�������Ă���
    	try{
			DocModel youkenModel = (DocModel)jTree.getModel();
			
			//�y�[�X�g��m�[�h�ƃy�[�X�g�m�[�h�̗v���\�����쐬
			if(ignoreRoot){	//fromNode��root�͒ǉ����Ȃ�
				Enumeration children = fromNode.children();
				while(children.hasMoreElements()){
					newNode = (DocNode)children.nextElement();
					youkenModel.insertDocFromAnotherProcessTrns(
							toNode,	newNode, copyCopyData);
				}
			}else{
				newNode = youkenModel.insertDocFromAnotherProcessTrns(
						toNode,	fromNode, copyCopyData);
			}

    	}finally{
    		//�z�Q�Ɠ��̃G���[����������ƁADB�̓��[���o�b�N����邪
    		//�c���[��Ƀf�[�^���ǉ����ꂽ�܂܂ɂȂ��Ă��܂��̂ŁArefresh����B
			Action act = actionMap_.get(ActRefreshSpecific.class);
			act.putValue(ActRefreshSpecific.REFRESH_NODE, newNode);
			act.actionPerformed(null);
			
			jTree.expandPath(new TreePath(toNode.getPath()));
			//�y�[�X�g����Node���A�N�e�B�u�ɂ���
			if(newNode != null){
				setSelection(jTree, toNode, newNode.getDoc().getDocId());
			}
    	}
    	
    	log.trace("end");
    }
    void printNode(DocNode fromNode, String indent){
    	log.debug(indent+fromNode.getDoc());
    	indent = indent + "\t";
    	Enumeration children = fromNode.children();
    	while(children.hasMoreElements()){
    		DocNode child = (DocNode)children.nextElement();
    		printNode(child,indent);
    	}
    }
    /* �ȉ��̂Q�̃P�[�X�ŌĂяo�����B
     * �Ectrl+X, ctrl+C���^�C�v�����ꍇ<br/>
     * �EDnD�Ńh���b�v�����ꍇimport�̌�ɌĂяo�����
     * @see javax.swing.TransferHandler#exportDone(javax.swing.JComponent, java.awt.datatransfer.Transferable, int)
     */
    protected void exportDone(JComponent c, Transferable t, int action) {
    	log.trace("start");
		DocNode fromNode = getTransNode(t);
        if(fromNode == null){
        	passedMethod_ = PassedMethod.NONE;	//�N���b�v�{�[�h�ɂ́A�ʂȃf�[�^�������Ă���
        	lastCreatedData_ = null;
        	return ;
        }
		if(action == MOVE){
			if(fromNode.isRoot()){
	        	passedMethod_ = PassedMethod.NONE;
	        	lastCreatedData_ = null;
				throw new AppException("Root�m�[�h�͈ړ��ł��܂���");
			}
		}
        if(passedMethod_ == PassedMethod.CreTrns){
			if(action == MOVE){
				passedMethod_ = PassedMethod.ExpDone_X;
			}else{
				passedMethod_ = PassedMethod.ExpDone_C;
			}
        }else if(passedMethod_ == PassedMethod.ImpData){	//�����DnD�̃P�[�X
			passedMethod_ = PassedMethod.NONE;
			if(action == MOVE){
				DocModel youkenModel = (DocModel)((JTree)c).getModel();
		 		youkenModel.deleteDocTrns(fromNode);
			}
        }else if(passedMethod_ == PassedMethod.ImpData_ignore){	//�����DnD�𖳌��ɂ���P�[�X
			passedMethod_ = PassedMethod.NONE;
        }else{
        	//���̏�Ԃ́A���肦�Ȃ�
        	RuntimeException e 
        		= new RuntimeException("���肦�Ȃ���Ԃ�importData���Ăяo����Ă���");
        	log.error(e);
			MessageView.show(mainView_, e);
        	throw e;
        }

    	log.trace("end");
    }

    private boolean hasLocalArrayListFlavor(DataFlavor[] flavors) {
        if (localFlavor == null) {
            return false;
        }

        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(localFlavor)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSerialArrayListFlavor(DataFlavor[] flavors) {
        if (serialFlavor == null) {
            return false;
        }

        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].equals(serialFlavor)) {
                return true;
            }
        }
        return false;
    }
    public boolean isStringFlavor(JComponent c, DataFlavor[] flavors) {
        if ( !(c instanceof JTree) ) {return false;}
        for (int i = 0; i < flavors.length; i++) {
            if (DataFlavor.stringFlavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean canImport(JComponent c, DataFlavor[] flavors) {
    	log.debug("flavors="+flavors);
        if ( !(c instanceof JTree) ) {return false;}
        
        //�ȉ��̓T���v���ɂ���������
        if (hasLocalArrayListFlavor(flavors))  { return true; }
        if (hasSerialArrayListFlavor(flavors)) { return true; }
        return false;
    }

    protected Transferable createTransferable(JComponent c) {
    	log.trace("start");
    	Transferable ret = null;
    	JTree tree;
        if (c instanceof JTree) {
            tree = (JTree)c;
            if(tree.getSelectionPath() == null){
            	return null;
            }
            DocNode selectedNode = 
            	(DocNode)tree.getSelectionPath().getLastPathComponent();
            if (selectedNode == null) {
                return null;
            }
            ret = new TreeNodeTransferable(selectedNode);
            lastCreatedData_ = selectedNode;
			passedMethod_ = PassedMethod.CreTrns;
        }
    	log.trace("end");
        return ret;
    }

    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    /**
     * @author mikami
     *
     */
    public class TreeNodeTransferable implements Transferable {
    	DocNode node_;
        public TreeNodeTransferable(DocNode node) {
        	log.trace("start");
        	node_ = node;
        	log.trace("end");
        }

		public Object getTransferData(DataFlavor flavor)
                                 throws UnsupportedFlavorException {
        	log.trace("start");
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
        	log.trace("end");
            return node_;
        }

        public DataFlavor[] getTransferDataFlavors() {
        	log.trace("start/end");
            return new DataFlavor[] { localFlavor,
                                      serialFlavor };
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
        	log.trace("start");
        	log.debug("flavor="+flavor);
            if (localFlavor.equals(flavor)) {
                return true;
            }
            if (serialFlavor.equals(flavor)) {
                return true;
            }
        	log.trace("end");
            return false;
        }
    }
    
}


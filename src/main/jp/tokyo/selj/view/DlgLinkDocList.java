package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.dao.DocDao;
import jp.tokyo.selj.dao.Output;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.model.DocNode;
import jp.tokyo.selj.view.PnlNodeList.SelectionLintener;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;

public class DlgLinkDocList extends JDialog implements ListSelectionListener{
	Logger log = Logger.getLogger(this.getClass());

	private JPanel jContentPane = null;
	FrmZeetaMain mainView_ = null;
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;
	DocDao docDao_ = null;
	Output lastSelected_ = null;

	private PnlNodeList dspDocs = null;
	private SelectionLintener docSelectionListener_ = new SelectionLintener(){
		public void process(Doc doc){
			mainView_.showDocNode(doc.getDocId(), true);
		}
		public void setRootNode(DocNode rootNode){}
	};

	/**
	 * This is the default constructor
	 */
	public DlgLinkDocList(JFrame owner) {
		super(owner);
		initialize();
	}
	/**
	 * This is the default constructor
	 */
	public DlgLinkDocList(JDialog owner) {
		super(owner);
		initialize();
	}

	public void setup(FrmZeetaMain mainView){
		mainView_ = mainView;
		docDao_ = (DocDao) daoCont_.getComponent(DocDao.class);
	}

	@Override
	public void setVisible(boolean b) {
		if(b){
			throw new RuntimeException("setVisible(boolean b, Output output)������Ȃ�����");
		}else{
			super.setVisible(b);
		}
	}
	public void setVisible(boolean b, Output output) {
		super.setVisible(b);
		lastSelected_ = output;
		refreshDocList();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(500, 200);
		this.setContentPane(getJContentPane());
		this.setTitle("Linked Doc List");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getDspYoukens(), BorderLayout.CENTER);
		}
		return jContentPane;
	}


	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private PnlNodeList getDspYoukens() {
		if (dspDocs == null) {
			dspDocs = new PnlNodeList();
		}
		return dspDocs;
	}

	public void valueChanged(ListSelectionEvent e) {
		//�I�����ꂽitem
		JList list = (JList)e.getSource();
		if( !isShowing() || list.getSelectedValue() == null){
			return;
		}
		if(list.getSelectedValue() == lastSelected_){
			//�Ȃ��������C�x���g���Q�A�����邽��
			return;
		}
		log.debug("selected="+list.getSelectedValue());
		lastSelected_ = (Output)list.getSelectedValue();
		refreshDocList();
	}
	void refreshDocList(){
		//lastSelected_�Ɋ֘A����Doc�ꗗ���擾
		List<Doc> docs = docDao_.findByOutputId(lastSelected_.getOutputId());
		getDspYoukens().setup(docs, docSelectionListener_);
	}

}  //  @jve:decl-index=0:visual-constraint="14,7"

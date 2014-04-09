package jp.tokyo.selj.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.model.DocModel;
import jp.tokyo.selj.model.DocNode;

import org.apache.log4j.Logger;

class DocTreeCellRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 1L;
	static ImageIcon IMG_DEF = new ImageIcon(DocTreeCellRenderer.class.getResource("/image/default.gif"));
	static ImageIcon IMG_COMMON = new ImageIcon(DocTreeCellRenderer.class.getResource("/image/common.gif"));
	static ImageIcon IMG_WORK = new ImageIcon(DocTreeCellRenderer.class.getResource("/image/work.gif"));
	static ImageIcon IMG_COMMONWORK = new ImageIcon(DocTreeCellRenderer.class.getResource("/image/commonWork.gif"));

	Logger log = Logger.getLogger(this.getClass());
	
	//�ꎞ�I�Ɉ��t����m�[�h
	DefaultMutableTreeNode markingNode_;
	Border markBorder_ = null;
	Border commonBorder_ = null;

	DocModel youkenModel_ = null;

//	Border borderCommon = new
	static Font italicFont_ =null;

	public DocTreeCellRenderer(DocModel docModel) {
		super();
		youkenModel_ = docModel;
	}
	Font cnvItalicFont(Font treeFont){
		if(italicFont_==null
		   ||
		   !(treeFont.getName().equals(italicFont_.getName())
			  && treeFont.getSize() == italicFont_.getSize() )
		){
			italicFont_ = new Font(
						treeFont.getName()
						,treeFont.getStyle() | Font.ITALIC
						,treeFont.getSize()
					);
		}
		return italicFont_;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

//		setOpaque(true); //��������ƑI����Ԃ̔w�i���`�悳��Ȃ�
		if (sel) {
			setBackground(getBackgroundSelectionColor());
			setForeground(getTextSelectionColor());
		} else {
			setBackground(getBackgroundNonSelectionColor());
			setForeground(getTextNonSelectionColor());
		}
		setEnabled(tree.isEnabled());
		
		DocNode docNode = null;
		if(value instanceof DocNode){	//JTree�Ƀ��f�����Z�b�g����O��String��`�悷��炵��
			docNode = (DocNode)value;
		}
		if(docNode != null){
			Doc doc = docNode.getDoc();
			setText(doc.getDocTitle());
			
//			Dimension preferredSize = getPreferredSize();
//			if(preferredSize.height > 0){
//				preferredSize.width += 100;
//				setPreferredSize(preferredSize);
//			}			
			
			
			//�A�C�R���̃Z�b�g
			int flags = 0;
			if( docNode.getParentCount() > 1){
				flags += 1;
			}
			if( docNode.getDoc().getWorkCount() > 0){
				flags += 2;
			}
			switch(flags){
			case 0:
				setIcon(IMG_DEF);
				break;
			case 1:
				setIcon(IMG_COMMON);
				break;
			case 2:
				setIcon(IMG_WORK);
				break;
			case 3:
				setIcon(IMG_COMMONWORK);
				break;
			}

			decorateFont(tree, docNode);

			setBorder(null);
			//�I�𒆃m�[�h�Ɠ���ID
			if(!sel && (tree.getSelectionPath() != null) ){
				if( docNode.getDoc().getDocId() ==
					((DocNode)tree.getSelectionPath().getLastPathComponent()).getDoc().getDocId() ){
					setBorder(getCommonBorder());
				}
			}
			//�}�[�L���O�m�[�h�H
			if(markingNode_ == value){
				setBorder(getMarkBorder());
			}
		}

//		setComponentOrientation(tree.getComponentOrientation());
		selected = sel;	//JDK�̃\�[�X���i��������Ȃ��ƑI����Ԃ̕\���ɂȂ�Ȃ��j
		return this;
	}
	protected void decorateFont(JTree tree, DocNode docNode){
		if(docNode.getDoc().getDocTypeId() == Doc.LINK_TYPE){
			setFont(cnvItalicFont(tree.getFont()));
		}else{
//			//����Pref��Font�̃Z�b�g
//			if(getText().startsWith("��")
//				|| getText().startsWith("->") 
//				|| getText().startsWith("=>") 
//				){
//				setFont(cnvItalicFont(tree.getFont()));
//			}else{
				setFont(tree.getFont());
//			}
		}
	}
	private Border getCommonBorder() {
		if( commonBorder_ == null){
			commonBorder_ = new MatteBorder(0,0,2,0,Color.ORANGE);
		}
		return commonBorder_;
	}
	protected Border getMarkBorder() {
		if( markBorder_ == null){
			markBorder_ = new LineBorder(Color.RED, 1, true);
		}
		return markBorder_;
	}
	public void setMarkingNode(DefaultMutableTreeNode node){
		markingNode_ = node;
	}
}

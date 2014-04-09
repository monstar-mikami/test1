package jp.tokyo.selj;

import javax.swing.JFrame;

import jp.tokyo.selj.common.MessageView;
import jp.tokyo.selj.dao.ModelCheckException;
import jp.tokyo.selj.dao.SysZeeta;
import jp.tokyo.selj.view.FrmZeetaMain;

import org.apache.log4j.Logger;

public class ZeetaMain  {
	Logger log_ = Logger.getLogger(this.getClass());
	static FrmZeetaMain mainView_;
	
	public static FrmZeetaMain getMainView(){
		return mainView_;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ZeetaMain main = new ZeetaMain();
		main.go();
	}
	protected FrmZeetaMain newMainView(){
		//version check, ������sysZeeta�e�[�u���̓��e�����[�h�����
		ZeetaDBManager.check();
		
		ZeetaUI.setup();
		
		FrmZeetaMain view = new FrmZeetaMain();
		view.setup();
		return view;
	}
	void go(){
		log_.trace("start zeeta.");
		
		//�f�t�H���g��O�n���h���o�^
		Thread.setDefaultUncaughtExceptionHandler(
			new Thread.UncaughtExceptionHandler(){
				public void uncaughtException(Thread t, Throwable e) {
					log_.error("Exception",e);
					if(mainView_ == null){	//���C����ʂ��\�������O�̃G���[
						MessageView.show(new JFrame(), e);
						System.exit(1);
					}else{
						MessageView.show(mainView_, e);
					}
					
					if( e instanceof ModelCheckException){		//���̓`�F�b�N�G���[
						//�������Ȃ�
					}else{
						//��Ԃ�����������
//						mainView_.initState();
						//Doc��insert/update�Ō����ӂ�G���[�ł������ɂ���̂ŁA
						//�ւ��ɏ��������Ȃ���������
					}
				}
			}
		);
		
		//splash��ʕ\���`Mein��ʕ\��
		new Splash(){
			@Override
			public void execute() {
				log_.trace("creating MainView...");
				mainView_ = newMainView();
				mainView_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//				mainView_.setLocationRelativeTo(null);	//��ʂ̒����ɕ\��
				mainView_.setVisible(true);
				log_.trace("show MainView.");
			}
			
		};
		
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				MainView thisClass = new MainView(true);
//				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//				thisClass.setVisible(true);
//			}
//		});
	}
}

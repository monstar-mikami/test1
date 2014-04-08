ああああ

package jp.co.nc.mill1on.common.cooperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.co.monstar.test.server.BeanEcho;
import jp.co.monstar.test.server.BeanEchoDto;
import jp.co.nc.mill1on.common.exception.AppException;
import jp.co.nc.mill1on.common.exception.SysException;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.util.StringUtil;


/**
 * 連携クラスの基底クラス<br>
 *
 * @author mikami
 */
public abstract class RenkeiBase{
	protected Logger log_ = Logger.getLogger(this.getClass());
	Object service_;

	/**
	 * ログ出力する
	 * @return
	 */
	protected void logout(String contents){
		log_.info(contents);
	}


	/**
	 * diconファイルパスを返却する
	 * 例）return "jp/co/monstar/test/CallBeanEcho.dicon";
	 * @return
	 */
	protected abstract String getDiconFilePath();

	/**
	 * 外部連携先のインターフェースクラスを返却する
	 * 例）return Cafe.class;
	 * @return
	 */
	protected abstract Class getRenkeiInterface();

	protected RenkeiBase(){
		S2Container container = S2ContainerFactory.create(getDiconFilePath());
        container.init();
		service_ = container.getComponent(getRenkeiInterface());
	}

	/**
	 * リタン値をチェック
	 * エラーの場合は、SysExceptionをスローする
	 * @param param
	 */
	protected Map checkReturnMap(Map<String, Object> param){
		return (Map)checkReturn(param);
	}
	protected Object[] checkReturnArray(Map<String, Object> param){
		Object ret =checkReturn(param);
		if(ret instanceof List){
			ret = ((List) ret).toArray();
		}else if(ret instanceof Object[]){
			//何もしない
		}else{
			throw new SysException("resultが不明な型.result.getClass()="+ret.getClass().getName());
		}
		return (Object[])ret;
	}

	protected Object checkReturn(Map<String, Object> ret){
		String statusCode = (String)ret.get("statusCode");
		if(Cooperation.RESULT_STATUS_OK.equals(statusCode)){
			Object result = ret.get("result");
			if(result == null){
				throw new SysException("外部連携エラー Result is null.");
			}
			return result;
		}else if(Cooperation.RESULT_STATUS_SA_DELETED.equals(statusCode)){
			throw new AppException("既に存在しないSAコードです。");
		}else{
			String errCode = "";
			Object o = ret.get("errCode");
			if((o!=null) && (o instanceof String) && !StringUtil.isEmpty(o.toString())){
				errCode = o.toString();
			}
			throw new SysException("外部連携エラー statusCode="+statusCode+", errCode="+errCode);
		}
	}



//	private void ex2() throws Exception{
//		final String  PATH="jp/co/monstar/test/CallBeanEcho.dicon";
//
//	    // 設定ファイルを読み込む.
//        SingletonS2ContainerFactory.setConfigPath(PATH);
//
//        // 初期化する.
//        SingletonS2ContainerFactory.init();
//
//        // コンテナを取得する.
//        S2Container container = SingletonS2ContainerFactory.getContainer();
//
//        // コンポーネントを呼び出す.
//        BeanEcho service = (BeanEcho) container.getComponent(BeanEcho.class);
//
//        BeanEchoDto param = new BeanEchoDto();
//        BeanEchoDto result = service.echo(param);
//
//        System.out.println(result);
//
//	}


}

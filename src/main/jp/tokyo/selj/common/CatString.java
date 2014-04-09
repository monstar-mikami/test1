package jp.tokyo.selj.common;

import java.util.ArrayList;
import java.util.List;

public class CatString {

	/**
	 * �����G���g���ɕ�������Ă���s���P�G���g���ɂ܂Ƃ߂�<br>
	 * <li>
	 * <ul>1."�ň͂܂ꂽ�����́A�s��������Ă��Ă�����"�܂ł𒊏o����</ul>
	 * <ul>2.1�Œ��o������Ɏc���������́A���s�܂ł�</ul>
	 * </li>
	 * @param lines
	 * @return
	 */
	public static String[] concatLine(String[] lines){
    	List<String> newLines = new ArrayList<String>();
    	boolean isFirstLine = true;
		int index = 0;
		
		while(index < lines.length){
			String catLine = "";
			String line = lines[index].replaceAll("\"\"", "&h0000"); 
			int tabCount = countTab(lines[index]);
			while(!"".equals(line)){
				line = line.trim();
				//�擪��"�Ŏn�܂�
				if(line.startsWith("\"")){
					//�����s�Ŏ���"��T��
					int pos = line.indexOf('\"', 1);
					if(pos >= 0){
						catLine += (isFirstLine)? tabs(tabCount):"\t";
						catLine += line.substring(1, pos);
						line = line.substring(pos+1);
					}else{
						//���s�ɂ܂�����ꍇ
						catLine += (isFirstLine)? tabs(tabCount):"\t";
						catLine += line.substring(1);
		    			line = "";
						//"���o�Ă���܂�catLine�ɘA��
						boolean loop = true;
						while(loop && ((index+1) < lines.length)){
							index++;
							line = lines[index].replaceAll("\"\"", "&h0000");
				    		int to = line.indexOf('\"');
				    		if(to <= -1){
				    			to = line.length();
				    		}else{
				    			loop = false;
				    		}
			    			catLine += "\n" + line.substring(0, to);
			    			line = line.substring(to);
			    			if(!loop){	//"���݂������ꍇ�́A"���폜
				    			line = line.substring(1);
			    			}
						}
					}
				}else{
					//�擪��"�Ŏn�܂�Ȃ��ꍇ��tab�܂ł��^�C�g���Ƃ���
		    		int to = line.indexOf('\t');
		    		if(to <= -1){
		    			to = line.length();
		    		}
					catLine += (isFirstLine)? tabs(tabCount):"\t";
	    			catLine += line.substring(0, to);
	    			line = line.substring(to);
				}
				isFirstLine = false;
			}
			newLines.add(catLine);
			index++;
			isFirstLine = true;
		}

		String[] ret = new String[newLines.size()];
		ret = newLines.toArray(ret);
		//�u�����Ă�����"�����ɖ߂�
		for(int i=0; i<ret.length; i++){
			ret[i] = ret[i].replaceAll("&h0000", "\"");
		}
		return ret;
    }
	static String tabs(int count){
		String ret = "";
		for(int i=0;i < count; i++){
			ret += "\t";
		}
		return ret;
	}
    static String extractUntilTab(String line){
		if(line.startsWith("\"")){
			
		}
    	return line;
    }
    public static int countTab(String line){
    	int ret = 0;
    	for(int i=0;i<line.length(); i++){
    		if(line.charAt(i) == '\t'){
    			ret++;
    		}else{
    			break;
    		}
    	}
    	return ret;
    }

}

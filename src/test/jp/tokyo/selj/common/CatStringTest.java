package jp.tokyo.selj.common;

import jp.tokyo.selj.common.CatString;
import junit.framework.TestCase;

public class CatStringTest extends TestCase {

	public void testConcatLine01() {
		String[] lines = new String[]{
				"������ ������"
		};
		
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "������ ������");
	}
	public void testConcatLine02() {
		String[] lines = new String[]{
				"\t������ ������"
		};
		
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "\t������ ������");
	}
	public void testConcatLine03() {
		String[] lines = new String[]{
				"������\t\"������\""
		};
		
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "������\t������");
	}
	public void testConcatLine04() {
		String[] lines = new String[]{
				"������\t\t\"������\""
		};
		
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "������\t������");
	}
	public void testConcatLine05() {
		String[] lines = new String[]{
				"\"������\" ������"
		};
		//�^�C�g����"�ň͂܂�Ă�����tab���Ȃ��Ă��㔼��memo�Ƃ���
		//���̂��߁A��؂蕶���́Atab�ɒu�������
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "������\t������");
	}
	public void testConcatLine06() {
		String[] lines = new String[]{
				"\"������\"\t������"
		};
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "������\t������");
	}
	public void testConcatLine07() {
		String[] lines = new String[]{
				"\"������\"\t  \"������\""
		};
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "������\t������");
	}
	public void testConcatLine08() {
		String[] lines = new String[]{
				"\"������\" \"������"
		};
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "������\t������");
	}
	public void testConcatLine09() {
		String[] lines = new String[]{
				"\"������\"\t  \"������\" \"������\""
		};
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "������\t������\t������");
	}
	public void testConcatLine10() {
		String[] lines = new String[]{
				"\"������\"\t  \"������\" \"������\"    ������"
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "������\t������\t������\t������");
	}
	public void testConcatLine11() {
		String[] lines = new String[]{
				"\"������\"\t  \"������\" \"������\"    ������ ������"
		};
		lines = CatString.concatLine(lines);
		 assertEquals(lines[0], "������\t������\t������\t������ ������");
	}

	//==== �����s�Ɍׂ�
	public void testConcatLine20() {

	}
	public void testConcatLine21() {
		String[] lines = new String[]{
				"������\t\"��"
				,"��"
				,"��\""
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "������\t��\n��\n��");
	}
	public void testConcatLine22() {
		String[] lines = new String[]{
				"\"��"
				,"��"
				,"��\"\t������"
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "��\n��\n��\t������");
	}
	public void testConcatLine23() {
		String[] lines = new String[]{
				"������\t\t\t\t\"��"
				,"��"
				,"��"
				,"���ււ�\""
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "������\t��\n��\n��\n���ււ�");
	}
	public void testConcatLine24() {
		String[] lines = new String[]{
				"������\t\t\t\t\"��"
				,"----��"
				,"\t��"
				,"���ււ�\""
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "������\t��\n----��\n\t��\n���ււ�");
	}
	public void testConcatLine25() {
		String[] lines = new String[]{
				"\"��"
				,"----��"
				,"��\"\t������"
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "��\n----��\n��\t������");
	}

	//==== "" ��"�ƔF������
	public void testConcatLine40() {
		String[] lines = new String[]{
				"��\"\"����"
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "��\"����");
	}
	public void testConcatLine41() {
		String[] lines = new String[]{
				"��\"\"����\t����\"\"��"
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "��\"����\t����\"��");
	}


	//====
	public void testConcatLine90() {
		String[] lines = new String[]{
				"�F�؁E�F��\t\"���p�Ҏ��_"
				,"    * ���p�҂ɕt�^�����ŗLID�A�p�X���[�h�ɂ��CWS�����R�ɕҏW���邱�Ƃ��o����B"
				,"�Ǘ��Ҏ��_\""
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "�F�؁E�F��\t���p�Ҏ��_\n"
				+"    * ���p�҂ɕt�^�����ŗLID�A�p�X���[�h�ɂ��CWS�����R�ɕҏW���邱�Ƃ��o����B\n"
				+"�Ǘ��Ҏ��_"
				);
	}
	public void testConcatLine91() {
		String[] lines = new String[]{
				"������\t\"��"
				,"��"
				,"��"	//����"�������Ă���
		};
		lines = CatString.concatLine(lines);
		assertEquals(lines[0], "������\t��\n��\n��");
	}
}

@echo ----------------------------------------------------
@echo ��Zeeta��DB�����������đ���Zeeta��DB���R�s�[����T���v���ł��B
@echo ���̗�ł́ApostgreSQL��DB����DB�ɃR�s�[���܂��B
@echo ���s�ɂ́ApostgreSQL��JDBC�h���C�o���J�����g�t�H���_�ɕK�v�ł��B
@echo ��낵���ł����H
@echo �L�����Z������ꍇ�́ACTRL+C���^�C�v���Ă��������B
@echo ----------------------------------------------------
@set CLS=../lib/selj.jar
@pause
@del /Q db\*.*
@java -cp %CLS% jp.tokyo.selj.util.DbSetup
@java -cp %CLS% ^
-DsrcDb.driver=org.postgresql.Driver ^
-DsrcDb.url=jdbc:postgresql://localhost:5432/zeeta ^
-DsrcDb.user=zeeta ^
-DsrcDb.password=zeeta ^
jp.tokyo.selj.util.CopyData 
@pause

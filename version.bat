@echo off
set /p newVersion=������汾�ţ�
echo ��ʼ�޸���Ŀ�����Ժ�......
echo --------------------------------------------

mvn versions:set -P !artifactory,!oss,!sonar -DnewVersion=%newVersion%
echo  ��ϲ��  �����ɹ�  ������һ��......
pause
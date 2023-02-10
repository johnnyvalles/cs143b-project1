/bin/rm -f *.class
/bin/rm -rf ~/cs143b-project1-submission-vallesja
/bin/mkdir ~/cs143b-project1-submission-vallesja
/bin/cp *.java ~/cs143b-project1-submission-vallesja
/bin/cp README.md ~/cs143b-project1-submission-vallesja

javac *.java && java ManagerDriver < test/input.txt > output.txt
/bin/cp ./output.txt ~/
/bin/rm -f *.class
/bin/rm -f output.txt